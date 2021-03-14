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

  public ValueStack(int size, Module moduleOof, Value[] initial) {
    stack = new Value[size];
    System.arraycopy(initial, 0, stack, 0, initial.length);
    stackPtr = initial.length;
    module = moduleOof;
  }

  public void pushStack(ValueStack stackToPush) {
    for (Value i : stackToPush.toArray()) {
      push(i);
    }
  }

  public void pushStack(Value[] stackToPush) {
    for (Value i : stackToPush) {
      push(i);
    }
  }

  public void push(Value v) {
    stack[stackPtr] = v;
    stackPtr++;
  }

  public void push(int v) {
    stack[stackPtr] = new ValueI32(v);
    stackPtr++;
  }

  public void push(long v) {
    stack[stackPtr] = new ValueI64(v);
    stackPtr++;
  }

  public void push(float v) {
    stack[stackPtr] = new ValueF32(v);
    stackPtr++;
  }

  public void push(double v) {
    stack[stackPtr] = new ValueF64(v);
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
    Value[] a = ArrayUtils.subarray(stack, 0, stackPtr);
    ArrayUtils.reverse(a);
    return a;
  }

  @Override
  public String toString() {
    return "ValueStack [" + StringUtils.join(toArray(), ", ") + "]";
  }

  public String displayString() {
    return StringUtils.join(toArray(), ", ");
  }
}
