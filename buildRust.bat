:: I'd suggest getting this to run before debugging using whatever IDE you use (for vs code look up "pre launch task")
cd ./src/main/Wasmtime-embedding
cargo build
XCOPY .\target\debug\Wasmtime_embedding.dll ..\resources /D /Y