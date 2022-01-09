use wasi_common::WasiCtx;
use wasmtime::{Instance, Store};

pub struct Module {
    pub instance: Instance,
    pub module: wasmtime::Module,
    pub store: Store<WasiCtx>,
}

impl Module {
    pub fn new(instance: Instance, module: wasmtime::Module, store: Store<WasiCtx>) -> Module {
        Module {
            instance,
            module,
            store,
        }
    }
}
