package wasmruntime.ModuleData;

import java.util.HashMap;

import wasmruntime.Errors.Trap;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleData.HelpfulEnums.GenericTypeRequirers;
import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.ExecExpression;
import wasmruntime.ModuleExecutor.InstructionType;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueF32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.ModuleExecutor.ValueF64;
import wasmruntime.ModuleExecutor.ValueStack;

public class Opcodes {
  public static Value[] immediates;
  
  static FunctionType nop = new FunctionType();
  static FunctionType drop = new FunctionType(new WasmType[] {WasmType.any}, new WasmType[0]);
  static FunctionType get = new FunctionType(WasmType.T);
  static FunctionType set = new FunctionType(new WasmType[] {WasmType.T}, new WasmType[0]);
  static FunctionType tee = new FunctionType(new WasmType[] {WasmType.T}, new WasmType[] {WasmType.T});

  static FunctionType i32Toi32 = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.i32});
  static FunctionType i64Toi32 = new FunctionType(new WasmType[] {WasmType.i64}, new WasmType[] {WasmType.i32});
  static FunctionType f32Toi32 = new FunctionType(new WasmType[] {WasmType.f32}, new WasmType[] {WasmType.i32});
  static FunctionType f64Toi32 = new FunctionType(new WasmType[] {WasmType.f64}, new WasmType[] {WasmType.i32});
  static FunctionType i32Toi64 = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.i64});
  static FunctionType i64Toi64 = new FunctionType(new WasmType[] {WasmType.i64}, new WasmType[] {WasmType.i64});
  static FunctionType f32Toi64 = new FunctionType(new WasmType[] {WasmType.f32}, new WasmType[] {WasmType.i64});
  static FunctionType f64Toi64 = new FunctionType(new WasmType[] {WasmType.f64}, new WasmType[] {WasmType.i64});
  static FunctionType i32Tof32 = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.f32});
  static FunctionType i64Tof32 = new FunctionType(new WasmType[] {WasmType.i64}, new WasmType[] {WasmType.f32});
  static FunctionType f32Tof32 = new FunctionType(new WasmType[] {WasmType.f32}, new WasmType[] {WasmType.f32});
  static FunctionType f64Tof32 = new FunctionType(new WasmType[] {WasmType.f64}, new WasmType[] {WasmType.f32});
  static FunctionType i32Tof64 = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.f64});
  static FunctionType i64Tof64 = new FunctionType(new WasmType[] {WasmType.i64}, new WasmType[] {WasmType.f64});
  static FunctionType f32Tof64 = new FunctionType(new WasmType[] {WasmType.f32}, new WasmType[] {WasmType.f64});
  static FunctionType f64Tof64 = new FunctionType(new WasmType[] {WasmType.f64}, new WasmType[] {WasmType.f64});

  static FunctionType storei32 = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.i32});
  static FunctionType storei64 = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.i64});
  static FunctionType storef32 = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.f32});
  static FunctionType storef64 = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.f64});

  static FunctionType consti32 = new FunctionType(WasmType.i32);
  static FunctionType consti64 = new FunctionType(WasmType.i64);
  static FunctionType constf32 = new FunctionType(WasmType.f32);
  static FunctionType constf64 = new FunctionType(WasmType.f64);

  static FunctionType comparei32 = new FunctionType(new WasmType[] {WasmType.i32, WasmType.i32}, new WasmType[] {WasmType.i32});
  static FunctionType comparei64 = new FunctionType(new WasmType[] {WasmType.i64, WasmType.i64}, new WasmType[] {WasmType.i32});
  static FunctionType comparef32 = new FunctionType(new WasmType[] {WasmType.f32, WasmType.f32}, new WasmType[] {WasmType.i32});
  static FunctionType comparef64 = new FunctionType(new WasmType[] {WasmType.f64, WasmType.f64}, new WasmType[] {WasmType.i32});

  static FunctionType operatori64 = new FunctionType(new WasmType[] {WasmType.i64, WasmType.i64}, new WasmType[] {WasmType.i64});
  static FunctionType operatorf32 = new FunctionType(new WasmType[] {WasmType.f32, WasmType.f32}, new WasmType[] {WasmType.f32});
  static FunctionType operatorf64 = new FunctionType(new WasmType[] {WasmType.f64, WasmType.f64}, new WasmType[] {WasmType.f64});

  static FunctionType select = new FunctionType(new WasmType[] {WasmType.T, WasmType.T, WasmType.i32}, new WasmType[] {WasmType.T});

  public static HashMap<Byte, InstructionType> opcodeMap = new HashMap<Byte, InstructionType>();
  public static HashMap<Byte, InstructionType> truncMap = new HashMap<Byte, InstructionType>();

  static {
    opcodeMap.put((byte) 0x00, new InstructionType(Opcodes::temp, nop, new WasmType[0]));
    opcodeMap.put((byte) 0x01, new InstructionType(Opcodes::temp, nop, new WasmType[0]));
    opcodeMap.put((byte) 0x1A, new InstructionType(Opcodes::temp, set, new WasmType[0]));
    opcodeMap.put((byte) 0x1B, new InstructionType(Opcodes::temp, select, new WasmType[0], GenericTypeRequirers.select));
    opcodeMap.put((byte) 0x20, new InstructionType(Opcodes::temp, get, new WasmType[] {WasmType.i32}, GenericTypeRequirers.local));
    opcodeMap.put((byte) 0x21, new InstructionType(Opcodes::temp, set, new WasmType[] {WasmType.i32}, GenericTypeRequirers.local));
    opcodeMap.put((byte) 0x22, new InstructionType(Opcodes::temp, tee, new WasmType[] {WasmType.i32}, GenericTypeRequirers.local));
    opcodeMap.put((byte) 0x23, new InstructionType(Opcodes::temp, get, new WasmType[] {WasmType.i32}, GenericTypeRequirers.global));
    opcodeMap.put((byte) 0x24, new InstructionType(Opcodes::temp, set, new WasmType[] {WasmType.i32}, GenericTypeRequirers.global));
    opcodeMap.put((byte) 0x28, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x29, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2a, new InstructionType(Opcodes::temp, i32Tof32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2b, new InstructionType(Opcodes::temp, i32Tof64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2c, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2d, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2e, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2f, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x30, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x31, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x32, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x33, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x34, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x35, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x36, new InstructionType(Opcodes::temp, storei32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x37, new InstructionType(Opcodes::temp, storei64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x38, new InstructionType(Opcodes::temp, storef32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x39, new InstructionType(Opcodes::temp, storef64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3a, new InstructionType(Opcodes::temp, storei32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3b, new InstructionType(Opcodes::temp, storei32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3c, new InstructionType(Opcodes::temp, storei64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3d, new InstructionType(Opcodes::temp, storei64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3e, new InstructionType(Opcodes::temp, storei64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3f, new InstructionType(Opcodes::temp, consti32, new WasmType[0]));
    opcodeMap.put((byte) 0x40, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x41, new InstructionType(Opcodes::temp, consti32, new WasmType[] {WasmType.i32}));
    opcodeMap.put((byte) 0x42, new InstructionType(Opcodes::temp, consti64, new WasmType[] {WasmType.i64}));
    opcodeMap.put((byte) 0x43, new InstructionType(Opcodes::temp, constf32, new WasmType[] {WasmType.f32}));
    opcodeMap.put((byte) 0x44, new InstructionType(Opcodes::temp, constf64, new WasmType[] {WasmType.f64}));
    opcodeMap.put((byte) 0x45, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x46, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x47, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x48, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x49, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4a, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4b, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4c, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4d, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4e, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4f, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x50, new InstructionType(Opcodes::temp, i64Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x51, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x52, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x53, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x54, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x55, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x56, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x57, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x58, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x59, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x5a, new InstructionType(Opcodes::temp, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x5b, new InstructionType(Opcodes::temp, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x5c, new InstructionType(Opcodes::temp, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x5d, new InstructionType(Opcodes::temp, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x5e, new InstructionType(Opcodes::temp, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x5f, new InstructionType(Opcodes::temp, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x60, new InstructionType(Opcodes::temp, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x61, new InstructionType(Opcodes::temp, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x62, new InstructionType(Opcodes::temp, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x63, new InstructionType(Opcodes::temp, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x64, new InstructionType(Opcodes::temp, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x65, new InstructionType(Opcodes::temp, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x66, new InstructionType(Opcodes::temp, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x67, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x68, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x69, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x6a, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6b, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6c, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6d, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6e, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6f, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x70, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x71, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x72, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x73, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x74, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x75, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x76, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x77, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x78, new InstructionType(Opcodes::temp, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x79, new InstructionType(Opcodes::temp, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0x7a, new InstructionType(Opcodes::temp, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0x7b, new InstructionType(Opcodes::temp, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0x7c, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x7d, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x7e, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x7f, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x80, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x81, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x82, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x83, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x84, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x85, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x86, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x87, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x88, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x89, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x8a, new InstructionType(Opcodes::temp, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x8b, new InstructionType(Opcodes::temp, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x8c, new InstructionType(Opcodes::temp, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x8d, new InstructionType(Opcodes::temp, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x8e, new InstructionType(Opcodes::temp, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x8f, new InstructionType(Opcodes::temp, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x90, new InstructionType(Opcodes::temp, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x91, new InstructionType(Opcodes::temp, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x92, new InstructionType(Opcodes::temp, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x93, new InstructionType(Opcodes::temp, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x94, new InstructionType(Opcodes::temp, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x95, new InstructionType(Opcodes::temp, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x96, new InstructionType(Opcodes::temp, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x97, new InstructionType(Opcodes::temp, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x98, new InstructionType(Opcodes::temp, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x99, new InstructionType(Opcodes::temp, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9a, new InstructionType(Opcodes::temp, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9b, new InstructionType(Opcodes::temp, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9c, new InstructionType(Opcodes::temp, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9d, new InstructionType(Opcodes::temp, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9e, new InstructionType(Opcodes::temp, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9f, new InstructionType(Opcodes::temp, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0xa0, new InstructionType(Opcodes::temp, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa1, new InstructionType(Opcodes::temp, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa2, new InstructionType(Opcodes::temp, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa3, new InstructionType(Opcodes::temp, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa4, new InstructionType(Opcodes::temp, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa5, new InstructionType(Opcodes::temp, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa6, new InstructionType(Opcodes::temp, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa7, new InstructionType(Opcodes::temp, i64Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xa8, new InstructionType(Opcodes::temp, f32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xa9, new InstructionType(Opcodes::temp, f32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xaa, new InstructionType(Opcodes::temp, f64Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xab, new InstructionType(Opcodes::temp, f64Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xac, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xad, new InstructionType(Opcodes::temp, i32Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xae, new InstructionType(Opcodes::temp, f32Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xaf, new InstructionType(Opcodes::temp, f32Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xb0, new InstructionType(Opcodes::temp, f64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xb1, new InstructionType(Opcodes::temp, f64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xb2, new InstructionType(Opcodes::temp, i32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0xb3, new InstructionType(Opcodes::temp, i32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0xb4, new InstructionType(Opcodes::temp, i64Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0xb5, new InstructionType(Opcodes::temp, i64Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0xb6, new InstructionType(Opcodes::temp, f64Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0xb7, new InstructionType(Opcodes::temp, i32Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0xb8, new InstructionType(Opcodes::temp, i32Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0xb9, new InstructionType(Opcodes::temp, i64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0xba, new InstructionType(Opcodes::temp, i64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0xbb, new InstructionType(Opcodes::temp, f32Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0xbc, new InstructionType(Opcodes::temp, f32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xbd, new InstructionType(Opcodes::temp, f64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xbe, new InstructionType(Opcodes::temp, i32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0xbf, new InstructionType(Opcodes::temp, i64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0xc0, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xc1, new InstructionType(Opcodes::temp, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xc2, new InstructionType(Opcodes::temp, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xc3, new InstructionType(Opcodes::temp, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xc4, new InstructionType(Opcodes::temp, i64Toi64, new WasmType[0]));

    truncMap.put((byte) 0, new InstructionType(Opcodes::temp, f32Toi32, new WasmType[0]));
    truncMap.put((byte) 1, new InstructionType(Opcodes::temp, f32Toi32, new WasmType[0]));
    truncMap.put((byte) 2, new InstructionType(Opcodes::temp, f64Toi32, new WasmType[0]));
    truncMap.put((byte) 3, new InstructionType(Opcodes::temp, f64Toi32, new WasmType[0]));
    truncMap.put((byte) 4, new InstructionType(Opcodes::temp, f32Toi64, new WasmType[0]));
    truncMap.put((byte) 5, new InstructionType(Opcodes::temp, f32Toi64, new WasmType[0]));
    truncMap.put((byte) 6, new InstructionType(Opcodes::temp, f64Toi64, new WasmType[0]));
    truncMap.put((byte) 7, new InstructionType(Opcodes::temp, f64Toi64, new WasmType[0]));
  }

  public static int i32(Value v) {
    return ((ValueI32)v).value;
  }

  public static float f32(Value v) {
    return ((ValueF32)v).value;
  }

  public static long i64(Value v) {
    return ((ValueI64)v).value;
  }

  public static double f64(Value v) {
    return ((ValueF64)v).value;
  }

  private static void temp(ValueStack stack) {

  }

  public static void branch(ValueStack stack) {
    ExecExpression.branchDepth = i32(immediates[0]);
  }

  public static void branchIf(ValueStack stack) {
    if (i32(stack.pop()) == 0) {
      ExecExpression.branchDepth = i32(immediates[0]);
    }
  }

  public static void branchTable(ValueStack stack) {
    int index = i32(immediates[i32(stack.pop())]);
    if (immediates.length <= index) {
      ExecExpression.branchDepth = i32(immediates[immediates.length - 1]);
    } else {
      ExecExpression.branchDepth = i32(immediates[index]);
    }
  }

  public static void call(ValueStack stack) {
    try {
      stack.module.Functions.get(i32(immediates[0])).Exec(stack);
    } catch (Trap trap) {
      throw new TrapRuntime(trap.getMessage());
    }
  }

  public static void callIndirect(ValueStack stack) {
    WasmFunctionInterface func = stack.module.Functions.get(stack.module.Tables.get(0).values.get(i32(stack.pop())));
    if (!func.type.equals(stack.module.TypeSection.get(i32(immediates[0])))) throw new TrapRuntime("call_indirect tried to call a function with the wrong type, are you passing an incorrect callback?");
    try {
      func.Exec(stack);
    } catch (Trap trap) {
      throw new TrapRuntime(trap.getMessage());
    }
  }
}
