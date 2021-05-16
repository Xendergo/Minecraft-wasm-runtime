#!/bin/bash
FILE=./target/debug/libWasmtime_embedding

cd ./src/main/Wasmtime-embedding
cargo build
if [ -f "$FILE.so" ]; then
  cp -i "$FILE.so" ../resources
elif [ -f "$FILE.dylib" ]; then
  cp -i "$FILE.dylib" ../resources
elif [ -f "$FILE.dll" ]; then
  cp -i "$FILE.dll" ../resources
else
  echo "$FILE does not exist"
fi
