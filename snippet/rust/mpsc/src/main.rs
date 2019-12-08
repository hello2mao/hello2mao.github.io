use std::thread;
use std::sync::mpsc::channel;

fn main() {
    simple();
    shared();
}

fn simple() {
    let (tx, rx) = channel();
    thread::spawn(move || {
        tx.send(10).unwrap();
    });
    print!("receive: {}\n", rx.recv().unwrap());
}

fn shared() {
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