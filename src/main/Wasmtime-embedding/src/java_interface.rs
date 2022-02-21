// https://github.com/kawamuray/wasmtime-java/blob/master/wasmtime-jni/src/interop.rs

use crate::errors::Result;
use crate::interoperable_val::{interoperable_val_to_object, obj_to_interoperable_val};
use crate::module::StoreContents;
use crate::wasm_interop::{call_import, import_signature, Signature};
use crate::{exported_functions, globals, j_call_export, load_module, unload_module};
use jni::objects::{JClass, JList, JObject, JString, JValue};
use jni::sys::{jlong, jobject};
use jni::{JNIEnv, JavaVM};
use wasmtime::{AsContextMut, Caller, Val};

pub fn j_call_import(
    jvm: &JavaVM,
    store: &mut Caller<StoreContents>,
    args: &[Val],
    name: &str,
    java_name: &str,
    module_id: i64,
) -> Result<Vec<Val>> {
    let sig = import_signature(&mut store.as_context_mut(), name)?;

    let ret_type = sig.returns;

    call_import(
        &mut store.as_context_mut(),
        name,
        Box::new(|vals| {
            let env = jvm.attach_current_thread_permanently()?;

            let val_class = env.find_class("wasmruntime/Types/Value")?;
            let imports_class = env.find_class("wasmruntime/ModuleImports")?;

            let arg_array = env.new_object_array(vals.len() as i32, val_class, JObject::null())?;

            for (i, value) in vals.iter().enumerate() {
                env.set_object_array_element(
                    arg_array,
                    i as i32,
                    interoperable_val_to_object(&env, value)?,
                )?;
            }

            let j_name = JValue::Object(*env.new_string(java_name.to_owned())?);
            let j_arg_array = JValue::Object(JObject::from(arg_array));
            let j_ret: JObject = env
                .call_static_method(
                    imports_class,
                    "callImport",
                    "(Ljava/lang/String;[Lwasmruntime/Types/Value;J)[Lwasmruntime/Types/Value;",
                    &[j_name, j_arg_array, JValue::Long(module_id)],
                )?
                .l()?;

            let mut ret = Vec::new();

            for (i, ty) in ret_type.iter().enumerate() {
                ret.push(obj_to_interoperable_val(
                    &env,
                    env.get_object_array_element(*j_ret, i as i32)?,
                    *ty,
                )?)
            }

            Ok(ret)
        }),
        args,
    )
}

pub fn func_type_to_bytes<'a>(env: JNIEnv<'a>, v: &Signature) -> Result<JObject<'a>> {
    let list_class = env.find_class("java/util/ArrayList")?;
    let byte_class = env.find_class("java/lang/Byte")?;

    let to_add = JList::from_env(&env, env.new_object(list_class, "()V", &[])?)?;

    for byte in v.as_bytes() {
        to_add.add(env.new_object(byte_class, "(B)V", &[JValue::Byte(byte as i8)])?)?;
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
    j_imports: JObject,
) -> jlong {
    wrap_error!(env, load_module(env, path, j_imports), Default::default())
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
        j_call_export(env, module_id, str, args),
        JObject::null().into_inner()
    )
}

// #[allow(unused_variables)]
// #[no_mangle]
// pub extern "system" fn Java_wasmruntime_ModuleWrapper_GetGlobal(
//     env: JNIEnv,
//     class: JClass,
//     module_id: jlong,
//     str: JString,
// ) -> jobject {
//     wrap_error!(
//         env,
//         get_global(env, module_id, str),
//         JObject::null().into_inner()
//     )
// }
