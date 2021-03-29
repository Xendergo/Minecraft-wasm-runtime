// https://github.com/kawamuray/wasmtime-java/blob/master/wasmtime-jni/src/errors.rs

use anyhow;
use jni::descriptors::Desc;
use jni::objects::JThrowable;
use jni::{self, JNIEnv};
use std::io;
use thiserror::Error;
use wasi_common::StringArrayError;

pub type Result<T, E = Error> = std::result::Result<T, E>;

#[derive(Debug, Error)]
pub enum Error {
  #[error("JNI error: {0}")]
  Jni(#[from] jni::errors::Error),
  #[error("Wasmtime error: {0}")]
  Wasmtime(#[from] anyhow::Error),
  #[error("unknown enum variant: {0}")]
  UnknownEnum(String),
  #[error("not implemented")]
  NotImplemented,
  #[error("{0}")]
  LockPoison(String),
  #[error("IO error: {0}")]
  Io(#[from] io::Error),
  #[error("{0}")]
  String(String),
  #[error("StringArrayError: {0}")]
  StringArrayError(String)
}

impl<G> From<std::sync::PoisonError<G>> for Error {
  fn from(err: std::sync::PoisonError<G>) -> Self {
    Error::LockPoison(err.to_string())
  }
}

impl From<&str> for Error {
  fn from(str: &str) -> Self {
    Error::String(String::from(str))
  }
}

impl From<String> for Error {
  fn from(str: String) -> Self {
    Error::String(str)
  }
}

impl From<StringArrayError> for Error {
  fn from(err: StringArrayError) -> Self {
    Error::StringArrayError(err.to_string())
  }
}

impl<'a> Desc<'a, JThrowable<'a>> for Error {
  fn lookup(self, env: &JNIEnv<'a>) -> jni::errors::Result<JThrowable<'a>> {
    use Error::*;
    let (ex_class, msg) = match &self {
      Jni(e) => {
        use jni::errors::Error;
        match e {
          Error::JavaException => return env.exception_occurred(),
          Error::NullPtr(_) | Error::NullDeref(_) => {
            ("java/lang/NullPointerException", self.to_string())
          }
          _ => (
            "java/lang/RuntimeException",
            format!("unknown exception caught (likely a BUG): {}", self),
          ),
        }
      }
      Wasmtime(e) => (
        "Java/wasmruntime/Exceptions/WasmtimeException",
        e.to_string(),
      ),
      Io(_) | UnknownEnum(_) | NotImplemented | LockPoison(_) | String(_) | StringArrayError(_) => {
        ("java/lang/RuntimeException", self.to_string())
      }
    };

    let jmsg = env.new_string(msg)?;
    Ok(env
      .new_object(ex_class, "(Ljava/lang/String;)V", &[jmsg.into()])?
      .into())
  }
}
