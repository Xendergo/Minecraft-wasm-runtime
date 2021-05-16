// https://github.com/kawamuray/wasmtime-java/blob/master/wasmtime-jni/src/errors.rs

use anyhow;
use jni::descriptors::Desc;
use jni::objects::JThrowable;
use jni::{self, JNIEnv};
use std::io;
use thiserror::Error;
use wasi_common::StringArrayError;
use wasmtime::{MemoryAccessError, Trap};
use std::str::Utf8Error;
use std::string::{FromUtf16Error, FromUtf8Error};

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
  #[error("")]
  String(String),
  #[error("StringArrayError: {0}")]
  StringArrayError(String),
  #[error("UTF8 error: {0}")]
  UTF8Error(String),
  #[error("UTF16 error: {0}")]
  UTF16Error(String),
  #[error("Memory access error: {0}")]
  MemoryAccessError(String),
  #[error("Wasm trapped: {0}")]
  WasmTrap(String)
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

impl From<Utf8Error> for Error {
  fn from(err: Utf8Error) -> Self {
    Error::UTF8Error(err.to_string())
  }
}

impl From<MemoryAccessError> for Error {
  fn from(err: MemoryAccessError) -> Self {
    Error::MemoryAccessError(err.to_string())
  }
}

impl From<FromUtf16Error> for Error {
  fn from(err: FromUtf16Error) -> Self {
    Error::UTF16Error(err.to_string())
  }
}

impl From<FromUtf8Error> for Error {
  fn from(err: FromUtf8Error) -> Self {
    Error::UTF8Error(err.to_string())
  }
}

impl From<Trap> for Error {
  fn from(err: Trap) -> Self {
    Error::WasmTrap(err.to_string())
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
        "wasmruntime/Exceptions/WasmtimeException",
        e.to_string(),
      ),
      Io(_) | NotImplemented | LockPoison(_) | StringArrayError(_) => {
        ("java/lang/RuntimeException", self.to_string())
      },
      String(e) | UTF8Error(e) | UnknownEnum(e) | UTF16Error(e) | MemoryAccessError(e) | WasmTrap(e) => {
        ("java/lang/RuntimeException", self.to_string() + e)
      }
    };

    let jmsg = env.new_string(msg)?;
    Ok(env
      .new_object(ex_class, "(Ljava/lang/String;)V", &[jmsg.into()])?
      .into())
  }
}
