#!/bin/bash
cd ./src/main/Wasmtime-embedding
cargo build
cp -i ./target/debug/libWasmtime_embedding.dylib ../resources