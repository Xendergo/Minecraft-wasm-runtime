use crate::Languages::AssemblyScript::AssemblyScriptLang;
use crate::LanguageTrait::LanguageImpl;
use wasmtime::*;

#[derive(Debug)]
pub enum Language {
  AssemblyScript
}

pub fn getLanguage(Instance: &Instance) -> Language {
  match Instance.get_global("language") {
    Some(v) => {
      match v.get() {
        Val::I32(v) => {
          match v {
            0 => Language::AssemblyScript,
            _ => Language::AssemblyScript
          }
        },
        _ => Language::AssemblyScript
      }
    },
    None => Language::AssemblyScript
  }
}

pub fn constructLanguageImpl(language: Language) -> Box<dyn LanguageImpl> {
  match language {
    Language::AssemblyScript => Box::new(AssemblyScriptLang {})
  }
}