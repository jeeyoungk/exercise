// #![feature(specialization)]

mod columnar;
mod linkedlist;
mod list_attempt_2;
mod cell;
mod tree;
mod tree_attempt_2;
mod kmeans;
mod complex;
mod circular_palindrome;

extern crate rand;

// rust - weird things.
// - mut Box<T> offers both internal and external mutability.

struct SplitIterator<'a> {
    s: &'a str,
    delimit: u8,
    idx: usize,
}

impl<'a> Iterator for SplitIterator<'a> {
    type Item = &'a str;
    // fn next(&mut self) -> Option<&'a Tree<T>> {
    fn next(&mut self) -> Option<&'a str> {
        let bytes = self.s.as_bytes();
        if self.idx > bytes.len() {
            return None;
        }
        let start = self.idx;
        for i in self.idx..bytes.len() {
            if bytes[i] == self.delimit {
                self.idx = i + 1;
                // assume that we're going to have good UTF-8,
                // (even though technically we can't
                return Some(std::str::from_utf8(&bytes[start..i]).unwrap());
            }
        }
        self.idx = bytes.len() + 1;
        return Some(std::str::from_utf8(&bytes[start..bytes.len()]).unwrap());
    }
}

fn string_split(s: &str, delimit: u8) -> SplitIterator {
    return SplitIterator {
               s: s,
               delimit: delimit,
               idx: 0,
           };
}

fn lcs<T: PartialOrd>(seq: &Vec<T>) -> Vec<&T> {
    let seq_len = seq.len();
    let mut prev: Vec<usize> = Vec::with_capacity(seq_len);
    // heads[N] heads of active subsequences of length N.
    // seq[heads[0]], seq[heads[1]], ... is an increasing seq.
    let mut heads: Vec<usize> = Vec::with_capacity(0);
    let mut len = 0;
    heads.push(0); // dummy element.
    for i in 0..seq_len {
        let ref current = seq[i];
        let mut lo = 1;
        let mut hi = len;
        while lo <= hi {
            let mid = (lo + hi + 1) / 2;
            let head = heads[mid]; // head of lcs with length 'mid'
            if seq[head] < *current {
                // discard shorter sequences, as current may be appendable to a longer sequence.
                lo = mid + 1;
            } else {
                // discard longer sequencess, current cannot be appended to seq[head].
                hi = mid - 1;
            }
        }
        let new_length = lo;
        prev.push(heads[new_length - 1]);
        if new_length < heads.len() {
            heads[new_length] = i;
        } else {
            assert![new_length == heads.len()];
            heads.push(i);
        }
        if new_length > len {
            len = new_length;
        }
    }
    let mut k = heads[len];
    let mut result = Vec::with_capacity(len);
    for _ in 0..len {
        result.push(&seq[k]);
        k = prev[k];
    }
    result.reverse();
    return result;
}

fn main() {
    circular_palindrome::main();
}

#[allow(unused_variables)]
#[cfg(test)]
mod tests {
    use std::mem;
    fn to_ref<'a, T>(v: &'a Vec<T>) -> Vec<&'a T> {
        let mut result = Vec::with_capacity(v.len());
        for item in v {
            result.push(item);
        }
        return result;
    }

    #[test]
    fn lcs_1() {
        let s = vec![1, 2];
        let result = vec![1, 2];
        assert_eq!(::lcs(&s), to_ref(&result));
    }

    #[test]
    fn lcs_2() {
        let s = vec![1, 2, 3, 11, 12, 4, 5, 13, 14];
        let result = vec![1, 2, 3, 4, 5, 13, 14];
        assert_eq!(::lcs(&s), to_ref(&result));
    }
    #[test]
    fn lcs_3() {
        let s = vec![5, 4, 3, 2, 1, 1];
        let result = vec![1];
        assert_eq!(::lcs(&s), to_ref(&result));
    }

    #[test]
    fn test_iterator() {
        let s = "foo,bar,,baz,";
        let mut iter = ::string_split(s, 44);
        assert_eq!(iter.next(), Some("foo"));
        assert_eq!(iter.next(), Some("bar"));
        assert_eq!(iter.next(), Some(""));
        assert_eq!(iter.next(), Some("baz"));
        assert_eq!(iter.next(), Some(""));
        assert_eq!(iter.next(), None);
    }
    #[test]
    fn test_intersection_lifetime() {
        fn x_or_y<'a>(x: &'a i32, y: &'a i32) -> &'a i32 {
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
                let tmp = XY { x, y };
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
            x: i32,
        }
        struct RefField<'a> {
            x: &'a i32,
        }
        struct RefFieldMultiple<'a> {
            x: &'a i16,
            y: &'a i32,
            z: &'a i64,
        }
        enum EnumA {
            A,
            B,
            C,
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
        assert_eq!(mem::size_of::<Box<i64>>(), 8);
        assert_eq!(mem::size_of::<usize>(), 8);

        // Magical - options of boxes are 8 bytes.
        assert_eq!(mem::size_of::<Box<()>>(), 8);
        assert_eq!(mem::size_of::<Option<Box<()>>>(), 8);
        assert_eq!(mem::size_of::<usize>(), 8);
        assert_eq!(mem::size_of::<Option<usize>>(), 16);
    }

    #[test]
    fn test_specialization() {
        /*
        struct Special {
            v: usize,
        }

        trait MyTrait {
            fn size(&self) -> usize;
        }
        impl<T> MyTrait for T {
            default fn size(&self) -> usize {
                return 0;
            }
        }

        impl MyTrait for Special {
            fn size(&self) -> usize {
                return self.v;
            }
        }
        assert_eq!(3.size(), 0);
        assert_eq!(Special { v: 5 }.size(), 5);
        */
    }

    #[test]
    fn test_drop() {
        // test dropping.
        use std::cell::Cell;
        use std::rc::Rc;

        struct Sample {
            v: Rc<Cell<isize>>,
        }
        impl Sample {
            fn new(v: &Rc<Cell<isize>>) -> Sample {
                v.set(v.get() + 1);
                return Sample { v: v.clone() };
            }
        }
        impl Drop for Sample {
            fn drop(&mut self) {
                self.v.set(self.v.get() - 1);
            }
        }

        let rcv = Rc::new(Cell::new(0));

        assert_eq!(rcv.get(), 0);
        let s1 = Sample::new(&rcv);
        assert_eq!(rcv.get(), 1);
        drop(s1);
        assert_eq!(rcv.get(), 0);
        {
            let s2 = Sample::new(&rcv);
            assert_eq!(rcv.get(), 1);
        }
        // automatically dropped
        assert_eq!(rcv.get(), 0);
    }

    #[test]
    fn test_borrow_trait() {
        use std::borrow::Borrow;
        // this works against borrows and non-borrows.
        fn increment_1<T: Borrow<isize>>(b: T) -> isize {
            return b.borrow() + 1;
        }
        // only works with concrete types.
        fn increment_2(b: isize) -> isize {
            return b + 1;
        }
        let zero = 0;
        assert_eq!(1, increment_1(zero));
        assert_eq!(1, increment_2(zero));
        assert_eq!(1, increment_1(&zero));
        // assert_eq!(1, increment_2(&zero)); // does not compile
        // but this compiles!
        assert_eq!(1, increment_2((&zero).clone()));
    }

    #[test]
    fn test_mutablity() {
        struct A {
            x: usize,
        }
        struct B {
            a: A,
        }
        let a = A { x: 3 };
        // DOES NOT COMPILE - a.x = 5;
        let mut b = B { a: a };
        b.a.x = 5; // But this compiles - MAGICAL.
        assert_eq!(b.a.x, 5);
    }

    #[test]
    fn test_conversions_options() {
        // testing various different conversions.
        let mut x = Some(3);
        assert_eq!(x.unwrap(), 3);
        x = Some(5); // this is okay, as x is mutable.
        assert_eq!(x.unwrap(), 5);
        // pushing mutablity inside the option.
        match x.as_mut() {
            None => (),
            Some(y) => {
                *y = 7;
            }
        }
        assert_eq!(x.unwrap(), 7);
        // the above is equivalent to...
        match x {
            None => (),
            Some(ref mut y) => {
                *y = 9;
            }
        }
        assert_eq!(x.unwrap(), 9);
    }

    #[test]
    fn test_mutable_boxes() {
        // mutable boxes are a bit weird, because it means
        // - i can change the point-ED value
        // - i can change the pointer - i.e. allocate and point to a new value.
        // ... but because of ownership, (1) and (2) are indistinguishable
        // from outside.
        // ... unless you keep a pointer.
        let mut x = Box::new(0usize);
        let ptr_1 = x.as_mut() as *const usize; // pointer, used for testing.
        assert_eq!(*x, 0);
        unsafe {
            assert_eq!(*ptr_1, 0);
        }
        x = Box::new(1); // ptr_1 is invalid at this point.
        let ptr_2 = x.as_mut() as *mut usize;
        assert_ne!(ptr_1, ptr_2); // two pointers are different.
        assert_eq!(*x, 1);
        unsafe {
            *ptr_2 = 2;
        }
        // yay pointer.
        assert_eq!(*x, 2);
    }

    #[test]
    fn test_conversions_boxes() {
        use std;
        let mut x = Box::new(0);
        let mut y = Box::new(4);
        let ptr = x.as_mut() as *const usize; // pointer, used for testing.
        assert_eq!(*x, 0);
        *x = 1; // you can use boxes via asref / asmut
        unsafe {
            assert_eq!(*ptr, 1);
        }
        assert_eq!(*x, 1);
        *x.as_mut() = 2; // ...that's a lvalue?
        assert_eq!(*x.as_ref(), 2); // more complete syntax
        {
            let x_ref: &mut usize = x.as_mut();
            *x_ref = 3;
            // x_ref = y.as_mut(); // does not compile.
        }
        assert_eq!(*x, 3);
        {
            let mut tmp: &usize = x.as_ref();
            // let mut - means that the variable can be changed.
            assert_eq!(*tmp, 3);
            tmp = y.as_ref();
            assert_eq!(*tmp, 4);
            assert_eq!(*x, 3); // x does not change.
        }
        {
            // double mutability - can change the reference AND referenced.
            let mut tmp: &mut usize = x.as_mut();
            *tmp = 5; // changes x.
            tmp = y.as_mut();
            *tmp = 6; // changes y.
        }
        assert_eq!(*x, 5);
        assert_eq!(*y, 6);
        fn increment_1(var: &mut Box<usize>) {
            // requires double star.
            // &mut Box<T> -> mut Box<T> -> &mut T
            **var = **var + 1;
            // note that this is magical, and somewhat "atomic". we cannot unwrap this to
            // multiple single-star operation.
            // &mut Box<T> -> mut Box<T>
            // would require ownership change.
        }
        fn increment_2(var: &mut Box<usize>) {
            // performs &Box<T> -> &T - i.e. removes the Box<_> in the middle.
            let var_unwrapped: &mut usize = var.as_mut();
            *var_unwrapped = *var_unwrapped + 1;
        }
        fn increment_3(var: &mut std::ops::DerefMut<Target = usize>) {
            // same thing as above, but using the magic '&**' / '&mut **' operator.
            // the above means:
            //   given a reference to a dereferenciable type,
            //   dereference the inner type and return the reference to it.
            // almost like A(B(C)) -> A(C)
            // but more of &X<T> -> X<T>
            let var_unwrapped: &mut usize = &mut **var;
            *var_unwrapped = *var_unwrapped + 1;
        }
        increment_1(&mut x);
        assert_eq!(*x, 6);
        increment_2(&mut x);
        assert_eq!(*x, 7);
        increment_3(&mut x);
        assert_eq!(*x, 8);
    }

    #[test]
    fn test_reproduce() {
        /*
        struct Node {
            value: usize,
            next: Option<Box<Node>>,
        }
        fn recursive<'a>(link: &'a mut Option<Box<Node>>) -> Option<&'a mut Option<Box<Node>>> {
            {
                match link.as_mut() {
                    None => (),
                    Some(cur) => {
                        // let unwrapped = link.as_mut().unwrap();
                        let next: &mut Option<Box<Node>> = &mut cur.next;
                        // return recursive(next);
                        if next.is_some() {
                            return Some(next);
                            // return None;
                        }
                    }
                };
            }
            return Some(link);
        }
        */
    }
}
