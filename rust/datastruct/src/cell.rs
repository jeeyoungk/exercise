// testing logic around cell and rccell

mod tests {
    use std::cell::Cell;
    use std::cell::RefCell;
    use std::rc::Rc;
    use std::sync::Arc;
    use std::thread;
    use std::sync::atomic;

    #[test]
    fn test_cell() {
        // testing interior vs inherited mutablity.
        let c = Cell::new(3);
        assert_eq!(c.get(), 3);
        c.set(10);
        assert_eq!(c.get(), 10);
    }

    #[allow(unused)]
    #[test]
    fn test_refcell() {
        let value = 3;
        let another_value = 5;
        let c = RefCell::new(value);
        assert_eq!(*c.borrow(), 3);
        {
            let m = c.borrow_mut();
            let y = c.try_borrow(); // this panics
            match y {
                Ok(_) => assert!(false),
                _ => (),
            }
        }
        {
            let mut m = c.borrow_mut();
            *m = another_value;
        }
        assert_eq!(*c.borrow(), 5);
    }

    #[test]
    fn test_weakref() {
        let mut value: Rc<i32> = Rc::new(0);
        *Rc::get_mut(&mut value).unwrap() = 1;
        assert_eq!(*value, 1);
        // weak references can be upgraded.
        let wr = Rc::downgrade(&value);
        let upgraded = wr.upgrade().unwrap();
        drop(upgraded);
        assert!(Rc::get_mut(&mut value).is_none());
        drop(value);
        assert!(wr.upgrade().is_none());
    }

    fn increment_in_thread(u: Arc<atomic::AtomicUsize>) {
        let handle = thread::spawn(move || {
            u.fetch_add(1, atomic::Ordering::Relaxed);
            println!("Hello, world");
        });
        handle.join();
    }
    #[test]
    fn test_threads() {
        let lock = Arc::new(atomic::AtomicUsize::new(0));
        increment_in_thread(lock.clone());
        assert_eq!(lock.load(atomic::Ordering::Relaxed), 1);
    }
}
