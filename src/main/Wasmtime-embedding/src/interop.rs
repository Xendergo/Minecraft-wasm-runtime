// https://github.com/kawamuray/wasmtime-java/blob/master/wasmtime-jni/src/interop.rs

use crate::errors::{Result, Error};
use jni::objects::{JObject, JValue, JClass, JList};
use jni::sys::{jlong, jobjectArray};
use jni::{JNIEnv, JavaVM};
use std::sync::RwLock;
use std::sync::RwLockReadGuard;
use wasmtime::{Val, ValType, FuncType};
use crate::errors;

/// Surrender a Rust object into a pointer.
/// The given value gets "forgotten" by Rust's memory management
/// so you have to get it back into a `T` at some point to avoid leaking memory.
pub fn into_raw<T>(val: T) -> jlong
where
    T: 'static,
{
    Box::into_raw(Box::new(RwLock::new(val))) as jlong
}

/// Restore a Rust object of type `T` from a pointer.
/// This is the reverse operation of `into_raw`.
pub fn from_raw<T>(ptr: jlong) -> Result<T> {
    Ok((*unsafe { Box::from_raw(ptr as *mut RwLock<T>) }).into_inner()?)
}

pub fn ref_from_raw<'a, T>(ptr: jlong) -> Result<RwLockReadGuard<'a, T>> {
    let ptr = ptr as *mut RwLock<T>;
    Ok(unsafe { (*ptr).read()? })
}

pub fn ObjToValType(env: &JNIEnv, obj: JObject) -> Result<ValType> {
  match env.call_method(obj, "getType", "()I", &[])?.i()? {
    0 => Ok(ValType::I32),
    1 => Ok(ValType::I64),
    2 => Ok(ValType::F32),
    3 => Ok(ValType::F64),
    4 => Ok(ValType::V128),
    5 => Ok(ValType::FuncRef),
    6 => Ok(ValType::ExternRef),
    _ => Err(Error::String(String::from("Unknown type id")))
  }
}

pub fn ObjToVal(env: &JNIEnv, obj: JObject, valType: &ValType) -> Result<Val> {
  match valType {
    ValType::I32 => Ok(Val::from(env.call_method(obj, "i32", "()I", &[])?.i()?)),
    ValType::I64 => Ok(Val::from(env.call_method(obj, "i64", "()J", &[])?.j()?)),
    ValType::F32 => Ok(Val::from(env.call_method(obj, "f32", "()F", &[])?.f()?)),
    ValType::F64 => Ok(Val::from(env.call_method(obj, "f64", "()D", &[])?.d()?)),
    _ => Err(errors::Error::String("Can't convert a non-number java type to a val".to_string()))
  }
}

pub fn ValToObj<'a>(env: &JNIEnv<'a>, val: &Val) -> Result<JObject<'a>> {
    let valClass = env.find_class("wasmruntime/Types/Value")?;
    ValToObjWithClass(env, val, valClass)
}

pub fn ValToObjWithClass<'a>(env: &JNIEnv<'a>, val: &Val, valClass: JClass) -> Result<JObject<'a>> {
  match val {
    Val::I32(v) => Ok(env.call_static_method(valClass, "fromI32", "(I)Lwasmruntime/Types/Value;", &[JValue::Int(*v)])?.l()?),
    Val::I64(v) => Ok(env.call_static_method(valClass, "fromI64", "(J)Lwasmruntime/Types/Value;", &[JValue::Long(*v)])?.l()?),
    Val::F32(v) => Ok(env.call_static_method(valClass, "fromF32", "(F)Lwasmruntime/Types/Value;", &[JValue::Float(f32::from_bits(*v))])?.l()?),
    Val::F64(v) => Ok(env.call_static_method(valClass, "fromF64", "(D)Lwasmruntime/Types/Value;", &[JValue::Double(f64::from_bits(*v))])?.l()?),
    _ => Err(errors::Error::String("Can't convert a non-number type to a java value".to_string()))
  }
}

pub fn CallImport<'a>(jvm: &JavaVM, vals: &[Val], name: &String, ModuleName: &String, results: &mut [Val]) -> Result<()> {
    let env = jvm.attach_current_thread_permanently()?;

    let valClass = env.find_class("wasmruntime/Types/Value")?;
    let importsClass = env.find_class("wasmruntime/ModuleImports")?;

    let argArray = env.new_object_array(vals.len() as i32, valClass, JObject::null())?;

    for i in 0..vals.len() {
        env.set_object_array_element(argArray, i as i32, ValToObjWithClass(&env, &vals[i], valClass)?)?;
    }

    let jName = JValue::Object(*env.new_string(name)?);
    let jArgArray = JValue::Object(JObject::from(argArray));
    let jModuleName = JValue::Object(*env.new_string(ModuleName)?);
    let ret: jobjectArray = env.call_static_method(importsClass, "callImport", "(Ljava/lang/String;[Lwasmruntime/Types/Value;Ljava/lang/String;)[Lwasmruntime/Types/Value;", &[jName, jArgArray, jModuleName])?.l()?.into_inner();
    for i in 0..env.get_array_length(ret)? {
      let obj = env.get_object_array_element(ret, i)?;
      results[i as usize] = ObjToVal(&env, obj, &ObjToValType(&env, obj)?)?;
    }

    Ok(())
}

pub fn FuncTypeToBytes(env: JNIEnv, v: FuncType) -> Result<JObject> {
  let listClass = env.find_class("java/util/ArrayList")?;
  let byteClass = env.find_class("java/lang/Byte")?;

  let toAdd = JList::from_env(&env, env.new_object(listClass, "()V", &[])?)?;
  
  for param in v.params() {
    toAdd.add(env.new_object(byteClass, "(B)V", &[JValue::Byte(match param {
      ValType::I32 => 0,
      ValType::I64 => 1,
      ValType::F32 => 2,
      ValType::F64 => 3,
      ValType::V128 => 4,
      ValType::ExternRef => 5,
      ValType::FuncRef => 6
    })])?)?;
  }

  for result in v.results() {
    toAdd.add(env.new_object(byteClass, "(B)V", &[JValue::Byte(match result {
      ValType::I32 => -128,
      ValType::I64 => -127,
      ValType::F32 => -126,
      ValType::F64 => -125,
      ValType::V128 => -124,
      ValType::ExternRef => -123,
      ValType::FuncRef => -122
    })])?)?;
  }

  Ok(*toAdd)
}