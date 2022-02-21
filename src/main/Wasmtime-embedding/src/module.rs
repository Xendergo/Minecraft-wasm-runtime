use std::sync::{Arc, Mutex};

use once_cell::sync::OnceCell;
use wasi_common::WasiCtx;
use wasmtime::Instance;

use crate::wasm_interop::Rust;

pub type StoreContents = (WasiCtx, Arc<Mutex<Rust>>, OnceCell<Arc<Mutex<Module>>>);

pub struct Module {
    pub instance: Instance,
    pub module: wasmtime::Module,
    pub module_id: i64,
}

impl Module {
    pub fn new(instance: Instance, module: wasmtime::Module, module_id: i64) -> Module {
        Module {
            instance,
            module,
            module_id,
        }
    }
}
