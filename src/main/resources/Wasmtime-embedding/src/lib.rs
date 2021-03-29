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
pub extern "system" fn Java_wasmruntime_ModuleWrapper_LoadModule(env: JNIEnv, class: JClass, path: JString, name: JString) -> jlong {
  wrap_error!(
    env,
    LoadModule(env, class, path, name),
    Default::default()
  )
}

#[no_mangle]
pub extern "system" fn Java_wasmruntime_ModuleWrapper_UnloadModule(env: JNIEnv, class: JClass) {
  wrap_error!(
    env,
    UnloadModule(env, class),
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

fn Init(env: JNIEnv, class: JClass) -> Result<()> {
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

pub fn LoadModule(env: JNIEnv, class: JClass, path: JString, name: JString) -> Result<jlong> {
  let path: String = env.get_string(path).expect("Can't load in path string").into();
  let module = Module::from_file(store!().engine(), path)?;

  let mut Linker = Linker::new(store!());
  let wasi = Wasi::new(store!(), WasiCtxBuilder::new().inherit_stdio().build()?);
  wasi.add_to_linker(&mut Linker)?;

  let instance = Linker.instantiate(&module)?;

  Ok(into_raw::<Instance>(instance))
}

pub fn UnloadModule(env: JNIEnv, class: JClass) -> Result<()> {
  interop::get_field::<JClass, &str, Instance>(&env, class, "InstanceID")?;
  Ok(())
}

pub fn Functions(env: JNIEnv, class: JClass, InstancePtr: jlong) -> Result<jobject> {
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