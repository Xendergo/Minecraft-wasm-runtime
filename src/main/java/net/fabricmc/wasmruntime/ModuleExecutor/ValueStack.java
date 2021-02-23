package net.fabricmc.wasmruntime.ModuleExecutor;

public class ValueStack {
  public int stackPtr = 0;
  public Value[] stack; // Anything on the stack on or after the stack pointer is garbage that can be overwritten

  public ValueStack(int size) {
    stack = new Value[size];
  }

  public void push(Value v) {
    stack[stackPtr] = v;
    stackPtr++;
  }

  public Value pop() {
    stackPtr--;
    return stack[stackPtr];
  }
}
