/*
wasmtime-java was a very helpful reference in writing this
https://github.com/kawamuray/wasmtime-java
*/

#![feature(slice_as_chunks, try_trait_v2)]

mod errors;
mod interoperable_val;
mod java_interface;
mod module;
mod utils;
mod wasm_interop;

use crate::errors::Result;
use crate::java_interface::*;
use crate::wasm_interop::{call_export, export_signature};
use errors::Error;
use interoperable_val::{interoperable_val_to_object, obj_to_interoperable_val};
use jni::objects::{JList, JMap, JObject, JString, JValue};
use jni::sys::{jlong, jobject};
use jni::{self, JNIEnv};
use module::{Module, StoreContents};
use once_cell::sync::{Lazy, OnceCell};
use std::collections::HashMap;
use std::sync::{Arc, Mutex, RwLock};
use utils::error_accumulator;
use wasm_interop::{import_signature, Rust};
use wasmtime::*;
use wasmtime_wasi::WasiCtxBuilder;

static MODULES: Lazy<RwLock<HashMap<i64, Store<StoreContents>>>> =
    Lazy::new(|| RwLock::new(HashMap::new()));

#[macro_export]
macro_rules! get_module {
    ($module_id: expr, $module: ident, $modules: ident) => {
        let mut $modules = crate::MODULES.try_write()?;

        let $module = $modules
            .get_mut(&$module_id)
            .ok_or(crate::errors::Error::ModuleDoesntExist($module_id))?;
    };
}

pub fn load_module(env: JNIEnv, path: JString, j_imports_obj: JObject) -> Result<jlong> {
    let path: String = env.get_string(path)?.into();

    let mut modules = MODULES.try_write()?;

    let module_id = modules.keys().fold(
        i64::MIN,
        |max, current| if max < *current { *current } else { max },
    ) + 1;

    let mut store = Store::new(
        &Engine::default(),
        (
            WasiCtxBuilder::new()
                .inherit_stdio()
                .inherit_args()
                .unwrap()
                .build(),
            Arc::new(Mutex::new(Rust::new())),
            OnceCell::new(),
        ),
    );

    let wasmtime_module = wasmtime::Module::from_file(store.engine(), path)?;
    let mut linker = Linker::new(store.engine());

    for import in wasmtime_module.imports() {
        let name = import.name().unwrap().to_owned();

        match import.ty() {
            ExternType::Func(ty) => {
                // Only let java handle imports if they're supposed to be, avoids messing with wasi imports
                if name.starts_with("__java_") {
                    let jvm = env.get_java_vm()?;

                    let name_closure = name.to_owned();
                    let stripped = name.strip_prefix("__java_").unwrap().to_owned();

                    let func = Func::new(&mut store, ty, move |mut caller, args, results| {
                        match j_call_import(
                            &jvm,
                            &mut caller,
                            args,
                            &name_closure,
                            &stripped,
                            module_id,
                        ) {
                            Ok(data) => {
                                for (i, v) in data.into_iter().enumerate() {
                                    results[i] = v;
                                }

                                Ok(())
                            }
                            Err(e) => Err(Trap::new(e.to_string())),
                        }
                    });

                    linker.define(import.module(), &name, func)?;
                }
            }
            _ => return Err(Error::NotImplemented),
        }
    }

    wasmtime_wasi::add_to_linker(&mut linker, |data: &mut StoreContents| &mut data.0)?;

    let instance = linker.instantiate(&mut store, &wasmtime_module)?;

    let module = Module::new(instance, wasmtime_module, module_id);

    let _ = store.data_mut().2.set(Arc::new(Mutex::new(module)));

    let mutex = Arc::clone(store.data().2.get().unwrap());
    let module = mutex.lock()?;

    let func_type_class = env.find_class("wasmruntime/Types/FuncSignature")?;
    let j_imports = env.get_map(j_imports_obj)?;

    let mut import_names = Vec::new();

    for import in module.module.imports() {
        let name = import.name().unwrap().to_owned();

        match import.ty() {
            ExternType::Func(_) => {
                // Only let java handle imports if they're supposed to be, avoids messing with wasi imports
                if name.starts_with("__java_") {
                    import_names.push(name);
                }
            }
            _ => return Err(Error::NotImplemented),
        }
    }

    drop(module);

    for name in import_names {
        let sig = import_signature(&mut store.as_context_mut(), &name)?;

        let name_without_prefix_closure = name.strip_prefix("__java_").unwrap().to_owned();
        let name_without_prefix = name_without_prefix_closure.clone();

        j_imports.put(
            *env.new_string(name_without_prefix)?,
            env.call_static_method(
                func_type_class,
                "FromList",
                "(Ljava/util/List;)Lwasmruntime/Types/FuncSignature;",
                &[JValue::Object(func_type_to_bytes(env, &sig)?)],
            )?
            .l()?,
        )?;
    }

    modules.insert(module_id, store);

    Ok(module_id)
}

pub fn unload_module(module_id: jlong) -> Result<()> {
    MODULES.try_write()?.remove(&module_id);
    Ok(())
}

pub fn exported_functions(env: JNIEnv, module_id: jlong) -> Result<jobject> {
    get_module!(module_id, store, modules);

    let map_class = env.find_class("java/util/HashMap")?;

    let ret = JMap::from_env(&env, env.new_object(map_class, "()V", &[])?)?;

    let mutex = Arc::clone(store.data().2.get().unwrap());
    let lock = mutex.lock()?;
    let exports = lock.module.exports();

    let mut names = Vec::new();

    for func in exports {
        if let ExternType::Func(_) = func.ty() {
            let name = func.name();

            if !name.starts_with("__") {
                names.push(name.to_owned());
            }
        }
    }

    drop(lock);

    for name in names {
        let sig = export_signature(&mut store.as_context_mut(), &name)?;

        ret.put(*env.new_string(name)?, func_type_to_bytes(env, &sig)?)?;
    }

    Ok(ret.into_inner())
}

pub fn globals(env: JNIEnv, module_id: jlong) -> Result<jobject> {
    get_module!(module_id, store, modules);

    let lock = store.data().2.get().unwrap().lock()?;
    let exports = lock.module.exports();

    let byte_class = env.find_class("java/lang/Byte")?;
    let map_class = env.find_class("java/util/HashMap")?;

    let ret = JMap::from_env(&env, env.new_object(map_class, "()V", &[])?)?;

    for global in exports {
        if let ExternType::Global(v) = global.ty() {
            let to_add = env.new_object(
                byte_class,
                "(B)V",
                &[JValue::Byte(match v.content() {
                    ValType::I32 => 0,
                    ValType::I64 => 1,
                    ValType::F32 => 2,
                    ValType::F64 => 3,
                    ValType::V128 => 4,
                    ValType::ExternRef => 5,
                    ValType::FuncRef => 6,
                })],
            )?;

            ret.put(*env.new_string(global.name())?, to_add)?;
        }
    }

    Ok(ret.into_inner())
}

fn j_call_export(
    env: JNIEnv,
    module_id: jlong,
    function_name: JString,
    args_obj: JObject,
) -> Result<jobject> {
    get_module!(module_id, store, modules);

    let name_string: String = env
        .get_string(function_name)
        .expect("Can't load in path string")
        .into();

    let sig = export_signature(&mut store.as_context_mut(), &name_string)?;

    let args = env
        .get_list(args_obj)?
        .iter()?
        .zip(sig.arguments.iter())
        .map(|(obj, ty)| obj_to_interoperable_val(&env, obj, *ty))
        .fold(Ok(Vec::new()), error_accumulator)?;

    let results = call_export(&mut store.as_context_mut(), &name_string, &args)?;

    let list_class = env.find_class("java/util/ArrayList")?;
    let ret = JList::from_env(&env, env.new_object(list_class, "()V", &[])?)?;

    for val in results.iter() {
        ret.add(interoperable_val_to_object(&env, val)?)?;
    }

    Ok(ret.into_inner())
}

fn get_global(env: JNIEnv, module_id: jlong, global_name: JString) -> Result<jobject> {
    todo!()
    // let module = get_module!(module_id);

    // let name_string: String = env.get_string(global_name)?.into();

    // let global = module
    //     .instance
    //     .get_global(&mut module.store, &name_string)
    //     .ok_or("Global doesn't exist")?;

    // Ok(val_to_obj(&env, &global.get(&mut module.store))?.into_inner())
}
