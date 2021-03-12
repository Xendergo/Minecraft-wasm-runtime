# Wasm runtime

## Wuts this?
Wasm runtime is a mod that executes webassembly modules within minecraft

## Stuff to do
### ✅Parsing modules
### ✅Validating modules
### Executing modules
1. ✅The loop to execute instructions
2. ✅Local variables
3. ✅Entering blocks
4. ✅Branching
5. Grinding out the logic for all the instructions
### Language support for higher order types
1. AssemblyScript
2. Emscripten?
3. Rust?
4. More?
### Minecraft API
Idk, I'll just do whatever seems useful here, I'll probably just copy scarpet
### Other APIs
1. Web requests
2. Terminal commands?
3. File system
### Source maps?
This would be useful for giving line/column numbers when traps are reached
### Wasm proposals
Ima work on implementing as many proposals as I can when I'm finished, if any proposals become spec, Ima implement those immediately

## API ideas
Spawning items
Creating loot tables to spawn items from (good for those videos that are like "manhunt but thingy drops op loot")
Some sort of equivalent to Javascript's `e.preventDefault()`