use std::thread;
use std::sync::mpsc::channel;

fn main() {
    native_thread();

    simple_mpsc();

    shared_mpsc();
}

fn native_thread() {
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

fn simple_mpsc() {
    let (tx, rx) = channel();
    thread::spawn(move || {
        tx.send(10).unwrap();
    });
    print!("receive: {}\n", rx.recv().unwrap());
}

fn shared_mpsc() {
    let (tx, rx) = channel();
    for i in 0..10 {
        let tx = tx.clone();
        thread::spawn(move || {
            tx.send(i).unwrap();
        });
    }

    for _ in 0..10 {
        print!("recv: {}\n", rx.recv().unwrap());
    }
}