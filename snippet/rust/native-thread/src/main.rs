use std::thread;

fn main() {
    // 创建一个线程，名字是thread1，堆栈大小为4k
    let new_thread_result = thread::Builder::new()
                            .name("thread1".to_string())
                            .stack_size(4*1024*1024)
                            .spawn(move || {
                                println!("I am thread1.")
                            });
    // 等待线程执行完成
    new_thread_result.unwrap().join().unwrap();
}
