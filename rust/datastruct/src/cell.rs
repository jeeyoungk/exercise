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
    fn test_refcell_one() {
        let value = 3;
        let another_value = 5;
        let c = RefCell::new(value);
        assert_eq!(*c.borrow(), 3);
        {
            let m = c.borrow_mut();
            match c.try_borrow() {
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
    fn test_refcell_two() {
        use std::collections::HashMap;
        let shared_map: Rc<RefCell<_>> = Rc::new(RefCell::new(HashMap::new()));
        shared_map.borrow_mut().insert("africa", 92388);
        shared_map.borrow_mut().insert("kyoto", 11837);
        shared_map.borrow_mut().insert("piccadilly", 11826);
        shared_map.borrow_mut().insert("marbles", 38);
    }

    #[test]
    fn test_refcell_three() {
        struct S {
            x: isize,
            y: isize,
        }
        let shared_struct: RefCell<S> = RefCell::new(S { x: 0, y: 0 });
        shared_struct.borrow_mut().x = 1;
        shared_struct.borrow_mut().y = 2;
        assert_eq!(shared_struct.borrow().x, 1);
        assert_eq!(shared_struct.borrow().y, 2);
    }

    struct A {
        v: isize,
        // nullable ref-counted pointer to internally mutable struct.
        ptr: Option<Rc<RefCell<A>>>,
    }

    #[test]
    fn test_mutually_mutable() {
        // linked list, pointing both ways.
        let a = A { v: 3, ptr: None };
        let b = A { v: 5, ptr: None };
        let a_ref = Rc::new(RefCell::new(a));
        let b_ref = Rc::new(RefCell::new(b));
        // a and b are now moved - cannot be used.
        assert_eq!(a_ref.borrow().v, 3);
        assert_eq!(b_ref.borrow().v, 5);
        // set pointers to each other.
        b_ref.borrow_mut().ptr = Some(a_ref.clone());
        a_ref.borrow_mut().ptr = Some(b_ref.clone());
        // try traversing.
        match b_ref.borrow().ptr {
            None => panic!("should not panic"),
            Some(ref a) => {
                assert_eq!(a.borrow().v, 3);
            }
        };
        a_ref.borrow_mut().v = 10;
        b_ref.borrow_mut().v = 15;
        match b_ref.borrow().ptr {
            None => panic!("should not panic"),
            Some(ref a) => {
                // mutable borrow should succeed here.
                assert!(a.try_borrow_mut().is_ok());
                assert_eq!(a.borrow().v, 10);
            }
        };
        match b_ref.borrow().ptr {
            None => panic!("should not panic"),
            Some(ref a) => {
                match a.borrow().ptr {
                    None => panic!("should not panic"),
                    Some(ref b) => {
                        // mutable borrow of b should fail here.
                        // as we borrowed immutably above.
                        assert!(b.try_borrow_mut().is_err());
                        assert_eq!(b.borrow().v, 15)
                    }
                };
            }
        };
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
        let handle = thread::spawn(move || { u.fetch_add(1, atomic::Ordering::Relaxed); });
        match handle.join() {
            Err(_) => panic!("Unexpected join failure."),
            _ => (),
        }
    }

    #[test]
    fn test_threads() {
        let lock = Arc::new(atomic::AtomicUsize::new(0));
        increment_in_thread(lock.clone());
        assert_eq!(lock.load(atomic::Ordering::Relaxed), 1);
    }
}
