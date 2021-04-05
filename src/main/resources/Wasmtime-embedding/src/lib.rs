#![allow(non_snake_case)]
#![allow(non_upper_case_globals)]
/*
wasmtime-java was a very helpful reference in writing this
https://github.com/kawamuray/wasmtime-java
*/

mod errors;
mod interop;
use jni::{self, JNIEnv};
use jni::objects::{JClass, JString, JObject, JMap, JList, JValue};
use jni::sys::{jlong, jobject};
use wasmtime::*;
use wasmtime_wasi::Wasi;
use wasi_cap_std_sync::WasiCtxBuilder;
use crate::errors::{Result};
use crate::interop::*;
use std::iter::FromIterator;

static mut StorePtr: i64 = 0;

macro_rules! wrap_error {
  ($env:expr, $body:expr, $default:expr) => {
    match $body {
      Ok(v) => v,
      Err(e) => {
        $env.throw(e).expect("error in throwing exception");
        $default
      }
    }
  };
}

macro_rules! store {
  () => {
    unsafe {
      &*ref_from_raw::<Store>(StorePtr)?
    }
  };
}

#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_Init(env: JNIEnv, class: JClass) {
  wrap_error!(
    env,
    Init(env, class),
    Default::default()
  )
}

#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_LoadModule(env: JNIEnv, class: JClass, path: JString) -> jlong {
  wrap_error!(
    env,
    LoadModule(env, class, path),
    Default::default()
  )
}

#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_UnloadModule(env: JNIEnv, class: JClass, InstancePtr: jlong) {
  wrap_error!(
    env,
    UnloadModule(env, class, InstancePtr),
    Default::default()
  )
}

#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_Functions(env: JNIEnv, class: JClass, InstancePtr: jlong) -> jobject {
  wrap_error!(
    env,
    Functions(env, class, InstancePtr),
    JObject::null().into_inner()
  )
}

#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_Globals(env: JNIEnv, class: JClass, InstancePtr: jlong) -> jobject {
  wrap_error!(
    env,
    Globals(env, class, InstancePtr),
    JObject::null().into_inner()
  )
}

#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_CallFunction(env: JNIEnv, class: JClass, InstancePtr: jlong, str: JString, args: JObject) -> jobject {
  wrap_error!(
    env,
    CallFunction(env, class, InstancePtr, str, args),
    JObject::null().into_inner()
  )
}

#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_GetGlobal(env: JNIEnv, class: JClass, InstancePtr: jlong, str: JString) -> jobject {
  wrap_error!(
    env,
    GetGlobal(env, class, InstancePtr, str),
    JObject::null().into_inner()
  )
}

fn Init(_env: JNIEnv, _class: JClass) -> Result<()> {
  let config = Config::default();
  let Store = Store::new(&Engine::new(&config).expect("There was an error generating a new engine"));

  assert!(Wasi::set_context(
    &Store,
    WasiCtxBuilder::new()
      .inherit_stdio()
      .inherit_args()?
      .build()?
    )
  .is_ok());

  unsafe {
    StorePtr = into_raw::<Store>(Store);
  }
  Ok(())
}

pub fn LoadModule(env: JNIEnv, _class: JClass, path: JString) -> Result<jlong> {
  let path: String = env.get_string(path).expect("Can't load in path string").into();
  let module = Module::from_file(store!().engine(), path)?;

  let mut Linker = Linker::new(store!());
  let wasi = Wasi::new(store!(), WasiCtxBuilder::new().inherit_stdio().build()?);
  wasi.add_to_linker(&mut Linker)?;

  let instance = Linker.instantiate(&module)?;

  Ok(into_raw::<Instance>(instance))
}

pub fn UnloadModule(_env: JNIEnv, _class: JClass, InstancePtr: jlong) -> Result<()> {
  from_raw::<Instance>(InstancePtr)?;
  Ok(())
}

pub fn Functions(env: JNIEnv, _class: JClass, InstancePtr: jlong) -> Result<jobject> {
  let Instance = &*ref_from_raw::<Instance>(InstancePtr)?;
  let exports = Instance::exports(Instance);

  let byteClass = env.find_class("java/lang/Byte")?;
  let mapClass = env.find_class("java/util/HashMap")?;
  let listClass = env.find_class("java/util/ArrayList")?;

  let ret = JMap::from_env(&env, env.new_object(mapClass, "()V", &[])?)?;
  
  for func in exports {
    match func.ty() {
      ExternType::Func(v) => {
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

        ret.put(*env.new_string(func.name())?, *toAdd)?;
      }

      _ => {}
    }
  }

  Ok(ret.into_inner())
}

pub fn Globals(env: JNIEnv, _class: JClass, InstancePtr: jlong) -> Result<jobject> {
  let Instance = &*ref_from_raw::<Instance>(InstancePtr)?;
  let exports = Instance::exports(Instance);

  let byteClass = env.find_class("java/lang/Byte")?;
  let mapClass = env.find_class("java/util/HashMap")?;

  let ret = JMap::from_env(&env, env.new_object(mapClass, "()V", &[])?)?;
  
  for global in exports {
    match global.ty() {
      ExternType::Global(v) => {        
        let toAdd = env.new_object(byteClass, "(B)V", &[JValue::Byte(match v.content() {
          ValType::I32 => 0,
          ValType::I64 => 1,
          ValType::F32 => 2,
          ValType::F64 => 3,
          ValType::V128 => 4,
          ValType::ExternRef => 5,
          ValType::FuncRef => 6
        })])?;

        ret.put(*env.new_string(global.name())?, toAdd)?;
      }
      _ => {}
    }
  }

  Ok(ret.into_inner())
}

fn CallFunction(env: JNIEnv, _class: JClass, InstancePtr: jlong, functionName: JString, argsObj: JObject) -> Result<jobject> {
  let Instance = &*ref_from_raw::<Instance>(InstancePtr)?;
  let nameString: String = env.get_string(functionName).expect("Can't load in path string").into();
  let ToCall = Instance.get_func(&nameString).unwrap();

  let paramsTypes = Vec::from_iter(ToCall.ty().params());

  let args = env.get_list(argsObj)?;

  let mut params = Vec::new();

  for i in 0..paramsTypes.len() {
    params.push(ObjToVal(&env, args.get(i as i32)?.ok_or("Input param must not be null")?, paramsTypes.get(i).ok_or("Input type must not be null")?)?)
  }

  let res = ToCall.call(&params)?;

  let listClass = env.find_class("java/util/ArrayList")?;
  let ret = JList::from_env(&env, env.new_object(listClass, "()V", &[])?)?;

  for val in res.iter() {
    ret.add(ValToObj(&env, val)?)?;
  }

  Ok(ret.into_inner())
}

fn GetGlobal(env: JNIEnv, _class: JClass, InstancePtr: jlong, globalName: JString) -> Result<jobject> {
  let Instance = &*ref_from_raw::<Instance>(InstancePtr)?;
  let nameString: String = env.get_string(globalName).expect("Can't load in path string").into();
  let Global = Instance.get_global(&nameString).ok_or("Global doesn't exist")?;

  Ok(ValToObj(&env, &Global.get())?.into_inner())
}