/*
wasmtime-java was a very helpful reference in writing this
https://github.com/kawamuray/wasmtime-java
*/

mod errors;
mod java_interface;
mod module;

use crate::errors::Result;
use crate::java_interface::*;
use errors::Error;
use jni::objects::{JList, JMap, JObject, JString, JValue};
use jni::sys::{jlong, jobject};
use jni::{self, JNIEnv};
use module::Module;
use once_cell::sync::Lazy;
use std::collections::HashMap;
use std::iter::FromIterator;
use std::sync::RwLock;
use wasmtime::*;
use wasmtime_wasi::WasiCtxBuilder;

static MODULES: Lazy<RwLock<HashMap<i64, Module>>> = Lazy::new(|| RwLock::new(HashMap::new()));

pub fn load_module(
    env: JNIEnv,
    path: JString,
    module_name: JString,
    j_imports_obj: JObject,
) -> Result<jlong> {
    let path: String = env.get_string(path)?.into();

    let mut store = Store::new(
        &Engine::default(),
        WasiCtxBuilder::new()
            .inherit_stdio()
            .inherit_args()
            .unwrap()
            .build(),
    );

    let module = wasmtime::Module::from_file(store.engine(), path)?;
    let mut linker = Linker::new(store.engine());

    let imports = module.imports();

    let func_type_class = env.find_class("wasmruntime/Types/FuncType")?;
    let j_imports = env.get_map(j_imports_obj)?;

    for import in imports {
        let jvm = env.get_java_vm()?;
        let name = String::from(import.name().unwrap());

        // Only do imports dynamically for functions that are supposed to be
        if name.starts_with("__dynamic_") {
            let module_name_rs: String = env.get_string(module_name)?.into();

            match import.ty() {
                ExternType::Func(ty) => {
                    let func =
                        Func::new(
                            &mut store,
                            ty,
                            move |_caller, params, results| match call_import(
                                &jvm,
                                params,
                                &name,
                                &module_name_rs,
                                results,
                            ) {
                                Ok(_) => Ok(()),
                                Err(e) => Err(Trap::new(e.to_string())),
                            },
                        );

                    linker.define(import.module(), import.name().unwrap(), Extern::Func(func))?;

                    j_imports.put(
                        *env.new_string(String::from(import.name().unwrap()))?,
                        env.call_static_method(
                            func_type_class,
                            "FromList",
                            "(Ljava/util/List;)Lwasmruntime/Types/FuncType;",
                            &[JValue::Object(func_type_to_bytes(
                                env,
                                import.ty().unwrap_func().clone(),
                            )?)],
                        )?
                        .l()?,
                    )?;
                }

                _ => return Err(Error::NotImplemented),
            }
        }
    }

    wasmtime_wasi::add_to_linker(&mut linker, |wasi| wasi)?;

    let instance = linker.instantiate(&mut store, &module)?;

    let mut modules = MODULES.try_write()?;

    let key = modules.keys().fold(
        i64::MIN,
        |max, current| if max < *current { *current } else { max },
    ) + 1;

    modules.insert(key, Module::new(instance, module, store));

    Ok(key)
}

pub fn unload_module(module_id: jlong) -> Result<()> {
    MODULES.try_write()?.remove(&module_id);
    Ok(())
}

pub fn exported_functions(env: JNIEnv, module_id: jlong) -> Result<jobject> {
    let modules = MODULES.try_read()?;

    let module = &modules
        .get(&module_id)
        .ok_or(Error::ModuleDoesntExist)?
        .module;

    let map_class = env.find_class("java/util/HashMap")?;

    let ret = JMap::from_env(&env, env.new_object(map_class, "()V", &[])?)?;

    let exports = module.exports();

    for func in exports {
        if let ExternType::Func(v) = func.ty() {
            ret.put(*env.new_string(func.name())?, func_type_to_bytes(env, v)?)?;
        }
    }

    Ok(ret.into_inner())
}

pub fn globals(env: JNIEnv, module_id: jlong) -> Result<jobject> {
    let modules = MODULES.try_read()?;

    let module = &modules
        .get(&module_id)
        .ok_or(Error::ModuleDoesntExist)?
        .module;

    let exports = module.exports();

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

fn call_export(
    env: JNIEnv,
    module_id: jlong,
    function_name: JString,
    args_obj: JObject,
) -> Result<jobject> {
    let mut modules = MODULES.try_write()?;

    let module = modules
        .get_mut(&module_id)
        .ok_or(Error::ModuleDoesntExist)?;

    let name_string: String = env
        .get_string(function_name)
        .expect("Can't load in path string")
        .into();

    let to_call = module
        .instance
        .get_func(&mut module.store, &name_string)
        .unwrap();

    let ty = to_call.ty(&module.store);

    let params_types = Vec::from_iter(ty.params());

    let args = env.get_list(args_obj)?;

    let mut params = Vec::new();

    for i in 0..params_types.len() {
        params.push(obj_to_val(
            &env,
            args.get(i as i32)?.ok_or("Input param must not be null")?,
            params_types.get(i).ok_or("Input type must not be null")?,
        )?)
    }

    let mut results = Vec::new();

    for _ in ty.results() {
        results.push(Val::I32(0))
    }

    to_call.call(&mut module.store, &params, &mut results)?;

    let list_class = env.find_class("java/util/ArrayList")?;
    let ret = JList::from_env(&env, env.new_object(list_class, "()V", &[])?)?;

    for val in results.iter() {
        ret.add(val_to_obj(&env, val)?)?;
    }

    Ok(ret.into_inner())
}

fn get_global(env: JNIEnv, module_id: jlong, global_name: JString) -> Result<jobject> {
    let mut modules = MODULES.try_write()?;

    let module = modules
        .get_mut(&module_id)
        .ok_or(Error::ModuleDoesntExist)?;

    let name_string: String = env.get_string(global_name)?.into();

    let global = module
        .instance
        .get_global(&mut module.store, &name_string)
        .ok_or("Global doesn't exist")?;

    Ok(val_to_obj(&env, &global.get(&mut module.store))?.into_inner())
}
