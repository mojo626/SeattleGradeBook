
use std::ffi::{CStr, CString};
use std::os::raw::c_char;

use crate::common::get_source_data as gsd;

#[no_mangle]
pub extern "C" fn get_source_data(username_raw: *const c_char, password_raw: *const c_char, download_path: *const c_char, quarter: *const c_char, load_pfp: bool) -> *mut c_char {
    let username = unsafe { CStr::from_ptr(username_raw) };
    let password = unsafe { CStr::from_ptr(password_raw) };
    let download_path = unsafe { CStr::from_ptr(download_path) };
    let quarter = unsafe { CStr::from_ptr(quarter) };
    let data_res = tokio::runtime::Runtime::new().unwrap().block_on(gsd(username.to_str().expect("can't get cstr"), password.to_str().expect("can't get cstr"), download_path.to_str().expect("can't get cstr"), quarter.to_str().expect("can't get cstr"), load_pfp));
    let data = match data_res {
        Ok(data) => data,
        Err(e) => format!("{e:?}"),
    };

    let output = CString::new(data).unwrap();

    output.into_raw()
}