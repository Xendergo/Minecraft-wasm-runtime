use crate::errors::{Error, Result};
use jni::{
    objects::{JObject, JValue},
    JNIEnv,
};

#[derive(Debug, Clone)]
pub enum InteroperableVal {
    Bool(bool),
    I8(i8),
    U8(u8),
    I16(i16),
    U16(u16),
    I32(i32),
    U32(u32),
    I64(i64),
    U64(u64),
    F32(f32),
    F64(f64),
    String(String),
}

impl InteroperableVal {
    pub fn ty(&self) -> InteroperableType {
        use InteroperableVal::*;

        match self {
            Bool(_) => InteroperableType::Bool,
            I8(_) => InteroperableType::I8,
            U8(_) => InteroperableType::U8,
            I16(_) => InteroperableType::I16,
            U16(_) => InteroperableType::U16,
            I32(_) => InteroperableType::I32,
            U32(_) => InteroperableType::U32,
            I64(_) => InteroperableType::I64,
            U64(_) => InteroperableType::U64,
            F32(_) => InteroperableType::F32,
            F64(_) => InteroperableType::F64,
            String(_) => InteroperableType::String,
        }
    }
}

#[derive(Debug, Clone, Copy)]
pub enum InteroperableType {
    Bool,
    I8,
    U8,
    I16,
    U16,
    I32,
    U32,
    I64,
    U64,
    F32,
    F64,
    String,
}

impl InteroperableType {
    pub fn to_type_index(self) -> u8 {
        use InteroperableType::*;

        match self {
            Bool => 1,
            U8 => 2,
            I8 => 3,
            U16 => 4,
            I16 => 5,
            U32 => 6,
            I32 => 7,
            U64 => 8,
            I64 => 9,
            F32 => 10,
            F64 => 11,
            String => 12,
        }
    }

    pub fn from_type_index(index: u8) -> Result<Self, Error> {
        use InteroperableType::*;

        Ok(match index {
            1 => Bool,
            2 => U8,
            3 => I8,
            4 => U16,
            5 => I16,
            6 => U32,
            7 => I32,
            8 => U64,
            9 => I64,
            10 => F32,
            11 => F64,
            12 => String,
            _ => return Err(Error::UnknownInteroperableTypeID),
        })
    }
}

macro_rules! strict_maybe_unwrap {
    ($name: ident, $variant: ident, $type_name: ty) => {
        pub fn $name(self) -> Result<$type_name, Error> {
            match self {
                InteroperableVal::$variant(v) => Ok(v),
                _ => Err(Error::ConversionError(format!(
                    "Value isn't a {}",
                    stringify!($type_name)
                ))),
            }
        }
    };
}

macro_rules! loose_maybe_unwrap {
    ($name: ident, $type_name: ty, $( $variant: pat_param => $conversion: expr),*,) => {
        pub fn $name(self) -> Result<$type_name, Error> {
            use InteroperableVal::*;

            match self {
                $(
                    $variant => Ok($conversion),
                )*
            }
        }
    };
}

macro_rules! loose_maybe_integer_unwrap {
    ($name: ident, $type_name: ident) => {
        loose_maybe_unwrap!($name, $type_name,
            Bool(v) => if v { 1 } else { 0 },
            U8(v) => v as $type_name,
            I8(v) => v as $type_name,
            U16(v) => v as $type_name,
            I16(v) => v as $type_name,
            U32(v) => v as $type_name,
            I32(v) => v as $type_name,
            U64(v) => v as $type_name,
            I64(v) => v as $type_name,
            F32(v) => if v.fract() == 0.0 { v as $type_name } else { return Err(Error::ConversionError("Can't convert a float with a decimal to an integer".to_owned())) },
            F64(v) => if v.fract() == 0.0 { v as $type_name } else { return Err(Error::ConversionError("Can't convert a float with a decimal to an integer".to_owned())) },
            String(v) => v.parse().ok().ok_or(Error::ConversionError("Can't parse the string into an integer".to_owned()))?,
        );
    };
}

macro_rules! loose_maybe_float_unwrap {
    ($name: ident, $type_name: ident) => {
        loose_maybe_unwrap!($name, $type_name,
            Bool(v) => if v { 1.0 } else { 0.0 },
            U8(v) => v as $type_name,
            I8(v) => v as $type_name,
            U16(v) => v as $type_name,
            I16(v) => v as $type_name,
            U32(v) => v as $type_name,
            I32(v) => v as $type_name,
            U64(v) => v as $type_name,
            I64(v) => v as $type_name,
            F32(v) => v as $type_name,
            F64(v) => v as $type_name,
            String(v) => v.parse().ok().ok_or_else(|| Error::ConversionError("Can't parse the string into a float".to_owned()))?,
        );
    };
}

impl InteroperableVal {
    strict_maybe_unwrap!(strict_u8, U8, u8);
    strict_maybe_unwrap!(strict_i8, I8, i8);
    strict_maybe_unwrap!(strict_u16, U16, u16);
    strict_maybe_unwrap!(strict_i16, I16, i16);
    strict_maybe_unwrap!(strict_u32, U32, u32);
    strict_maybe_unwrap!(strict_i32, I32, i32);
    strict_maybe_unwrap!(strict_u64, U64, u64);
    strict_maybe_unwrap!(strict_i64, I64, i64);
    strict_maybe_unwrap!(strict_f32, F32, f32);
    strict_maybe_unwrap!(strict_f64, F64, f64);
    strict_maybe_unwrap!(strict_string, String, String);

    loose_maybe_unwrap!(loose_void, (),
        _ => (),
    );

    loose_maybe_unwrap!(loose_bool, bool,
        Bool(v) => v,

        U8(v) => v != 0,
        I8(v) => v != 0,
        U16(v) => v != 0,
        I16(v) => v != 0,
        U32(v) => v != 0,
        I32(v) => v != 0,
        U64(v) => v != 0,
        I64(v) => v != 0,
        F32(v) => v != 0.0,
        F64(v) => v != 0.0,
        String(v) => match v.to_lowercase().as_str() {
            "true" => true,
            "false" => false,
            _ => return Err(Error::ConversionError("Can't parse the string into a bool".to_owned())),
        },
    );

    loose_maybe_integer_unwrap!(loose_u8, u8);
    loose_maybe_integer_unwrap!(loose_i8, i8);
    loose_maybe_integer_unwrap!(loose_u16, u16);
    loose_maybe_integer_unwrap!(loose_i16, i16);
    loose_maybe_integer_unwrap!(loose_u32, u32);
    loose_maybe_integer_unwrap!(loose_i32, i32);
    loose_maybe_integer_unwrap!(loose_u64, u64);
    loose_maybe_integer_unwrap!(loose_i64, i64);

    loose_maybe_float_unwrap!(loose_f32, f32);
    loose_maybe_float_unwrap!(loose_f64, f64);

    loose_maybe_unwrap!(loose_string, String,
        Bool(v) => v.to_string(),
        U8(v) => v.to_string(),
        I8(v) => v.to_string(),
        U16(v) => v.to_string(),
        I16(v) => v.to_string(),
        U32(v) => v.to_string(),
        I32(v) => v.to_string(),
        U64(v) => v.to_string(),
        I64(v) => v.to_string(),
        F32(v) => v.to_string(),
        F64(v) => v.to_string(),
        String(v) => v,
    );
}

pub fn obj_to_interoperable_val(
    env: &JNIEnv,
    obj: JObject,
    val_type: InteroperableType,
) -> Result<InteroperableVal> {
    match val_type {
        InteroperableType::Bool => Ok(InteroperableVal::Bool(
            env.call_method(obj, "bool", "()Z", &[])?.z()?,
        )),
        InteroperableType::U8 => Ok(InteroperableVal::U8(
            env.call_method(obj, "i8", "()B", &[])?.b()? as u8,
        )),
        InteroperableType::I8 => Ok(InteroperableVal::I8(
            env.call_method(obj, "i8", "()B", &[])?.b()?,
        )),
        InteroperableType::U16 => Ok(InteroperableVal::U16(
            env.call_method(obj, "i16", "()S", &[])?.s()? as u16,
        )),
        InteroperableType::I16 => Ok(InteroperableVal::I16(
            env.call_method(obj, "i16", "()S", &[])?.s()?,
        )),
        InteroperableType::U32 => Ok(InteroperableVal::U32(
            env.call_method(obj, "i32", "()I", &[])?.i()? as u32,
        )),
        InteroperableType::I32 => Ok(InteroperableVal::I32(
            env.call_method(obj, "i32", "()I", &[])?.i()?,
        )),
        InteroperableType::U64 => Ok(InteroperableVal::U64(
            env.call_method(obj, "i64", "()J", &[])?.j()? as u64,
        )),
        InteroperableType::I64 => Ok(InteroperableVal::I64(
            env.call_method(obj, "i64", "()J", &[])?.j()?,
        )),
        InteroperableType::F32 => Ok(InteroperableVal::F32(
            env.call_method(obj, "f32", "()F", &[])?.f()?,
        )),
        InteroperableType::F64 => Ok(InteroperableVal::F64(
            env.call_method(obj, "f64", "()D", &[])?.d()?,
        )),
        InteroperableType::String => Ok(InteroperableVal::String(
            env.get_string(
                env.call_method(obj, "string", "()Ljava/lang/String;", &[])?
                    .l()?
                    .into(),
            )?
            .into(),
        )),
    }
}

pub fn interoperable_val_to_object<'a>(
    env: &JNIEnv<'a>,
    val: &InteroperableVal,
) -> Result<JObject<'a>, Error> {
    let j_interoperable_type = env
        .call_static_method(
            env.find_class("wasmruntime/Types/InteroperableType")?,
            "fromId",
            "(B)Lwasmruntime/Types/InteroperableType;",
            &[JValue::Byte(val.ty().to_type_index() as i8)],
        )?
        .l()?;

    use InteroperableVal::*;

    let arg = match val {
        Bool(v) => env.new_object(
            env.find_class("java/lang/Boolean")?,
            "(Z)V",
            &[JValue::Bool(*v as u8)],
        )?,
        U8(v) => env.new_object(
            env.find_class("java/lang/Byte")?,
            "(B)V",
            &[JValue::Byte(*v as i8)],
        )?,
        I8(v) => env.new_object(
            env.find_class("java/lang/Byte")?,
            "(B)V",
            &[JValue::Byte(*v)],
        )?,
        U16(v) => env.new_object(
            env.find_class("java/lang/Short")?,
            "(S)V",
            &[JValue::Short(*v as i16)],
        )?,
        I16(v) => env.new_object(
            env.find_class("java/lang/Short")?,
            "(S)V",
            &[JValue::Short(*v)],
        )?,
        U32(v) => env.new_object(
            env.find_class("java/lang/Int")?,
            "(I)V",
            &[JValue::Int(*v as i32)],
        )?,
        I32(v) => env.new_object(env.find_class("java/lang/Int")?, "(I)V", &[JValue::Int(*v)])?,
        U64(v) => env.new_object(
            env.find_class("java/lang/Long")?,
            "(J)V",
            &[JValue::Long(*v as i64)],
        )?,
        I64(v) => env.new_object(
            env.find_class("java/lang/Long")?,
            "(J)V",
            &[JValue::Long(*v)],
        )?,
        F32(v) => env.new_object(
            env.find_class("java/lang/Float")?,
            "(F)V",
            &[JValue::Float(*v)],
        )?,
        F64(v) => env.new_object(
            env.find_class("java/lang/Double")?,
            "(D)V",
            &[JValue::Double(*v)],
        )?,
        String(v) => env.new_string(v)?.into(),
    };

    Ok(env.new_object(
        env.find_class("wasmruntime/Types/Value")?,
        "(Ljava/lang/Object;Lwasmruntime/Types/InteroperableType;)V",
        &[JValue::Object(arg), JValue::Object(j_interoperable_type)],
    )?)
}
