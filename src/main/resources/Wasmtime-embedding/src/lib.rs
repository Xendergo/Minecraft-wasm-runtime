use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

#[no_mangle]
pub extern "system" fn Java_ModuleWrapper_yee(env: JNIEnv,
                                              class: JClass,
                                              input: JString)
                                            -> jstring {
  // First, we have to get the string out of Java. Check out the `strings`
  // module for more info on how this works.
  let input: String = env.get_string(input).expect("Couldn't get java string!").into();

  // Then we have to create a new Java string to return. Again, more info
  // in the `strings` module.
  let output = env.new_string(format!("Hello, {}!", input))
      .expect("Couldn't create java string!");

  // Finally, extract the raw pointer to return.
  output.into_inner()
}