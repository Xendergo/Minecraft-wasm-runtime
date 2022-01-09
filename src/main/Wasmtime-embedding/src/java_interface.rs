// https://github.com/kawamuray/wasmtime-java/blob/master/wasmtime-jni/src/interop.rs

use crate::errors::{Error, Result};
use crate::{
    call_export, errors, exported_functions, get_global, globals, load_module, unload_module,
};
use jni::objects::{JClass, JList, JObject, JString, JValue};
use jni::sys::{jlong, jobject, jobjectArray};
use jni::{JNIEnv, JavaVM};
use wasmtime::{FuncType, Val, ValType};

pub fn obj_to_val_type(env: &JNIEnv, obj: JObject) -> Result<ValType> {
    match env.call_method(obj, "getType", "()I", &[])?.i()? {
        0 => Ok(ValType::I32),
        1 => Ok(ValType::I64),
        2 => Ok(ValType::F32),
        3 => Ok(ValType::F64),
        4 => Ok(ValType::V128),
        5 => Ok(ValType::FuncRef),
        6 => Ok(ValType::ExternRef),
        _ => Err(Error::String(String::from("Unknown type id"))),
    }
}

pub fn obj_to_val(env: &JNIEnv, obj: JObject, val_type: &ValType) -> Result<Val> {
    match val_type {
        ValType::I32 => Ok(Val::from(env.call_method(obj, "i32", "()I", &[])?.i()?)),
        ValType::I64 => Ok(Val::from(env.call_method(obj, "i64", "()J", &[])?.j()?)),
        ValType::F32 => Ok(Val::from(env.call_method(obj, "f32", "()F", &[])?.f()?)),
        ValType::F64 => Ok(Val::from(env.call_method(obj, "f64", "()D", &[])?.d()?)),
        _ => Err(errors::Error::String(
            "Can't convert a non-number java type to a val".to_string(),
        )),
    }
}

pub fn val_to_obj<'a>(env: &JNIEnv<'a>, val: &Val) -> Result<JObject<'a>> {
    let val_class = env.find_class("wasmruntime/Types/Value")?;
    val_to_obj_with_class(env, val, val_class)
}

pub fn val_to_obj_with_class<'a>(
    env: &JNIEnv<'a>,
    val: &Val,
    val_class: JClass,
) -> Result<JObject<'a>> {
    match val {
        Val::I32(v) => Ok(env
            .call_static_method(
                val_class,
                "fromI32",
                "(I)Lwasmruntime/Types/Value;",
                &[JValue::Int(*v)],
            )?
            .l()?),
        Val::I64(v) => Ok(env
            .call_static_method(
                val_class,
                "fromI64",
                "(J)Lwasmruntime/Types/Value;",
                &[JValue::Long(*v)],
            )?
            .l()?),
        Val::F32(v) => Ok(env
            .call_static_method(
                val_class,
                "fromF32",
                "(F)Lwasmruntime/Types/Value;",
                &[JValue::Float(f32::from_bits(*v))],
            )?
            .l()?),
        Val::F64(v) => Ok(env
            .call_static_method(
                val_class,
                "fromF64",
                "(D)Lwasmruntime/Types/Value;",
                &[JValue::Double(f64::from_bits(*v))],
            )?
            .l()?),
        _ => Err(errors::Error::String(
            "Can't convert a non-number type to a java value".to_string(),
        )),
    }
}

pub fn call_import(
    jvm: &JavaVM,
    vals: &[Val],
    name: &str,
    module_name: &str,
    results: &mut [Val],
) -> Result<()> {
    let env = jvm.attach_current_thread_permanently()?;

    let val_class = env.find_class("wasmruntime/Types/Value")?;
    let imports_class = env.find_class("wasmruntime/ModuleImports")?;

    let arg_array = env.new_object_array(vals.len() as i32, val_class, JObject::null())?;

    for (i, value) in vals.iter().enumerate() {
        env.set_object_array_element(
            arg_array,
            i as i32,
            val_to_obj_with_class(&env, value, val_class)?,
        )?;
    }

    let j_name = JValue::Object(*env.new_string(name)?);
    let j_arg_array = JValue::Object(JObject::from(arg_array));
    let j_module_name = JValue::Object(*env.new_string(module_name)?);
    let ret: jobjectArray = env.call_static_method(imports_class, "callImport", "(Ljava/lang/String;[Lwasmruntime/Types/Value;Ljava/lang/String;)[Lwasmruntime/Types/Value;", &[j_name, j_arg_array, j_module_name])?.l()?.into_inner();
    for i in 0..env.get_array_length(ret)? {
        let obj = env.get_object_array_element(ret, i)?;
        results[i as usize] = obj_to_val(&env, obj, &obj_to_val_type(&env, obj)?)?;
    }

    Ok(())
}

pub fn func_type_to_bytes(env: JNIEnv, v: FuncType) -> Result<JObject> {
    let list_class = env.find_class("java/util/ArrayList")?;
    let byte_class = env.find_class("java/lang/Byte")?;

    let to_add = JList::from_env(&env, env.new_object(list_class, "()V", &[])?)?;

    for param in v.params() {
        to_add.add(env.new_object(
            byte_class,
            "(B)V",
            &[JValue::Byte(match param {
                ValType::I32 => 0,
                ValType::I64 => 1,
                ValType::F32 => 2,
                ValType::F64 => 3,
                ValType::V128 => 4,
                ValType::ExternRef => 5,
                ValType::FuncRef => 6,
            })],
        )?)?;
    }

    for result in v.results() {
        to_add.add(env.new_object(
            byte_class,
            "(B)V",
            &[JValue::Byte(match result {
                ValType::I32 => -128,
                ValType::I64 => -127,
                ValType::F32 => -126,
                ValType::F64 => -125,
                ValType::V128 => -124,
                ValType::ExternRef => -123,
                ValType::FuncRef => -122,
            })],
        )?)?;
    }

    Ok(*to_add)
}

macro_rules! wrap_error {
    ($env:expr, $body:expr, $default:expr) => {
        match $body {
            Ok(v) => v,
            Err(e) => {
                let _ = $env.throw(e);
                $default
            }
        }
    };
}

#[allow(unused_variables)]
#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_LoadModule(
    env: JNIEnv,
    class: JClass,
    path: JString,
    module_name: JString,
    j_imports: JObject,
) -> jlong {
    println!("called rust");
    wrap_error!(
        env,
        load_module(env, path, module_name, j_imports),
        Default::default()
    )
}

#[allow(unused_variables)]
#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_UnloadModule(
    env: JNIEnv,
    class: JClass,
    module_id: jlong,
) {
    wrap_error!(env, unload_module(module_id), Default::default())
}

#[allow(unused_variables)]
#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_ExportedFunctions(
    env: JNIEnv,
    class: JClass,
    module_id: jlong,
) -> jobject {
    wrap_error!(
        env,
        exported_functions(env, module_id),
        JObject::null().into_inner()
    )
}

#[allow(unused_variables)]
#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_Globals(
    env: JNIEnv,
    class: JClass,
    module_id: jlong,
) -> jobject {
    wrap_error!(env, globals(env, module_id), JObject::null().into_inner())
}

#[allow(unused_variables)]
#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_CallExport(
    env: JNIEnv,
    class: JClass,
    module_id: jlong,
    str: JString,
    args: JObject,
) -> jobject {
    wrap_error!(
        env,
        call_export(env, module_id, str, args),
        JObject::null().into_inner()
    )
}

#[allow(unused_variables)]
#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_GetGlobal(
    env: JNIEnv,
    class: JClass,
    module_id: jlong,
    str: JString,
) -> jobject {
    wrap_error!(
        env,
        get_global(env, module_id, str),
        JObject::null().into_inner()
    )
}
