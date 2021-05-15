use jni::sys::jobject;
use jni::objects::JValue;
use byteorder::{LittleEndian, ByteOrder};
use crate::JNIEnv;
use jni::objects::JList;
use wasmtime::{Instance};
use crate::errors::{Error, Result};
use crate::LanguageTrait::LanguageImpl;

pub struct AssemblyScriptLang {}

enum PtrTypes {
  String
}

impl PtrTypes {
  fn getId(&self) -> i32 {
    match self {
      PtrTypes::String => 1
    }
  }
}

impl AssemblyScriptLang {
  fn PtrInfo(env: JNIEnv, jniPtr: JList, Instance: &Instance) -> Result<(usize, usize)> {
    let ptr = env.call_method(jniPtr.get(0)?.unwrap(), "longValue", "()J", &[])?.j()? as usize;
    let lenBuffer = &mut [0, 0, 0, 0];
    Instance.get_memory("memory").unwrap().read(ptr - 4, lenBuffer)?;
    let len = LittleEndian::read_u32(lenBuffer) as usize;

    Ok((ptr, len))
  }

  fn New(len: i32, ptrType: PtrTypes, Instance: &Instance) -> Result<i32> {
    let __new = Instance.get_typed_func::<(i32, i32), (i32,)>("__new")?;

    Ok(__new.call((len, ptrType.getId()))?.0)
  }
}

impl LanguageImpl for AssemblyScriptLang {
  fn ReadString(&self, env: JNIEnv, jniPtr: JList, Instance: &Instance) -> Result<String> {
    let (ptr, len) = AssemblyScriptLang::PtrInfo(env, jniPtr, Instance)?;

    let mem = Instance.get_memory("memory").ok_or(Error::String("There's no memory exported named \"name\"".to_string()))?;

    let bytes;
    unsafe {
      bytes = mem.data_unchecked();
    }

    let mut u16Slice: Vec<u16> = Vec::new();

    for v in 0..len >> 1 {
      u16Slice.push(LittleEndian::read_u16(bytes.get(ptr + (v << 1)..).ok_or("Pointer is out of range".to_string())?));
    }

    Ok(String::from_utf16(&u16Slice)?)
  }

  fn NewString(&self, env: JNIEnv, str: String, Instance: &Instance) -> Result<jobject> {
    let wasmPtr = AssemblyScriptLang::New((str.len() << 1) as i32, PtrTypes::String, Instance)?;

    let mem = Instance.get_memory("memory").ok_or(Error::String("There's no memory exported named \"name\"".to_string()))?;

    let u16Utf16: Vec<u16> = str.encode_utf16().collect();
    let mut u8Utf16: Vec<u8> = Vec::new();

    for v in u16Utf16 {
      let mut buf = [0; 2];
      LittleEndian::write_u16(&mut buf, v);
      u8Utf16.push(buf[0]);
      u8Utf16.push(buf[1]);
    }

    mem.write(wasmPtr as usize, &u8Utf16)?;

    let list = env.find_class("java/util/ArrayList")?;
    let long = env.find_class("java/lang/Long")?;

    let ret = JList::from_env(&env, env.new_object(list, "()V", &[])?)?;

    ret.add(env.new_object(long, "(J)V", &[JValue::Long(wasmPtr as i64)])?)?;

    Ok(ret.into_inner())
  }
}