use crate::Languages::AssemblyScript::AssemblyScriptLang;
use crate::Languages::Unknown::UnknownLang;
use crate::LanguageTrait::LanguageImpl;
use wasmtime::*;

#[derive(Debug)]
pub enum Language {
  AssemblyScript,
  Unknown,
}

pub fn getLanguage(Instance: &Instance) -> Language {
  match Instance.get_global("language") {
    Some(v) => {
      match v.get() {
        Val::I32(v) => getLanguageFromNumber(v),
        Val::I64(v) => getLanguageFromNumber(v as i32),
        Val::F32(v) => getLanguageFromNumber(v as i32),
        Val::F64(v) => getLanguageFromNumber(v as i32),
        _ => Language::Unknown
      }
    },
    None => Language::Unknown
  }
}

fn getLanguageFromNumber(v: i32) -> Language {
  match v {
    0 => Language::AssemblyScript,
    _ => Language::Unknown
  }
}

pub fn constructLanguageImpl(language: Language) -> Box<dyn LanguageImpl> {
  match language {
    Language::AssemblyScript => Box::new(AssemblyScriptLang {}),
    Language::Unknown => Box::new(UnknownLang {})
  }
}