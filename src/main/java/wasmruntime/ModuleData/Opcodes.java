package wasmruntime.ModuleData;

import java.util.HashMap;

import wasmruntime.ModuleData.HelpfulEnums.GenericTypeRequirers;
import wasmruntime.ModuleData.HelpfulEnums.WasmType;
import wasmruntime.ModuleExecutor.InstructionType;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueExternref;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueF32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.ModuleExecutor.ValueF64;
import wasmruntime.ModuleExecutor.ValueFuncref;
import wasmruntime.ModuleExecutor.ValueStack;
import wasmruntime.Operations.Math.Add;
import wasmruntime.Operations.Math.Divide;
import wasmruntime.Operations.Basic;
import wasmruntime.Operations.Const;
import wasmruntime.Operations.Globals;
import wasmruntime.Operations.Math.Multiply;
import wasmruntime.Operations.Reference;
import wasmruntime.Operations.Math.Remainder;
import wasmruntime.Operations.Math.Subtract;
import wasmruntime.Operations.Math.FloatingPoint.MinMax;
import wasmruntime.Operations.Math.FloatingPoint.Rounding;
import wasmruntime.Operations.Math.FloatingPoint.abs;
import wasmruntime.Operations.Math.FloatingPoint.copysign;
import wasmruntime.Operations.Math.FloatingPoint.neg;
import wasmruntime.Operations.Math.FloatingPoint.sqrt;
import wasmruntime.Operations.Table;
import wasmruntime.Operations.Bitwise.Gates;
import wasmruntime.Operations.Bitwise.Rotate;
import wasmruntime.Operations.Bitwise.Shift;
import wasmruntime.Operations.Bitwise.clz;
import wasmruntime.Operations.Bitwise.ctz;
import wasmruntime.Operations.Bitwise.popcnt;
import wasmruntime.Operations.Compare.eq;
import wasmruntime.Operations.Compare.eqz;
import wasmruntime.Operations.Compare.ge;
import wasmruntime.Operations.Compare.gt;
import wasmruntime.Operations.Compare.le;
import wasmruntime.Operations.Compare.lt;
import wasmruntime.Operations.Compare.ne;
import wasmruntime.Operations.Conversions.Extend;
import wasmruntime.Operations.Conversions.Trunc;
import wasmruntime.Operations.Conversions.wrap;
import wasmruntime.Operations.Memory.Load;
import wasmruntime.Operations.Memory.MemoryManagement;
import wasmruntime.Operations.Memory.Store;

// Imports for operations (good for copy/paste)

/*
import wasmruntime.ModuleExecutor.ValueF32;
import wasmruntime.ModuleExecutor.ValueF64;
import wasmruntime.ModuleExecutor.ValueI32;
import wasmruntime.ModuleExecutor.ValueI64;
import wasmruntime.ModuleExecutor.ValueFuncref;
import wasmruntime.Errors.TrapRuntime;
import wasmruntime.ModuleExecutor.Value;
import wasmruntime.ModuleExecutor.ValueExternref;
import wasmruntime.ModuleExecutor.ValueStack;
import static wasmruntime.ModuleData.Opcodes.*;
*/

public class Opcodes {
  public static Value[] immediates;
  public static WasmType currentTypeAnnotation;
  
  static FunctionType nop = new FunctionType();
  static FunctionType drop = new FunctionType(new WasmType[] {WasmType.any}, new WasmType[0]);
  public static FunctionType get = new FunctionType(WasmType.T);
  public static FunctionType set = new FunctionType(new WasmType[] {WasmType.T}, new WasmType[0]);
  public static FunctionType tee = new FunctionType(new WasmType[] {WasmType.T}, new WasmType[] {WasmType.T});

  public static FunctionType tableGet = new FunctionType(new WasmType[] {WasmType.i32}, new WasmType[] {WasmType.T});
  public static FunctionType tableSet = new FunctionType(new WasmType[] {WasmType.i32, WasmType.T}, new WasmType[0]);
  public static FunctionType threeI32 = new FunctionType(new WasmType[] {WasmType.i32, WasmType.i32, WasmType.i32}, new WasmType[0]);

  public static FunctionType tableGrow = new FunctionType(new WasmType[] {WasmType.T, WasmType.i32}, new WasmType[] {WasmType.i32});
  public static FunctionType tableFill = new FunctionType(new WasmType[] {WasmType.i32, WasmType.T, WasmType.i32}, new WasmType[0]);

  public static FunctionType getFuncref = new FunctionType(new WasmType[0], new WasmType[] {WasmType.funcref});
  public static FunctionType genericToI32 = new FunctionType(new WasmType[] {WasmType.T}, new WasmType[] {WasmType.i32});

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

  static FunctionType storei32 = new FunctionType(new WasmType[] {WasmType.i32, WasmType.i32}, new WasmType[0]);
  static FunctionType storei64 = new FunctionType(new WasmType[] {WasmType.i32, WasmType.i64}, new WasmType[0]);
  static FunctionType storef32 = new FunctionType(new WasmType[] {WasmType.i32, WasmType.f32}, new WasmType[0]);
  static FunctionType storef64 = new FunctionType(new WasmType[] {WasmType.i32, WasmType.f64}, new WasmType[0]);

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
  public static HashMap<Byte, InstructionType> opcodeExtendedMap = new HashMap<Byte, InstructionType>();

  static {
    opcodeMap.put((byte) 0x00, new InstructionType(Basic::unreachable, nop, new WasmType[0]));
    opcodeMap.put((byte) 0x01, new InstructionType(Basic::nop, nop, new WasmType[0]));
    opcodeMap.put((byte) 0x1A, new InstructionType(Basic::drop, drop, new WasmType[0]));
    opcodeMap.put((byte) 0x1B, new InstructionType(Basic::select, select, new WasmType[0], GenericTypeRequirers.select));
    opcodeMap.put((byte) 0x1C, new InstructionType(Basic::select, select, new WasmType[0], WasmType.any, GenericTypeRequirers.annotated));
    opcodeMap.put((byte) 0x23, new InstructionType(Globals::get, get, new WasmType[] {WasmType.i32}, GenericTypeRequirers.global));
    opcodeMap.put((byte) 0x24, new InstructionType(Globals::set, set, new WasmType[] {WasmType.i32}, GenericTypeRequirers.global));
    opcodeMap.put((byte) 0x25, new InstructionType(Table::get, tableGet, new WasmType[] {WasmType.i32}, GenericTypeRequirers.table));
    opcodeMap.put((byte) 0x26, new InstructionType(Table::set, tableSet, new WasmType[] {WasmType.i32}, GenericTypeRequirers.table));
    opcodeMap.put((byte) 0x28, new InstructionType(Load::loadI32, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x29, new InstructionType(Load::loadI64, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2a, new InstructionType(Load::loadF32, i32Tof32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2b, new InstructionType(Load::loadF64, i32Tof64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2c, new InstructionType(Load::loadI32_8s, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2d, new InstructionType(Load::loadI32_8u, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2e, new InstructionType(Load::loadI32_16s, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x2f, new InstructionType(Load::loadI32_16u, i32Toi32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x30, new InstructionType(Load::loadI64_8s, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x31, new InstructionType(Load::loadI64_8u, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x32, new InstructionType(Load::loadI64_16s, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x33, new InstructionType(Load::loadI64_16u, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x34, new InstructionType(Load::loadI64_32s, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x35, new InstructionType(Load::loadI64_32u, i32Toi64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x36, new InstructionType(Store::storeI32, storei32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x37, new InstructionType(Store::storeI64, storei64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x38, new InstructionType(Store::storeF32, storef32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x39, new InstructionType(Store::storeF64, storef64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3a, new InstructionType(Store::storeI32_8, storei32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3b, new InstructionType(Store::storeI32_16, storei32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3c, new InstructionType(Store::storeI64_8, storei64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3d, new InstructionType(Store::storeI64_16, storei64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3e, new InstructionType(Store::storeI64_32, storei64, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeMap.put((byte) 0x3f, new InstructionType(MemoryManagement::size, consti32, new WasmType[] {WasmType.i32}));
    opcodeMap.put((byte) 0x40, new InstructionType(MemoryManagement::grow, i32Toi32, new WasmType[] {WasmType.i32}));
    opcodeMap.put((byte) 0x41, new InstructionType(Const::ConstOperation, consti32, new WasmType[] {WasmType.i32}));
    opcodeMap.put((byte) 0x42, new InstructionType(Const::ConstOperation, consti64, new WasmType[] {WasmType.i64}));
    opcodeMap.put((byte) 0x43, new InstructionType(Const::ConstOperation, constf32, new WasmType[] {WasmType.f32}));
    opcodeMap.put((byte) 0x44, new InstructionType(Const::ConstOperation, constf64, new WasmType[] {WasmType.f64}));
    opcodeMap.put((byte) 0x45, new InstructionType(eqz::I32, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x46, new InstructionType(eq::I32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x47, new InstructionType(ne::I32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x48, new InstructionType(lt::I32_s, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x49, new InstructionType(lt::I32_u, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4a, new InstructionType(gt::I32_s, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4b, new InstructionType(gt::I32_u, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4c, new InstructionType(le::I32_s, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4d, new InstructionType(le::I32_u, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4e, new InstructionType(ge::I32_s, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x4f, new InstructionType(ge::I32_u, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x50, new InstructionType(eqz::I64, i64Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x51, new InstructionType(eq::I64, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x52, new InstructionType(ne::I64, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x53, new InstructionType(lt::I64_s, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x54, new InstructionType(lt::I64_u, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x55, new InstructionType(gt::I64_s, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x56, new InstructionType(gt::I64_u, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x57, new InstructionType(le::I64_s, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x58, new InstructionType(le::I64_u, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x59, new InstructionType(ge::I64_s, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x5a, new InstructionType(ge::I64_u, comparei64, new WasmType[0]));
    opcodeMap.put((byte) 0x5b, new InstructionType(eq::F32, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x5c, new InstructionType(ne::F32, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x5d, new InstructionType(lt::F32, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x5e, new InstructionType(gt::F32, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x5f, new InstructionType(le::F32, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x60, new InstructionType(ge::F32, comparef32, new WasmType[0]));
    opcodeMap.put((byte) 0x61, new InstructionType(eq::F64, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x62, new InstructionType(ne::F64, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x63, new InstructionType(lt::F64, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x64, new InstructionType(gt::F64, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x65, new InstructionType(le::F64, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x66, new InstructionType(ge::F64, comparef64, new WasmType[0]));
    opcodeMap.put((byte) 0x67, new InstructionType(clz::I32, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x68, new InstructionType(ctz::I32, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x69, new InstructionType(popcnt::I32, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0x6a, new InstructionType(Add::I32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6b, new InstructionType(Subtract::I32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6c, new InstructionType(Multiply::I32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6d, new InstructionType(Divide::I32_s, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6e, new InstructionType(Divide::I32_u, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x6f, new InstructionType(Remainder::i32_s, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x70, new InstructionType(Remainder::i32_u, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x71, new InstructionType(Gates::and_i32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x72, new InstructionType(Gates::or_i32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x73, new InstructionType(Gates::xor_i32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x74, new InstructionType(Shift::shl_i32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x75, new InstructionType(Shift::shr_i32_s, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x76, new InstructionType(Shift::shr_i32_u, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x77, new InstructionType(Rotate::rotl_i32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x78, new InstructionType(Rotate::rotr_i32, comparei32, new WasmType[0]));
    opcodeMap.put((byte) 0x79, new InstructionType(clz::I64, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0x7a, new InstructionType(ctz::I64, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0x7b, new InstructionType(popcnt::I64, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0x7c, new InstructionType(Add::I64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x7d, new InstructionType(Subtract::I64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x7e, new InstructionType(Multiply::I64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x7f, new InstructionType(Divide::I64_s, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x80, new InstructionType(Divide::I64_u, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x81, new InstructionType(Remainder::i64_s, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x82, new InstructionType(Remainder::i64_u, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x83, new InstructionType(Gates::and_i64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x84, new InstructionType(Gates::or_i64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x85, new InstructionType(Gates::xor_i64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x86, new InstructionType(Shift::shl_i64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x87, new InstructionType(Shift::shr_i64_s, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x88, new InstructionType(Shift::shr_i64_u, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x89, new InstructionType(Rotate::rotl_i64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x8a, new InstructionType(Rotate::rotr_i64, operatori64, new WasmType[0]));
    opcodeMap.put((byte) 0x8b, new InstructionType(abs::F32, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x8c, new InstructionType(neg::F32, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x8d, new InstructionType(Rounding::ceil_f32, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x8e, new InstructionType(Rounding::floor_f32, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x8f, new InstructionType(Rounding::trunc_f32, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x90, new InstructionType(Rounding::nearest_f32, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x91, new InstructionType(sqrt::F32, f32Tof32, new WasmType[0]));
    opcodeMap.put((byte) 0x92, new InstructionType(Add::F32, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x93, new InstructionType(Subtract::F32, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x94, new InstructionType(Multiply::F32, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x95, new InstructionType(Divide::F32, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x96, new InstructionType(MinMax::Min_F32, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x97, new InstructionType(MinMax::Max_F32, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x98, new InstructionType(copysign::F32, operatorf32, new WasmType[0]));
    opcodeMap.put((byte) 0x99, new InstructionType(abs::F64, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9a, new InstructionType(neg::F64, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9b, new InstructionType(Rounding::ceil_f64, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9c, new InstructionType(Rounding::floor_f64, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9d, new InstructionType(Rounding::trunc_f64, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9e, new InstructionType(Rounding::nearest_f64, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0x9f, new InstructionType(sqrt::F64, f64Tof64, new WasmType[0]));
    opcodeMap.put((byte) 0xa0, new InstructionType(Add::F64, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa1, new InstructionType(Subtract::F64, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa2, new InstructionType(Multiply::F64, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa3, new InstructionType(Divide::F64, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa4, new InstructionType(MinMax::Min_F64, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa5, new InstructionType(MinMax::Max_F64, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa6, new InstructionType(copysign::F64, operatorf64, new WasmType[0]));
    opcodeMap.put((byte) 0xa7, new InstructionType(wrap::I32, i64Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xa8, new InstructionType(Trunc::f32_i32_s, f32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xa9, new InstructionType(Trunc::f32_i32_u, f32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xaa, new InstructionType(Trunc::f64_i32_s, f64Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xab, new InstructionType(Trunc::f64_i32_u, f64Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xac, new InstructionType(Extend::I32_s, i32Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xad, new InstructionType(Extend::I32_u, i32Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xae, new InstructionType(Trunc::f32_i64_s, f32Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xaf, new InstructionType(Trunc::f32_i64_u, f32Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xb0, new InstructionType(Trunc::f64_i64_s, f64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xb1, new InstructionType(Trunc::f64_i64_u, f64Toi64, new WasmType[0]));
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
    opcodeMap.put((byte) 0xc0, new InstructionType(Extend::I32_8, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xc1, new InstructionType(Extend::I32_16, i32Toi32, new WasmType[0]));
    opcodeMap.put((byte) 0xc2, new InstructionType(Extend::I64_8, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xc3, new InstructionType(Extend::I64_16, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xc4, new InstructionType(Extend::I64_32, i64Toi64, new WasmType[0]));
    opcodeMap.put((byte) 0xd0, new InstructionType(Reference::nullRef, get, new WasmType[0], WasmType.reftype, GenericTypeRequirers.annotated));
    opcodeMap.put((byte) 0xd1, new InstructionType(Reference::isNull, genericToI32, new WasmType[0]));
    opcodeMap.put((byte) 0xd2, new InstructionType(Reference::func, getFuncref, new WasmType[] {WasmType.i32}));

    opcodeExtendedMap.put((byte) 0x00, new InstructionType(Opcodes::temp, f32Toi32, new WasmType[0]));
    opcodeExtendedMap.put((byte) 0x01, new InstructionType(Opcodes::temp, f32Toi32, new WasmType[0]));
    opcodeExtendedMap.put((byte) 0x02, new InstructionType(Opcodes::temp, f64Toi32, new WasmType[0]));
    opcodeExtendedMap.put((byte) 0x03, new InstructionType(Opcodes::temp, f64Toi32, new WasmType[0]));
    opcodeExtendedMap.put((byte) 0x04, new InstructionType(Opcodes::temp, f32Toi64, new WasmType[0]));
    opcodeExtendedMap.put((byte) 0x05, new InstructionType(Opcodes::temp, f32Toi64, new WasmType[0]));
    opcodeExtendedMap.put((byte) 0x06, new InstructionType(Opcodes::temp, f64Toi64, new WasmType[0]));
    opcodeExtendedMap.put((byte) 0x07, new InstructionType(Opcodes::temp, f64Toi64, new WasmType[0]));

    opcodeExtendedMap.put((byte) 0x08, new InstructionType(Opcodes::temp, threeI32, new WasmType[] {WasmType.i32}));
    opcodeExtendedMap.put((byte) 0x09, new InstructionType(Opcodes::temp, nop, new WasmType[] {WasmType.i32}));
    opcodeExtendedMap.put((byte) 0x0a, new InstructionType(Opcodes::temp, threeI32, new WasmType[0]));
    opcodeExtendedMap.put((byte) 0x0b, new InstructionType(Opcodes::temp, threeI32, new WasmType[0]));

    opcodeExtendedMap.put((byte) 0x0c, new InstructionType(Table::init, threeI32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeExtendedMap.put((byte) 0x0d, new InstructionType(Table::dropElem, nop, new WasmType[] {WasmType.i32}));
    opcodeExtendedMap.put((byte) 0x0e, new InstructionType(Table::copy, threeI32, new WasmType[] {WasmType.i32, WasmType.i32}));
    opcodeExtendedMap.put((byte) 0x0f, new InstructionType(Table::grow, tableGrow, new WasmType[] {WasmType.i32}, GenericTypeRequirers.table));
    opcodeExtendedMap.put((byte) 0x10, new InstructionType(Table::size, consti32, new WasmType[] {WasmType.i32}));
    opcodeExtendedMap.put((byte) 0x11, new InstructionType(Table::fill, tableFill, new WasmType[] {WasmType.i32}, GenericTypeRequirers.table));
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

  public static int funcref(Value v) {
    return ((ValueFuncref)v).value;
  }

  public static int externref(Value v) {
    return ((ValueExternref)v).value;
  }

  public static int i32(ValueStack v) {
    return ((ValueI32)v.pop()).value;
  }

  public static float f32(ValueStack v) {
    return ((ValueF32)v.pop()).value;
  }

  public static long i64(ValueStack v) {
    return ((ValueI64)v.pop()).value;
  }

  public static double f64(ValueStack v) {
    return ((ValueF64)(v.pop())).value;
  }

  public static int funcref(ValueStack v) {
    return ((ValueFuncref)(v.pop())).value;
  }

  public static int externref(ValueStack v) {
    return ((ValueExternref)(v.pop())).value;
  }

  private static void temp(ValueStack stack) {

  }
}
