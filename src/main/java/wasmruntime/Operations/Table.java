package wasmruntime.Operations;

import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;

import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleData.Element;

public class Table {
  public static void get(ValueStack stack) {
    int i = i32(stack);
    wasmruntime.ModuleData.Table t = stack.module.Tables.get(i32(immediates[0]));
    if (t.values.length <= i) throw new TrapRuntime("Index " + i + " is out of bounds for table index " + i32(immediates[0]));
    stack.push(t.values[i]);
  }

  public static void set(ValueStack stack) {
    Value v = stack.pop();
    int i = i32(stack);
    wasmruntime.ModuleData.Table t = stack.module.Tables.get(i32(immediates[0]));
    if (t.values.length <= i) throw new TrapRuntime("Index " + i + " is out of bounds for table index " + i32(immediates[0]));
    t.values[i] = v;
  }

  public static void init(ValueStack stack) {
    int length = i32(stack);
    int offset = i32(stack);
    int start = i32(stack);

    Element elem = stack.module.Elements.get(i32(immediates[0]));
    wasmruntime.ModuleData.Table table = stack.module.Tables.get(i32(immediates[0]));

    if (start + length > elem.data.length) throw new TrapRuntime("Can't intialize more than the length of an element");
    if (offset + length > table.values.length) throw new TrapRuntime("Tried to initialize table past the size of the table");

    table.Initialize(elem, offset, length, start);
  }

  public static void dropElem(ValueStack stack) {
    stack.module.Elements.set(i32(immediates[0]), null);
  }

  public static void copy(ValueStack stack) {
    int length = i32(stack);
    int offset = i32(stack);
    int start = i32(stack);

    wasmruntime.ModuleData.Table table1 = stack.module.Tables.get(i32(immediates[0]));
    wasmruntime.ModuleData.Table table2 = stack.module.Tables.get(i32(immediates[1]));

    if (start + length > table2.values.length) throw new TrapRuntime("Can't copy past the length of the table you're copying from");
    if (offset + length > table1.values.length) throw new TrapRuntime("Tried to copy to a table past the size of the table");

    table1.Copy(table2, offset, length, start);
  }

  public static void grow(ValueStack stack) {
    int growAmt = i32(stack);
    Value fillValue = stack.pop();

    wasmruntime.ModuleData.Table table = stack.module.Tables.get(i32(immediates[0]));
    int len = table.values.length + growAmt;

    if (table.limits.withinLimits(len)) {
      Value[] newValues = new Value[len];
      System.arraycopy(table.values, 0, newValues, 0, table.values.length);
  
      for (int i = table.values.length; i < len; i++) {
        newValues[i] = fillValue;
      }

      stack.push(new ValueI32(table.values.length));

      table.values = newValues;
    } else {
      stack.push(new ValueI32(-1));
    }
  }

  public static void size(ValueStack stack) {
    stack.push(new ValueI32(stack.module.Tables.get(i32(immediates[0])).values.length));
  }

  public static void fill(ValueStack stack) {
    int start = i32(stack);
    Value v = stack.pop();
    int length = i32(stack);

    wasmruntime.ModuleData.Table table = stack.module.Tables.get(i32(immediates[0]));

    int end = start + length;

    if (end > table.values.length) throw new TrapRuntime("Tried to fill outside of the table's size");

    for (int i = start; i < end; i++) {
      table.values[i] = v;
    }
  }
}
