mod columnar;
mod linkedlist;
mod cell;
mod tree;

extern crate rand;

fn main() {
    println!("Hello, world!");
}

#[cfg(test)]
mod tests {
    use std::mem;
    #[test]
    fn test_intersection_lifetime() {
        fn x_or_y<'a>(x : &'a i32, y: &'a i32) -> &'a i32{
            if x < y {
                return x;
            }
            return y;
        }
        {
            let x = &3;
            {
                let y = &5;
                assert_eq!(*x_or_y(x, y), 3);
                assert_eq!(*x_or_y(y, x), 3);
            }
        }
    }
    #[test]
    fn test_different_lifetime() {
        struct XY<'a, 'b> {
            x: &'a i32,
            y: &'b i32,
        }
        {
            let x = &3;
            let mut z = &5;
            assert_eq!(*z, 5);
            {
                let y = &7;
                let tmp = XY{x, y};
                z = tmp.x; // this code would not work without x and y being in different lifetime.
                assert_eq!(*tmp.y, 7);
            }
            assert_eq!(*z, 3);
        }
    }

    #[test]
    fn test_reference() {
        let x = 5;
        let a = &x;
        let b = &x; // duplicate read-only references are allowed.
        let c = a; // reference can be copied.
        assert_eq!(x, *a);
        assert_eq!(x, *b);
        assert_eq!(x, *c);
    }

    #[test]
    fn test_copy() {
        let x = 5;
        let y = x; // copy
        assert_eq!(x, y);
    }

    #[test]
    fn test_non_copy() {
        let x = Box::new(5);
        let y = x; // move
        // assert_eq!(x, y); // cannot use x again.
    }

    #[test]
    #[allow(dead_code)]
    fn test_sizeof() {
        struct SingleField {
            x: i32
        }
        struct RefField<'a> {
            x: &'a i32
        }
        struct RefFieldMultiple<'a> {
            x: &'a i16,
            y: &'a i32,
            z: &'a i64,
        }
        enum EnumA {
            A,
            B,
            C
        }
        enum EnumB {
            A,
            B(i32),
        }
        enum EnumC {
            A,
            B(i32),
            C(i64),
        }
        assert_eq!(mem::size_of::<i64>(), 8);
        assert_eq!(mem::size_of::<i32>(), 4);
        assert_eq!(mem::size_of::<i16>(), 2);
        assert_eq!(mem::size_of::<i8>(), 1);

        assert_eq!(mem::size_of::<SingleField>(), 4);
        assert_eq!(mem::size_of::<RefField>(), 8); // pointer size.
        assert_eq!(mem::size_of::<RefFieldMultiple>(), 24); // pointer size.
        assert_eq!(mem::size_of::<EnumA>(), 1);
        assert_eq!(mem::size_of::<EnumB>(), 8);
        assert_eq!(mem::size_of::<EnumC>(), 16);
    }
}