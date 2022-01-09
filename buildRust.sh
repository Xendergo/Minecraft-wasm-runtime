#!/bin/bash
FILE=./target/debug/libWasmtime_embedding
FILE_TO=../resources/Wasmtime_embedding

cd ./src/main/Wasmtime-embedding
cargo build
if [ -f "$FILE.so" ]; then
  cp "$FILE.so" "$FILE_TO.so"
elif [ -f "$FILE.dylib" ]; then
  cp "$FILE.dylib" "$FILE_TO.dylib"
elif [ -f "$FILE.dll" ]; then
  cp "$FILE.dll" "$FILE_TO.dll"
else
  echo "$FILE does not exist"
fi
