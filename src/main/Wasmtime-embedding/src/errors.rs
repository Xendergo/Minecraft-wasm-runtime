// Modified from https://github.com/kawamuray/wasmtime-java/blob/master/wasmtime-jni/src/errors.rs

use jni::descriptors::Desc;
use jni::objects::JThrowable;
use jni::{self, JNIEnv};
use std::io;
use std::str::Utf8Error;
use std::string::{FromUtf16Error, FromUtf8Error};
use std::sync::{Arc, TryLockError};
use thiserror::Error;
use wasi_common::StringArrayError;
use wasmtime::{MemoryAccessError, Trap};

pub type Result<T, E = Error> = std::result::Result<T, E>;

#[derive(Debug, Error, Clone)]
pub enum Error {
    #[error("JNI error: {0}")]
    Jni(Arc<jni::errors::Error>),
    #[error("Wasmtime error: {0}")]
    Wasmtime(Arc<anyhow::Error>),
    #[error("unknown enum variant: {0}")]
    UnknownEnum(String),
    #[error("Not implemented")]
    NotImplemented,
    #[error("Lock poisoned: {0}")]
    LockPoison(String),
    #[error("IO error: {0}")]
    Io(#[from] Arc<io::Error>),
    #[error("StringArrayError: {0}")]
    StringArrayError(String),
    #[error("UTF8 error: {0}")]
    UTF8Error(String),
    #[error("UTF16 error: {0}")]
    UTF16Error(String),
    #[error("Memory access error: {0}")]
    MemoryAccessError(String),
    #[error("Wasm trapped: {0}")]
    WasmTrap(String),
    #[error("Couldn't convert value: {0}")]
    CouldntConvertValue(String),
    #[error("Resource was locked")]
    ResourceLocked,
    #[error("Module doesn't exist: {0}")]
    ModuleDoesntExist(i64),
    #[error("Unknown id for an interoperable type")]
    UnknownInteroperableTypeID,
    #[error("Export doesn't exist: {0}")]
    ExportDoesntExist(String),
    #[error("Incorrect argument, index {0}")]
    IncorrectArgument(usize),
    #[error("Incorrect return value, index {0}")]
    IncorrectReturn(usize),
    #[error("Corrupted module: {0}")]
    CorruptedModule(String),
    #[error("Conversion error: {0}")]
    ConversionError(String),
}

impl From<jni::errors::Error> for Error {
    fn from(err: jni::errors::Error) -> Self {
        Error::Jni(Arc::new(err))
    }
}

impl From<anyhow::Error> for Error {
    fn from(err: anyhow::Error) -> Self {
        Error::Wasmtime(Arc::new(err))
    }
}

impl From<io::Error> for Error {
    fn from(err: io::Error) -> Self {
        Error::Io(Arc::new(err))
    }
}

impl<G> From<std::sync::PoisonError<G>> for Error {
    fn from(err: std::sync::PoisonError<G>) -> Self {
        Error::LockPoison(err.to_string())
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

impl<T> From<TryLockError<T>> for Error {
    fn from(err: TryLockError<T>) -> Self {
        match err {
            TryLockError::Poisoned(e) => Error::from(e),
            TryLockError::WouldBlock => Error::ResourceLocked,
        }
    }
}

impl<'a> Desc<'a, JThrowable<'a>> for Error {
    fn lookup(self, env: &JNIEnv<'a>) -> jni::errors::Result<JThrowable<'a>> {
        use Error::*;
        let (ex_class, msg) = match &self {
            Jni(e) => {
                use jni::errors::Error;
                match &**e {
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

            Wasmtime(_)
            | Io(_)
            | UnknownEnum(_)
            | NotImplemented
            | LockPoison(_)
            | StringArrayError(_)
            | UTF8Error(_)
            | UTF16Error(_)
            | MemoryAccessError(_)
            | WasmTrap(_)
            | CouldntConvertValue(_)
            | ResourceLocked
            | UnknownInteroperableTypeID
            | ModuleDoesntExist(_)
            | ExportDoesntExist(_)
            | IncorrectArgument(_)
            | IncorrectReturn(_)
            | CorruptedModule(_)
            | ConversionError(_) => (
                "wasmruntime/Exceptions/WasmtimeEmbeddingException",
                self.to_string(),
            ),
        };

        let jmsg = env.new_string(msg)?;
        Ok(env
            .new_object(ex_class, "(Ljava/lang/String;)V", &[jmsg.into()])?
            .into())
    }
}
