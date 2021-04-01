// https://github.com/kawamuray/wasmtime-java/blob/master/wasmtime-jni/src/interop.rs

use crate::errors::Result;
use jni::objects::{JObject, JValue, JClass};
use jni::sys::{jlong};
use jni::JNIEnv;
use std::sync::Mutex;
use std::sync::MutexGuard;
use wasmtime::{Val, ValType};
use crate::errors;

/// Surrender a Rust object into a pointer.
/// The given value gets "forgotten" by Rust's memory management
/// so you have to get it back into a `T` at some point to avoid leaking memory.
pub fn into_raw<T>(val: T) -> jlong
where
    T: 'static,
{
    Box::into_raw(Box::new(Mutex::new(val))) as jlong
}

/// Restore a Rust object of type `T` from a pointer.
/// This is the reverse operation of `into_raw`.
pub fn from_raw<T>(ptr: jlong) -> Result<T> {
    Ok((*unsafe { Box::from_raw(ptr as *mut Mutex<T>) }).into_inner()?)
}

pub fn ref_from_raw<'a, T>(ptr: jlong) -> Result<MutexGuard<'a, T>> {
    let ptr = ptr as *mut Mutex<T>;
    Ok(unsafe { (*ptr).lock()? })
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

pub fn ValToObj<'a>(env: JNIEnv<'a>, val: &Val, valClass: JClass) -> Result<JObject<'a>> {
    match val {
        Val::I32(v) => Ok(env.call_static_method(valClass, "fromI32", "(I)Lwasmruntime/Types/Value;", &[JValue::Int(*v)])?.l()?),
        Val::I64(v) => Ok(env.call_static_method(valClass, "fromI64", "(J)Lwasmruntime/Types/Value;", &[JValue::Long(*v)])?.l()?),
        Val::F32(v) => Ok(env.call_static_method(valClass, "fromF32", "(F)Lwasmruntime/Types/Value;", &[JValue::Float(f32::from_bits(*v))])?.l()?),
        Val::F64(v) => Ok(env.call_static_method(valClass, "fromF64", "(D)Lwasmruntime/Types/Value;", &[JValue::Double(f64::from_bits(*v))])?.l()?),
        _ => Err(errors::Error::String("Can't convert a non-number type to a java value".to_string()))
    }
}