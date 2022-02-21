use std::{any::Any, collections::HashMap, fmt::Debug, iter, sync::Arc};

use wasmtime::{Memory, StoreContextMut, Val, ValType};

use crate::{
    errors::Error,
    interoperable_val::{InteroperableType, InteroperableVal},
    module::StoreContents,
    utils::error_accumulator,
};

#[derive(Debug, Clone)]
pub struct Signature {
    pub arguments: Vec<InteroperableType>,
    pub returns: Vec<InteroperableType>,
}

impl Signature {
    pub fn as_bytes(&self) -> Vec<u8> {
        self.arguments
            .iter()
            .map(|v| v.to_type_index())
            .chain(iter::once(0))
            .chain(self.returns.iter().map(|v| v.to_type_index()))
            .collect()
    }

    pub fn from_bytes(bytes: &[u8], module_name: &str) -> Result<Signature, Error> {
        let type_indexes = bytes.split(|v| *v == 0).map(|indexes| {
            indexes
                .iter()
                .map(|index| InteroperableType::from_type_index(*index))
                .fold(Ok(Vec::new()), error_accumulator)
        });

        // trying to convert type_indexes to a vec and destructure it normally gives an E0508 error for some reason

        let mut array = [Vec::new(), Vec::new()];

        for (i, v) in type_indexes.enumerate() {
            if i > 1 {
                return Err(Error::CorruptedModule(format!(
                    "Signature `{}` is invalid, too many zeroes",
                    module_name.to_owned()
                )));
            }

            array[i] = v.ok().ok_or_else(|| {
                Error::CorruptedModule(format!(
                    "Signature `{}` has invalid type ids",
                    module_name.to_owned()
                ))
            })?;
        }

        let [arguments, returns] = array;

        Ok(Signature { arguments, returns })
    }
}

pub fn call_export(
    store: &mut StoreContextMut<StoreContents>,
    name: &str,
    args: &[InteroperableVal],
) -> Result<Vec<InteroperableVal>, Error> {
    Rust::call_export(store, name, args)
}

pub fn export_signature(
    store: &mut StoreContextMut<StoreContents>,
    name: &str,
) -> Result<Signature, Error> {
    Rust::export_signature(store, name)
}

pub fn call_import<'a>(
    store: &mut StoreContextMut<StoreContents>,
    name: &'a str,
    callback: Box<dyn FnOnce(&[InteroperableVal]) -> Result<Vec<InteroperableVal>, Error> + 'a>,
    args: &[Val],
) -> Result<Vec<Val>, Error> {
    Rust::call_import(store, name, callback, args)
}

pub fn import_signature(
    store: &mut StoreContextMut<StoreContents>,
    name: &str,
) -> Result<Signature, Error> {
    Rust::import_signature(store, name)
}

pub trait Language: Any {
    fn call_export(
        store: &mut StoreContextMut<StoreContents>,
        name: &str,
        args: &[InteroperableVal],
    ) -> Result<Vec<InteroperableVal>, Error>;

    fn export_signature(
        store: &mut StoreContextMut<StoreContents>,
        name: &str,
    ) -> Result<Signature, Error>;

    fn call_import<'a>(
        store: &mut StoreContextMut<StoreContents>,
        name: &'a str,
        callback: Box<dyn FnOnce(&[InteroperableVal]) -> Result<Vec<InteroperableVal>, Error> + 'a>,
        args: &[Val],
    ) -> Result<Vec<Val>, Error>;

    fn import_signature(
        store: &mut StoreContextMut<StoreContents>,
        name: &str,
    ) -> Result<Signature, Error>;
}

pub struct Rust {
    sig_cache: HashMap<String, Result<Signature, Error>>,
}

impl Language for Rust {
    fn call_export(
        store: &mut StoreContextMut<StoreContents>,
        name: &str,
        args: &[InteroperableVal],
    ) -> Result<Vec<InteroperableVal>, Error> {
        let export = Arc::clone(store.data_mut().2.get().unwrap())
            .lock()?
            .instance
            .get_func(&mut *store, name)
            .ok_or_else(|| Error::ExportDoesntExist(name.to_owned()))?;

        let sig = Self::export_signature(&mut *store, name)?;

        let args_wasm = args
            .iter()
            .zip(sig.arguments.iter())
            .map(|(arg, expected_type)| Self::into_arg(store, *expected_type, arg.clone()))
            .fold(Ok(Vec::new()), error_accumulator)?;

        let mut results = export
            .ty(&mut *store)
            .results()
            .map(|ty| match ty {
                ValType::I32 => Val::I32(0),
                ValType::I64 => Val::I64(0),
                ValType::F32 => Val::F32(0.0_f32.to_bits()),
                ValType::F64 => Val::F64(0.0_f64.to_bits()),
                ValType::V128 => Val::V128(0),
                ValType::FuncRef => Val::FuncRef(None),
                ValType::ExternRef => Val::ExternRef(None),
            })
            .collect::<Vec<_>>();

        export.call(&mut *store, &args_wasm, &mut results)?;

        results
            .into_iter()
            .zip(sig.returns.iter())
            .map(|(value, expected_type)| Rust::from_ret(store, *expected_type, &value))
            .fold(Ok(Vec::new()), error_accumulator)
    }

    fn export_signature(
        store: &mut StoreContextMut<StoreContents>,
        name: &str,
    ) -> Result<Signature, Error> {
        Self::signature(store, &format!("__{}_esig", name))
    }

    fn call_import<'a>(
        store: &mut StoreContextMut<StoreContents>,
        name: &'a str,
        callback: Box<dyn FnOnce(&[InteroperableVal]) -> Result<Vec<InteroperableVal>, Error> + 'a>,
        args: &[Val],
    ) -> Result<Vec<Val>, Error> {
        let sig = Self::import_signature(&mut *store, name)?;

        let args_converted = args
            .iter()
            .zip(sig.arguments.iter())
            .map(|(value, expected_type)| Rust::from_ret(store, *expected_type, value))
            .fold(Ok(Vec::new()), error_accumulator)?;

        let ret_ty = sig.returns;
        let ret = callback(&args_converted[..])?;

        ret.into_iter()
            .zip(ret_ty.iter())
            .map(|(value, ty)| Self::into_arg(store, *ty, value))
            .fold(Ok(Vec::new()), error_accumulator)
    }

    fn import_signature(
        store: &mut StoreContextMut<StoreContents>,
        name: &str,
    ) -> Result<Signature, Error> {
        Self::signature(store, &format!("__{}_isig", name))
    }
}

// Using `as` to convert between signed and unsigned wraps as expected
impl Rust {
    pub fn new() -> Rust {
        Rust {
            sig_cache: HashMap::new(),
        }
    }

    fn into_arg(
        store: &mut StoreContextMut<StoreContents>,
        expected_type: InteroperableType,
        value: InteroperableVal,
    ) -> Result<Val, Error> {
        match expected_type {
            InteroperableType::Bool => Ok(Val::I32(if value.loose_bool()? { 1 } else { 0 })),
            InteroperableType::U8 => Ok(Val::I32(value.loose_u8()? as i32)),
            InteroperableType::I8 => Ok(Val::I32(value.loose_i8()? as i32)),
            InteroperableType::U16 => Ok(Val::I32(value.loose_u16()? as i32)),
            InteroperableType::I16 => Ok(Val::I32(value.loose_i16()? as i32)),
            InteroperableType::U32 => Ok(Val::I32(value.loose_u32()? as i32)),
            InteroperableType::I32 => Ok(Val::I32(value.loose_i32()?)),
            InteroperableType::U64 => Ok(Val::I64(value.loose_u64()? as i64)),
            InteroperableType::I64 => Ok(Val::I64(value.loose_i64()?)),
            InteroperableType::F32 => Ok(Val::F32(value.loose_f32()?.to_bits())),
            InteroperableType::F64 => Ok(Val::F64(value.loose_f64()?.to_bits())),
            InteroperableType::String => vec_into_args(value.loose_string()?.into_bytes(), store),
        }
    }

    fn from_ret(
        store: &mut StoreContextMut<StoreContents>,
        expected_type: InteroperableType,
        value: &Val,
    ) -> Result<InteroperableVal, Error> {
        const E: fn() -> Error = || {
            Error::CorruptedModule(
                "Couldn't convert from a wasm val to an interoperable val".to_owned(),
            )
        };

        match expected_type {
            InteroperableType::Bool => Ok(InteroperableVal::Bool(value.i32().ok_or_else(E)? != 0)),
            InteroperableType::I8 => Ok(InteroperableVal::I8(value.i32().ok_or_else(E)? as i8)),
            InteroperableType::U8 => Ok(InteroperableVal::U8(value.i32().ok_or_else(E)? as u8)),
            InteroperableType::I16 => Ok(InteroperableVal::I16(value.i32().ok_or_else(E)? as i16)),
            InteroperableType::U16 => Ok(InteroperableVal::U16(value.i32().ok_or_else(E)? as u16)),
            InteroperableType::I32 => Ok(InteroperableVal::I32(value.i32().ok_or_else(E)?)),
            InteroperableType::U32 => Ok(InteroperableVal::U32(value.i32().ok_or_else(E)? as u32)),
            InteroperableType::I64 => Ok(InteroperableVal::I64(value.i64().ok_or_else(E)?)),
            InteroperableType::U64 => Ok(InteroperableVal::U64(value.i64().ok_or_else(E)? as u64)),
            InteroperableType::F32 => Ok(InteroperableVal::F32(value.f32().ok_or_else(E)?)),
            InteroperableType::F64 => Ok(InteroperableVal::F64(value.f64().ok_or_else(E)?)),
            InteroperableType::String => Ok(InteroperableVal::String(String::from_utf8(
                vec_from_ret(value, store)?,
            )?)),
        }
    }

    fn signature(
        store: &mut StoreContextMut<StoreContents>,
        name: &str,
    ) -> Result<Signature, Error> {
        let mutex = Arc::clone(&store.data_mut().1);
        let mut lock = mutex.lock()?;
        let cached = lock.sig_cache.get(name);

        match cached {
            Some(v) => v.to_owned(),
            None => {
                let sig = Rust::signature_no_cache(store, name);
                lock.sig_cache.insert(name.to_owned(), sig.clone());
                sig
            }
        }
    }

    fn signature_no_cache(
        store: &mut StoreContextMut<StoreContents>,
        name: &str,
    ) -> Result<Signature, Error> {
        let mut ret = [Val::I32(0)];

        let mutex = Arc::clone(store.data().2.get().unwrap());
        let mut lock = mutex.lock()?;

        let instance = &mut lock.instance;

        instance
            .get_func(&mut *store, name)
            .ok_or_else(|| Error::ExportDoesntExist(name.to_owned()))?
            .call(&mut *store, &[], &mut ret)?;

        drop(lock);

        let ptr = ret[0].i32().unwrap() as usize;

        let memory = Rust::memory(&mut *store)?.data(store);

        let len = memory[ptr] as usize;

        Signature::from_bytes(&memory[ptr + 1..ptr + len + 1], name)
    }

    pub fn memory(store: &mut StoreContextMut<StoreContents>) -> Result<Memory, Error> {
        Arc::clone(store.data_mut().2.get().unwrap())
            .lock()?
            .instance
            .get_memory(store, "memory")
            .ok_or_else(|| Error::CorruptedModule("Memory not present".to_string()))
    }

    fn malloc(store: &mut StoreContextMut<StoreContents>, amt: i32) -> Result<i32, Error> {
        let mut out = [Val::I32(0)];

        Arc::clone(store.data_mut().2.get().unwrap())
            .lock()?
            .instance
            .get_func(&mut *store, "__malloc")
            .ok_or_else(|| Error::CorruptedModule("__malloc not present".to_string()))?
            .call(&mut *store, &[Val::I32(amt)], &mut out)?;

        out[0]
            .i32()
            .ok_or_else(|| Error::CorruptedModule("__malloc doesn't return an i32".to_string()))
    }

    fn free(store: &mut StoreContextMut<StoreContents>, ptr: i32, len: i32) -> Result<(), Error> {
        Arc::clone(store.data_mut().2.get().unwrap())
            .lock()?
            .instance
            .get_func(&mut *store, "__free")
            .ok_or_else(|| Error::CorruptedModule("__free not present".to_string()))?
            .call(&mut *store, &[Val::I32(ptr), Val::I32(len)], &mut [])?;

        Ok(())
    }
}

fn vec_into_args<T: Copy + Debug>(
    vector: Vec<T>,
    store: &mut StoreContextMut<StoreContents>,
) -> Result<Val, Error> {
    let len = vector.len() as i32;
    let capacity = vector.capacity();

    let size = capacity * std::mem::size_of::<T>();

    let ptr = Rust::malloc(store, size as i32)?;

    let parts_ptr = Rust::malloc(store, 3 * 4)?;

    let memory = Rust::memory(store)?;

    let bytes = unsafe { std::slice::from_raw_parts(vector.as_ptr() as *const u8, size) };

    memory.write(&mut *store, ptr as usize, bytes)?;

    memory.write(
        &mut *store,
        parts_ptr as usize,
        &[
            ptr.to_le_bytes(),
            len.to_le_bytes(),
            (capacity as i32).to_le_bytes(),
        ]
        .into_iter()
        .flatten()
        .collect::<Vec<_>>()[..],
    )?;

    Ok(Val::I32(parts_ptr))
}

fn vec_from_ret<T: Copy>(
    value: &Val,
    store: &mut StoreContextMut<StoreContents>,
) -> Result<Vec<T>, Error> {
    let memory = Rust::memory(store)?;

    let slice = memory.data(&mut *store);

    let (chunks, _) = slice.as_chunks::<4>();

    let value_converted = value.i32().ok_or_else(|| {
        Error::CorruptedModule("Returned a value other than an i32 as a pointer".to_owned())
    })?;

    let data_ptr = value_converted >> 2;

    let ptr = i32::from_le_bytes(chunks[data_ptr as usize]) as usize;
    let len = i32::from_le_bytes(chunks[data_ptr as usize + 1]) as usize;
    let capacity = i32::from_le_bytes(chunks[data_ptr as usize + 2]) as usize;

    Rust::free(&mut *store, value_converted, 3 * 4)?;

    let size = capacity * std::mem::size_of::<T>();

    let mut buffer = vec![0; size];

    memory.read(&mut *store, ptr, &mut buffer)?;

    Rust::free(&mut *store, ptr as i32, size as i32)?;

    unsafe {
        let vec = Vec::from_raw_parts(buffer.as_mut_ptr() as *mut T, len, capacity);
        std::mem::forget(buffer); // as_mut_ptr doesn't take ownership, this prevents use after free
        Ok(vec)
    }
}
