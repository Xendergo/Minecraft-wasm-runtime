use jni::sys::jobject;
use jni::objects::JList;
use jni::JNIEnv;
use wasmtime::Instance;
use crate::LanguageTrait::LanguageImpl;
use crate::errors::{Result, Error};

pub struct UnknownLang {}

impl LanguageImpl for UnknownLang {
  fn ReadString(&self, _: JNIEnv, _: JList, _: &Instance) -> Result<String> {
    Err(Error::String("Can't read a string when the language is unknown".to_string()))
  }

  fn NewString(&self, _: JNIEnv, _: String, _: &Instance) -> Result<jobject> {
    Err(Error::String("Can't write a string when the language is unknown".to_string()))
  }
}