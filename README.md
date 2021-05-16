# Wasm runtime

## Wuts this?
Wasm runtime is a mod that executes webassembly modules within minecraft using wasmtime

## Current supported platforms
Since wasmtime is written in rust, to use it, I have to write a native library in rust, which I have to compile to specific platforms.

* Windows
* Mac
* Linux

<sub>Mac build is for x86, not arm</sub>

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
You can find documentation in the [wiki section](https://github.com/Xendergo/Minecraft-wasm-runtime/wiki)

ps if you see weird comments in the source code blame matgenius04