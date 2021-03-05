package wasmruntime.ModuleExecutor;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import wasmruntime.ModuleData.Module;

public class ValueStack {
  public int stackPtr = 0;
  public Value[] stack; // Anything on the stack on or after the stack pointer is garbage that can be overwritten
  public Module module;

  public ValueStack(int size, Module moduleOof) {
    stack = new Value[size];
    module = moduleOof;
  }

  public void push(Value v) {
    stack[stackPtr] = v;
    stackPtr++;
  }

  public Value pop() {
    stackPtr--;
    return stack[stackPtr];
  }

  public Value peek() {
    return stack[stackPtr - 1];
  }

  public Value[] toArray() {
    return ArrayUtils.subarray(stack, 0, stackPtr);
  }

  @Override
  public String toString() {
    return "ValueStack [" + StringUtils.join(toArray(), ", ") + "]";
  }

  public String displayString() {
    return StringUtils.join(toArray(), ", ");
  }
}
