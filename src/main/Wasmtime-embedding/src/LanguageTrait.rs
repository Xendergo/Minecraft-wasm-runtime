use jni::sys::jobject;
use jni::JNIEnv;
use jni::objects::JList;
use wasmtime::Instance;
use crate::errors::Result;

pub trait LanguageImpl {
  fn ReadString(&self, env: JNIEnv, ptr: JList, Instance: &Instance) -> Result<String>;
  fn NewString(&self, env: JNIEnv, str: String, Instance: &Instance) -> Result<jobject>;
}