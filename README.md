# Wasm runtime

## Wuts this?
Wasm runtime is a mod that executes webassembly modules within minecraft using wasmtime

## Current supported platforms
Since wasmtime is written in rust, to use it, I have to write a native library in rust, which I have to compile to specific platforms.

* Windows
* Linux

<sub>wasmtime doesn't like cross compiling, and I don't have a mac, so I have to wait for my friend....</sub>

## Stuff to do
### Minecraft API
Idk, I'll just do whatever seems useful here, I'll probably just copy scarpet...
### Other APIs
1. Web requests
2. Terminal commands?
3. File system

## Future API ideas
Spawning items
Creating loot tables to spawn items from (good for those videos that are like "manhunt but thingy drops op loot")
Some sort of equivalent to Javascript's `e.preventDefault()`
Manipulating nbt is definitely a must have

## How to use
Put your .wasm files in the wasm folder in the config folder, then in game you can do /wasm load \<name\> to load the wasm module

To automatically load wasm modules, in your world folder, open `wasm.json` and between the brackets add `"<name>"`, for example:

```json
[
  "moduleName1",
  "moduleName2",
  "moduleName3"
]
```

To run a function you can use `/wasm invoke <moduleName> <functionName> <arguments>`

ps if you see weird comments in the source code blame matgenius04