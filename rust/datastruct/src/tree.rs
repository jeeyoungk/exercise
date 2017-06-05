use std::cmp::max;
use std::mem;
use std::iter::Map;
use std::ops::FnMut;
use std::fmt;

#[derive(Debug)]
enum Tree<T: Ord + Copy> {
    Leaf,
    Node {
        v: T,
        left: Box<Tree<T>>,
        right: Box<Tree<T>>,
        cached_depth: usize,
    },
}

/*
impl fmt::Display for Structure {
    // This trait requires `fmt` with this exact signature.
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        // Write strictly the first element into the supplied output
        // stream: `f`. Returns `fmt::Result` which indicates whether the
        // operation succeeded or failed. Note that `write!` uses syntax which
        // is very similar to `println!`.
        write!(f, "{}", self.0)
    }
}
*/
impl<T: Ord + Copy + fmt::Debug> fmt::Display for Tree<T> {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        return self.fmt_indent(f, 0);
    }
}

// value or reference to a tree - used for navigation.
enum VorT<'a, S: Ord + Copy + 'a> {
    V(&'a Tree<S>),
    T(&'a Tree<S>),
}

struct TreeIterator<'a, T: Ord + Copy + 'a> {
    stack: Vec<VorT<'a, T>>,
}

struct AppendResult {
    depth_changed: bool,
}

impl<'a, T: Ord + Copy + fmt::Debug> Iterator for TreeIterator<'a, T> {
    type Item = &'a Tree<T>;
    fn next(&mut self) -> Option<&'a Tree<T>> {
        let maybe_append = |stack: &mut Vec<_>, tree: &'a Box<Tree<T>>| {
            // maybe append to the stack if the value is not a leaf node.
            let t = tree.as_ref();
            match *t {
                Tree::Leaf => (),
                ref x => stack.push(VorT::T(t)),
            }
        };
        while self.stack.len() > 0 {
            let vort = self.stack.pop().unwrap();
            match vort {
                VorT::V(x) => {
                    match *x {
                        Tree::Leaf => panic!("Should not traverse leaf nodes"),
                        Tree::Node { .. } => return Some(x),
                    }
                }
                VorT::T(x) => {
                    match *x { // trees are additionally traversed.
                        Tree::Leaf => panic!("Should not traverse leaf nodes"),
                        Tree::Node {
                            v,
                            ref left,
                            ref right,
                            ..
                        } => {
                            // performs in-order traversal.
                            maybe_append(&mut self.stack, right);
                            self.stack.push(VorT::V(x));
                            maybe_append(&mut self.stack, left);
                        }
                    }
                }
            }
        }
        return None;
    }
}

impl<T: Ord + Copy + fmt::Debug> Tree<T> {
    fn append(&mut self, v: T) -> AppendResult {
        // consumes self, and creates a new list.
        let old_depth = self.depth();
        let result: AppendResult = match *self {
            Tree::Leaf => {
                *self = Tree::Node {
                    v,
                    left: Box::new(Tree::Leaf),
                    right: Box::new(Tree::Leaf),
                    cached_depth: 1,
                };
                return AppendResult { depth_changed: true }; // no need to recompute for new nodes.
            }
            Tree::Node {
                v: nv,
                ref mut left,
                ref mut right,
                ..
            } => {
                if v == nv {
                    AppendResult { depth_changed: false }
                } else {
                    let result: AppendResult = if v < nv {
                        left.append(v)
                    } else {
                        right.append(v)
                    };
                    result
                }
            }
        };
        if result.depth_changed {
            /*
            let (left_depth, right_depth) = match *self {
                Tree::Leaf => panic!("No leaf node"),
                Tree::Node {
                    ref left,
                    ref right,
                    ..
                } => (left.depth(), right.depth()),
            };
            let delta = left_depth as i32 - right_depth as i32;
            if delta == 2 {
                self.rotate_right(); // need to call this at the end.
                self.check_depth();
            } else if delta == -2 {
                self.rotate_left(); // need to call this at the end.
                self.check_depth();
            } else if delta == 0 || delta == -1 || delta == 1 {
                // do nothing
            } else {
                panic!("Depth difference cannot be more than 2, got {}\n{}",
                       delta,
                       &self);
            }
            */
        }
        self.recompute(result);
        return AppendResult { depth_changed: old_depth != self.depth() };
    }

    fn check_depth(&mut self) {
        let (left_depth, right_depth) = match *self {
            Tree::Leaf => panic!("No leaf node"),
            Tree::Node {
                ref left,
                ref right,
                ..
            } => (left.depth(), right.depth()),
        };
        let delta = left_depth as i32 - right_depth as i32;
        assert!(delta >= -2, "delta={} tree=\n{}", delta, self);
        if delta == 3 {
            self.rotate_right();
        };
        assert!(delta <= 2, "delta={} tree=\n{}", delta, self);
    }

    fn recompute(&mut self, prev_result: AppendResult) -> AppendResult {
        // recompute - and see if the depth has changed.
        let depth_changed = if prev_result.depth_changed {
            self.recompute_depth()
        } else {
            false
        };
        return AppendResult { depth_changed };
    }

    fn recompute_depth(&mut self) -> bool {
        match *self {
            Tree::Node {
                ref left,
                ref right,
                ref mut cached_depth,
                ..
            } => {
                let new_depth = 1 + max(left.depth(), right.depth());
                if new_depth != *cached_depth {
                    *cached_depth = new_depth;
                    true
                } else {
                    false
                }
            }
            _ => panic!("Leaf nodes cannot be recomputed"),
        }
    }

    fn value(&self) -> Option<T> {
        match *self {
            Tree::Leaf => None,
            Tree::Node { v, .. } => Some(v),
        }
    }

    fn left(&self) -> &Tree<T> {
        match *self {
            Tree::Leaf => self,
            Tree::Node { ref left, .. } => left,
        }
    }

    fn right(&self) -> &Tree<T> {
        match *self {
            Tree::Leaf => self,
            Tree::Node { ref right, .. } => right,
        }
    }

    fn iter<'a>(&'a self) -> TreeIterator<'a, T> {
        TreeIterator { stack: vec![VorT::T(self)] }
    }

    fn len(&self) -> usize {
        match *self {
            Tree::Leaf => 0,
            Tree::Node {
                ref left,
                ref right,
                ..
            } => 1 + left.len() + right.len(),
        }
    }

    fn depth(&self) -> usize {
        match *self {
            Tree::Leaf => 0,
            Tree::Node { cached_depth, .. } => cached_depth,
        }
    }

    fn raw_depth(&self) -> usize {
        // returns un-cached depth - used for testing.
        match *self {
            Tree::Leaf => 0,
            Tree::Node {
                ref left,
                ref right,
                ..
            } => 1 + max(left.raw_depth(), right.raw_depth()),
        }
    }

    fn rotate_left(&mut self) {
        // converting
        //   X
        // a   Y
        //    b c
        // to
        //    Y
        //  X   c
        // a b
        // println!("rotate_left [{:?}]\n{}", self.value().unwrap(), self);
        match *self {
            Tree::Leaf => panic!("cannot rotate"), // cannot rotate leaf node.
            Tree::Node {
                v: ref mut x_val,
                left: ref mut a,
                right: ref mut y,
                ..
            } => {
                let is_y_node = match *y.as_ref() {
                    Tree::Leaf => false, // cannot rotate when y is leaf node.
                    Tree::Node { .. } => true,
                };
                if !is_y_node {
                    panic!("cannot rotate")
                }
                mem::swap(a, y);
                let new_a = y;
                let new_y = a;
                match *new_y.as_mut() {
                    Tree::Node {
                        v: ref mut y_val,
                        left: ref mut b,
                        right: ref mut c,
                        ..
                    } => {
                        mem::swap(x_val, y_val);
                        mem::swap(b, c);
                        mem::swap(b, new_a);
                    }
                    _ => panic!("Cannot be leaf"),
                };
                new_y.recompute_depth();
            }
        }
        self.recompute_depth();
    }

    fn rotate_right(&mut self) {
        // reverse of rotate_left
        //println!("rotate_right [{:?}]\n{}", self.value().unwrap(), self);
        match *self {
            Tree::Leaf => panic!("cannot rotate"),
            Tree::Node {
                v: ref mut y_val,
                left: ref mut x,
                right: ref mut c,
                ..
            } => {
                let is_x_node = match *x.as_ref() {
                    Tree::Leaf => false,
                    Tree::Node { .. } => true,
                };
                if !is_x_node {
                    panic!("cannot rotate")
                }
                mem::swap(x, c);
                let new_x = c;
                let new_c = x;
                match *new_x.as_mut() {
                    Tree::Node {
                        v: ref mut x_val,
                        left: ref mut a,
                        right: ref mut b,
                        ..
                    } => {
                        mem::swap(x_val, y_val);
                        mem::swap(a, b);
                        mem::swap(b, new_c);
                    }
                    _ => panic!("Cannot be leaf"),
                }
                new_x.recompute_depth();
            }
        }
        self.recompute_depth();
    }

    fn find(&self, v: T) -> bool {
        match *self {
            Tree::Leaf => false,
            Tree::Node {
                v: nv,
                ref left,
                ref right,
                ..
            } => {
                if v < nv {
                    left.find(v)
                } else if nv < v {
                    right.find(v)
                } else {
                    true
                }
            }
        }
    }

    fn fmt_indent(&self, f: &mut fmt::Formatter, indent: usize) -> fmt::Result
        where T: fmt::Debug
    {
        match *self {
            Tree::Leaf => {
                write!(f, "{}_\n", "|".repeat(indent));
            }
            Tree::Node {
                v,
                ref left,
                ref right,
                ..
            } => {
                write!(f, "{}{:?}\n", "|".repeat(indent), v);
                left.fmt_indent(f, indent + 1);
                right.fmt_indent(f, indent + 1);
            }
        }
        return write!(f, "");
    }
}

#[cfg(test)]
mod tests {
    use tree::Tree;
    use std::fmt;

    fn verify_depty<T: Ord + Copy + fmt::Debug>(t: &Tree<T>) {
        let iter = t.iter();
        for t in iter {
            assert_eq!(t.depth(), t.raw_depth());
        }
    }

    #[test]
    fn test_tree() {
        let mut t = Tree::Leaf::<i32>;
        for x in 1..10 {
            t.append(x);
            assert_eq!(t.len(), x as usize);
            // assert_eq!(t.depth(), x as usize);
            assert!(t.find(x));
            assert!(!t.find(x + 1));
        }
        verify_depty(&t);
    }

    #[test]
    fn test_tree_balanced() {
        let mut t = Tree::Leaf::<i32>;
        t.append(2);
        assert_eq!(t.depth(), 1);
        t.append(3);
        assert_eq!(t.depth(), 2);
        t.append(1);
        assert_eq!(t.len(), 3);
        assert_eq!(t.depth(), 2);
        let v: Vec<i32> = t.iter().map(|x| x.value().unwrap()).collect();
        assert_eq!(vec![1, 2, 3], v);
        let iter = t.iter();
        // t.append(4); // does not compile - iterator is still alive.
        verify_depty(&t);
    }

    #[test]
    fn test_tree_rotate() {
        let mut t = Tree::Leaf::<i32>;
        t.append(10); // original root
        t.append(5);
        t.append(20); // new root
        t.append(15);
        t.append(25);
        assert_eq!(t.value(), Some(10));
        t.rotate_left();
        verify_depty(&t);
        assert_eq!(t.value(), Some(20));
        assert_eq!(t.left().value(), Some(10));
        assert_eq!(t.left().left().value(), Some(5));
        assert_eq!(t.left().right().value(), Some(15));
        assert_eq!(t.right().value(), Some(25));
        let v: Vec<i32> = t.iter().map(|x| x.value().unwrap()).collect();
        assert_eq!(vec![5, 10, 15, 20, 25], v);
        t.rotate_right();
        verify_depty(&t);
        assert_eq!(t.value(), Some(10));
        assert_eq!(t.left().value(), Some(5));
        assert_eq!(t.right().value(), Some(20));
        assert_eq!(t.right().left().value(), Some(15));
        assert_eq!(t.right().right().value(), Some(25));
        let v: Vec<i32> = t.iter().map(|x| x.value().unwrap()).collect();
        assert_eq!(vec![5, 10, 15, 20, 25], v);
    }

    #[test]
    fn test_random_tree() {
        use rand::{SeedableRng, StdRng, Rng};
        let seed: &[_] = &[1, 2, 3, 4];
        let mut rng: StdRng = SeedableRng::from_seed(seed);
        let mut t = Tree::Leaf::<u32>;
        for x in 1..100 {
            let v = rng.gen::<u32>();
            t.append(v);
            assert_eq!(t.len(), x);
        }
        assert_eq!(t.depth(), 13);
        verify_depty(&t);
        let v: Vec<_> = t.iter().map(|x| x.value().unwrap()).collect();
        assert_eq!(vec![75549367, 103053517, 157650975, 203616506, 226504616, 243216458,
                        248794081, 251732419, 300437459, 335380164, 342577264, 367011760,
                        374644985, 413093421, 498698172, 637344937, 652361274, 664864318,
                        724515175, 733242882, 820718454, 839642200, 876161274, 940243002,
                        1138038293, 1177224828, 1209374189, 1217959109, 1261517434, 1297596776,
                        1328103458, 1359117399, 1385243153, 1476030984, 1489050478, 1495650166,
                        1534300197, 1555071402, 1558432965, 1624325784, 1624732341, 1674534844,
                        1725340658, 1747619289, 1759762385, 1785865466, 1796945992, 1847359564,
                        1905941133, 1916480447, 1930352495, 1958716676, 1977298456, 2002306141,
                        2051352252, 2105877633, 2122373754, 2152134042, 2172665463, 2331331168,
                        2408553372, 2463920895, 2557924481, 2562717490, 2605252975, 2682864714,
                        2807458044, 2844724439, 3010536724, 3041046859, 3112756344, 3190684552,
                        3214958374, 3263723615, 3282254227, 3283235778, 3324486977, 3372320223,
                        3379484849, 3395625218, 3423674611, 3503054875, 3512225330, 3562663178,
                        3724157157, 3794564476, 3795010240, 3820911753, 3866819168, 3874167136,
                        4020181860, 4027885666, 4063100626, 4073414544, 4090189466, 4102334411,
                        4203006323, 4235359872, 4243950918],
                   v);
    }

    #[test]
    fn test_random_shuffle() {
        use rand::{SeedableRng, StdRng, Rng};
        let seed: &[_] = &[1, 2, 3, 4];
        let mut rng: StdRng = SeedableRng::from_seed(seed);
        let mut t = Tree::Leaf::<u32>;
        let mut shuffled = (1..100).collect::<Vec<u32>>();
        rng.shuffle(&mut shuffled);
        for i in &shuffled {
            t.append(*i);
        }
        verify_depty(&t);
        let v: Vec<_> = t.iter().map(|x| x.value().unwrap()).collect();
        assert_eq!(v, (1..100).collect::<Vec<u32>>());
    }
}
