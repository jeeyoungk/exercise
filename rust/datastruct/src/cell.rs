// testing logic around cell and rccell

mod tests {
    use std::cell::Cell;
    use std::cell::RefCell;
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
                _ => ()
            }
        }
        {
            let mut m = c.borrow_mut();
            *m = another_value;
        }
        assert_eq!(*c.borrow(), 5);
    }
}