use std::alloc::{alloc, dealloc};
use std::{alloc::Layout, mem};

// https://github.com/rustwasm/wasm-bindgen/blob/main/src/lib.rs#L1397
#[no_mangle]
extern "C" fn __malloc(amt: usize) -> *mut u8 {
    let align = mem::align_of::<usize>();
    if let Ok(layout) = Layout::from_size_align(amt, align) {
        unsafe {
            if layout.size() > 0 {
                return alloc(layout);
            } else {
                return align as *mut u8;
            }
        }
    }

    std::ptr::null_mut()
}

// https://github.com/rustwasm/wasm-bindgen/blob/main/src/lib.rs#L1439
#[no_mangle]
unsafe extern "C" fn __free(ptr: *mut u8, size: usize) {
    if size == 0 {
        return;
    }

    let align = mem::align_of::<usize>();
    let layout = Layout::from_size_align_unchecked(size, align);
    dealloc(ptr, layout);
}
