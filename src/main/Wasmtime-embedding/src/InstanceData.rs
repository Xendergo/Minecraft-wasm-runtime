use crate::LanguageTrait::LanguageImpl;
use wasmtime::Instance;

pub struct InstanceDataStruct {
  pub instance: Instance,
  pub langImpl: Box<dyn LanguageImpl>,
}