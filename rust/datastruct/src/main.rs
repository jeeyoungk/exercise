mod columnar;
mod linkedlist;
mod cell;
mod tree;
mod kmeans;
mod complex;

extern crate rand;

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

fn string_split<'a>(s: &'a str, delimit: u8) -> SplitIterator {
    return SplitIterator {
               s: s,
               delimit: delimit,
               idx: 0,
           };
}

fn lcs<'a, T: PartialOrd>(seq: &'a Vec<T>) -> Vec<&'a T> {
    let seq_len = seq.len();
    let mut prev: Vec<usize> = Vec::with_capacity(seq_len);
    // heads[N] heads of active subsequences 
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
                lo = mid + 1; // discard shorter sequences, as current may be appendable to a longer sequence.
            } else {
                hi = mid - 1; // discard longer sequencess, current cannot be appended to seq[head].
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
    println!("Hello, world!");
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
    }
}
