use jni::{JNIEnv, objects::{JClass, JString}, sys::jstring};

use crate::common::get_source_data;

#[no_mangle]
pub extern "C" fn Java_com_chrissytopher_source_SourceApi_getSourceData<'local>(mut env: JNIEnv<'local>, _class: JClass<'local>, username: JString<'local>, password: JString<'local>, download_path: JString<'local>) -> jstring {
    let username: String =
        env.get_string(&username).expect("Couldn't get java string!").into();

    let password: String =
        env.get_string(&password).expect("Couldn't get java string!").into();

    let download_path: String =
        env.get_string(&download_path).expect("Couldn't get java string!").into();

    let data_res = get_source_data(&username, &password, &download_path);
    let data = match data_res {
        Ok(data) => data,
        Err(e) => format!("{e:?}"),
    };

    let output = env.new_string(data)
        .expect("Couldn't create java string!");

    output.into_raw()
}