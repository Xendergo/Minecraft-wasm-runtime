// https://github.com/kawamuray/wasmtime-java/blob/master/wasmtime-jni/src/interop.rs

use crate::errors::Result;
use jni::descriptors::Desc;
use jni::errors::Result as JniResult;
use jni::errors::Error;
use jni::objects::{JFieldID, JObject, JValue, JClass};
use jni::signature::{JavaType, Primitive};
use jni::strings::JNIString;
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

macro_rules! non_null {
    ( $obj:expr, $ctx:expr ) => {
        if $obj.is_null() {
            return Err(jni::errors::Error::NullPtr($ctx).into());
        } else {
            $obj
        }
    };
}

/// A port of `JNIEnv::set_rust_field` with type `T` modified to not require `Send`.
/// It still preserves Mutex around the value, not for atomic access but for making sure
/// the unique owner at the time it is taken.
pub fn set_field<'a, O, S, T>(env: &JNIEnv<'a>, obj: O, field: S, rust_object: T) -> JniResult<()>
where
    O: Into<JObject<'a>>,
    S: AsRef<str>,
    T: 'static,
{
    let obj = obj.into();
    let class = env.auto_local(env.get_object_class(obj)?);
    let field_id: JFieldID = (&class, &field, "J").lookup(env)?;

    let _guard = env.lock_obj(obj)?;

    // Check to see if we've already set this value. If it's not null, that
    // means that we're going to leak memory if it gets overwritten.
    let field_ptr = env
        .get_field_unchecked(obj, field_id, JavaType::Primitive(Primitive::Long))?
        .j()? as *mut Mutex<T>;
    if !field_ptr.is_null() {
        return Err(Error::FieldAlreadySet(format!("field already set: {}", field.as_ref()).into()));
    }

    let ptr = into_raw(rust_object);

    env.set_field_unchecked(obj, field_id, (ptr as jni::sys::jlong).into())
}

/// A port of `JNIEnv::get_rust_field` with type `T` modified to not require `Send`.
pub fn get_field<'a, O, S, T>(env: &JNIEnv<'a>, obj: O, field: S) -> JniResult<MutexGuard<'a, T>>
where
    O: Into<JObject<'a>>,
    S: Into<JNIString>,
    T: 'static,
{
    let obj = obj.into();
    let _guard = env.lock_obj(obj)?;

    let ptr = env.get_field(obj, field, "J")?.j()? as *mut Mutex<T>;
    non_null!(ptr, "rust value from Java");
    unsafe {
        // dereferencing is safe, because we checked it for null
        Ok((*ptr).lock().unwrap())
    }
}

/// A port of `JNIEnv::take_rust_field` with type `T` modified to not require `Send`.
pub fn take_field<'a, O, S, T>(env: &JNIEnv<'a>, obj: O, field: S) -> JniResult<T>
where
    O: Into<JObject<'a>>,
    S: AsRef<str>,
    T: 'static,
{
    let obj = obj.into();
    let class = env.auto_local(env.get_object_class(obj)?);
    let field_id: JFieldID = (&class, &field, "J").lookup(env)?;

    let mbox = {
        let _guard = env.lock_obj(obj)?;

        let ptr = env
            .get_field_unchecked(obj, field_id, JavaType::Primitive(Primitive::Long))?
            .j()? as *mut Mutex<T>;

        non_null!(ptr, "rust value from Java");

        let mbox = unsafe { Box::from_raw(ptr) };

        // attempt to acquire the lock. This prevents us from consuming the
        // mutex if there's an outstanding lock. No one else will be able to
        // get a new one as long as we're in the guarded scope.
        drop(mbox.try_lock()?);

        env.set_field_unchecked(
            obj,
            field_id,
            (::std::ptr::null_mut::<()>() as jni::sys::jlong).into(),
        )?;

        mbox
    };

    Ok(mbox.into_inner().unwrap())
}

// pub fn set_inner<'a, O, S, T>(env: &JNIEnv<'a>, obj: JObject<'a>, rust_object: T) -> JniResult<()>
// where
//     T: 'static,
// {
//     set_field(env, obj, INNER_PTR_FIELD, rust_object)
// }

// pub fn get_inner<'a, T>(env: &JNIEnv<'a>, obj: JObject<'a>) -> JniResult<MutexGuard<'a, T>>
// where
//     T: 'static,
// {
//     get_field(env, obj, INNER_PTR_FIELD)
// }

// pub fn take_inner<'a, T>(env: &JNIEnv<'a>, obj: JObject<'a>) -> JniResult<T>
// where
//     T: 'static,
// {
//     take_field(env, obj, INNER_PTR_FIELD)
// }

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