use std::mem;
use std::borrow::Borrow;
use std::cmp::Ordering;

type Link<T> = Option<Box<Node<T>>>;

#[derive(Debug)]
struct Node<T> {
    value: T,
    left: Link<T>,
    right: Link<T>,
}

struct Tree<T: PartialOrd> {
    root: Link<T>,
}

struct NodeIter<'a, T: PartialOrd + 'a> {
    // false - visiting
    // true - should emit value.
    stack: Vec<(bool, &'a Node<T>)>,
}

// enum for binary tree searching.
#[derive(Debug, Copy, Clone)]
enum FindStrategy {
    EQ, // search for equality
    GE, // search for smallest s.t. >=
    LE, // search for greatest s.t. <=
    LT, // search for smallest s.t. <
    GT, // search for smallest s.t. <
    PREV, // search for the parent node, or would-be parent node if it does not exist.
}

impl<T> Node<T> {
    fn new(value: T) -> Node<T> {
        Node {
            value: value,
            left: None,
            right: None,
        }
    }
}

impl<T> Node<T>
    where T: PartialOrd
{
    fn insert(&mut self, value: T) -> bool {
        let link;
        if value == self.value {
            return false; // value already exists.
        } else if value < self.value {
            link = &mut self.left;
        } else if value > self.value {
            link = &mut self.right;
        } else {
            panic!("Incorrect comparison.");
        }
        match *link {
            None => {
                *link = Some(Box::new(Node::new(value)));
                true
            }
            Some(ref mut next_ref) => next_ref.insert(value),
        }
    }

    fn find<'a>(&'a self, value: &T, strategy: FindStrategy) -> Option<&'a Node<T>> {
        let link;
        if *value == self.value {
            // equal value discovered.
            // either return a value, or recurse further (if it is LT or GT comparison).
            match strategy {
                FindStrategy::EQ | FindStrategy::GE | FindStrategy::LE => return Some(self),
                FindStrategy::LT => link = &self.left,
                FindStrategy::GT => link = &self.right,
                // for prev call, caller (the parent node) should return
                // itself when it sees a None result.
                FindStrategy::PREV => return None,
            }
        } else if *value < self.value {
            link = &self.left;
        } else if *value > self.value {
            link = &self.right;
        } else {
            panic!("Incorrect comparison.");
        }

        let result = match *link {
            None => None,
            Some(ref next_ref) => next_ref.find(value, strategy),
        };
        if result.is_some() {
            return result;
        }
        // recursion didn't yield anything.
        // for non-EQ comparison, we may provide the best answer.
        match strategy {
            FindStrategy::EQ => (),
            FindStrategy::GE | FindStrategy::GT => {
                if *value < self.value {
                    return Some(self);
                }
            }
            FindStrategy::LE | FindStrategy::LT => {
                if *value > self.value {
                    return Some(self);
                }
            }
            FindStrategy::PREV => {
                return Some(self);
            }
        };
        None
    }
}

impl<T: PartialOrd> Tree<T> {
    fn new() -> Tree<T> {
        return Tree { root: None };
    }

    pub fn insert(&mut self, value: T) -> bool {
        // let mut root = mem::replace(&mut self.root, None);
        match self.root {
            None => {
                self.root = Some(Box::new(Node::new(value)));
                true
            }
            Some(ref mut cur_ref) => cur_ref.insert(value),
        }
    }

    pub fn delete<V: Borrow<T>>(&mut self, value: V) -> Option<T> {
        let found = self.find_mut(value.borrow());
        found.map(|link| {
            let extracted: Link<T> = mem::replace(link, None);
            let has_left;
            let has_right;
            {
                // rust plz non-lexical scope.
                let node = extracted.as_ref().unwrap();
                has_left = node.left.is_some();
                has_right = node.right.is_some();
            };
            let Node { value, left, right } = *extracted.unwrap();
            if !has_left && has_right {
                mem::replace(link, right);
            } else if has_left && !has_right {
                mem::replace(link, left);
            } else if has_left && has_right {
                mem::replace(link, right);
                let new_parent = find_mut_link(link, &value, FindStrategy::PREV);
                *new_parent.unwrap() = left;
            }
            value
        })
    }

    pub fn contains<V: Borrow<T>>(&self, value: V) -> bool {
        return self.find_node(value, FindStrategy::EQ).is_some();
    }

    fn iter_node<'a>(&'a self) -> NodeIter<'a, T> {
        let mut stack = vec![];
        match self.root {
            None => (),
            Some(ref boxed_node) => {
                stack.push((false, boxed_node.as_ref()));
            }
        }
        NodeIter { stack: stack }
    }

    fn find_node<'a, V: Borrow<T>>(&'a self,
                                   value: V,
                                   strategy: FindStrategy)
                                   -> Option<&'a Node<T>> {
        match self.root {
            None => None,
            Some(ref root) => root.find(value.borrow(), strategy),
        }
    }

    // find a mutable reference to the given value.
    fn find_mut<'a, V: Borrow<T>>(&'a mut self, value: V) -> Option<&'a mut Link<T>> {
        if self.root.is_none() {
            None
        } else {
            // note - this function always returns Some(...)
            find_mut_link(&mut self.root, value.borrow(), FindStrategy::EQ)
        }
    }

    fn len(&self) -> usize {
        self.iter_node().count()
    }
}

impl<'a, T: PartialOrd + 'a> Iterator for NodeIter<'a, T> {
    type Item = &'a Node<T>;
    fn next(&mut self) -> Option<Self::Item> {
        while let Some((emit, node)) = self.stack.pop() {
            if emit {
                return Some(node);
            } else {
                node.right
                    .as_ref()
                    .map(|node| { self.stack.push((false, node)); });
                self.stack.push((true, node));
                node.left
                    .as_ref()
                    .map(|node| { self.stack.push((false, node)); });
            }
        }
        return None;
    }
}

fn find_mut_link<'a, T>(link_to_cur: &'a mut Link<T>,
                        value: &T,
                        strategy: FindStrategy)
                        -> Option<&'a mut Link<T>>
    where T: PartialOrd
{
    // find a mutable reference to a given node.
    // note - this function cannot return Some(None(...)).

    // i need to do a comparison via immutable reference first,
    // as we are interested in navigating into this structure
    // if the values are not equal.
    let ordering = match value.partial_cmp(&link_to_cur.as_ref().unwrap().value) {
        None => panic!("Incorrect Comparison."),
        Some(ordering) => ordering,
    };
    if ordering == Ordering::Equal {
        return Some(link_to_cur);
    }
    let cur = link_to_cur.as_mut().unwrap();
    let link: &mut Link<T> = match ordering {
        Ordering::Less => &mut cur.left,
        Ordering::Greater => &mut cur.right,
        _ => panic!("Incorrect comparison."),
    };
    match *link {
        Some(_) => return find_mut_link(link, value, strategy),
        None => {
            match strategy {
                FindStrategy::EQ => None,
                FindStrategy::PREV => Some(link),
                _ => panic!("Not implemented."),
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use tree_attempt_2::Tree;
    use tree_attempt_2::FindStrategy;
    use rand::{SeedableRng, StdRng, Rng};

    #[test]
    fn test_sample() {
        let mut t = Tree::new();
        assert_eq!(t.len(), 0);
        assert!(t.insert(1));
        assert_eq!(t.len(), 1);
        assert!(!t.insert(1));
        assert_eq!(t.len(), 1);
        t.insert(2);
        assert_eq!(t.len(), 2);
    }

    #[test]
    fn test_iterator() {
        let mut t = Tree::new();
        t.insert(1);
        t.insert(3);
        t.insert(2);
        assert_eq!(vec![1, 2, 3],
                   t.iter_node().map(|x| x.value.clone()).collect::<Vec<_>>());
    }

    #[test]
    fn test_delete() {
        let mut t = Tree::new();
        t.insert(10);
        t.insert(20);
        t.insert(25);
        t.insert(5);
        t.insert(2);

        assert_eq!(t.delete(2), Some(2));
        assert_eq!(t.len(), 4);
        t.insert(30);
        t.insert(40);
        t.insert(50);
        assert_eq!(t.delete(40), Some(40));
        assert_eq!(t.contains(40), false);
        assert_eq!(t.contains(50), true);
        assert_eq!(t.len(), 6);
        t.delete(10);
        assert_eq!(t.len(), 5);
    }

    #[test]
    fn test_delete_2() {
        let mut t = Tree::new();
        t.insert(2);
        t.insert(1);
        t.insert(3);
        t.delete(2);
        assert_eq!(vec![1, 3],
                   t.iter_node().map(|x| x.value.clone()).collect::<Vec<_>>());
    }

    #[test]
    fn test_random_tree_1000() {
        test_random(&[0, 0, 0, 0], 1000);
    }

    #[test]
    fn test_random_tree_100() {
        test_random(&[0, 0, 0, 0], 100);
    }

    fn test_random(seed: &[usize], size: usize) {
        let mut rng: StdRng = SeedableRng::from_seed(seed);
        let mut shuffled = (0..size).collect::<Vec<usize>>();
        let mut t = Tree::new();
        rng.shuffle(&mut shuffled);
        for (i, item) in shuffled.iter().enumerate() {
            assert_eq!(i, t.len());
            t.insert(*item);
        }
        for (i, item) in shuffled.iter().enumerate() {
            t.delete(item);
            assert_eq!(size - i - 1, t.len());
        }
    }

    #[test]
    fn test_find() {
        let mut t = Tree::new();
        t.insert(100);
        t.insert(50);
        t.insert(150);
        assert!(t.find_node(150, FindStrategy::EQ).is_some());
        assert!(t.find_node(101, FindStrategy::EQ).is_none());
        assert!(t.find_node(100, FindStrategy::EQ).is_some());
        assert!(t.find_node(99, FindStrategy::EQ).is_none());
        assert!(t.find_node(50, FindStrategy::EQ).is_some());
        // finding prev node
        assert_eq!(t.find_node(150, FindStrategy::PREV)
                       .map(|x| x.value.clone()),
                   Some(100));
        assert_eq!(t.find_node(50, FindStrategy::PREV).map(|x| x.value.clone()),
                   Some(100));
        assert_eq!(t.find_node(151, FindStrategy::PREV)
                       .map(|x| x.value.clone()),
                   Some(150));
        assert_eq!(t.find_node(49, FindStrategy::PREV).map(|x| x.value.clone()),
                   Some(50));
;
        assert_eq!(t.find_node(200, FindStrategy::LE).map(|x| x.value.clone()),
                   Some(150));
        assert_eq!(t.find_node(101, FindStrategy::GE).map(|x| x.value.clone()),
                   Some(150));
        assert_eq!(t.find_node(101, FindStrategy::LE).map(|x| x.value.clone()),
                   Some(100));
        assert_eq!(t.find_node(99, FindStrategy::GE).map(|x| x.value.clone()),
                   Some(100));
        assert_eq!(t.find_node(99, FindStrategy::LE).map(|x| x.value.clone()),
                   Some(50));
        assert_eq!(t.find_node(0, FindStrategy::GE).map(|x| x.value.clone()),
                   Some(50));

        assert_eq!(t.find_node(100, FindStrategy::LT).map(|x| x.value.clone()),
                   Some(50));
        assert_eq!(t.find_node(50, FindStrategy::LT).map(|x| x.value.clone()),
                   None);
        assert_eq!(t.find_node(100, FindStrategy::GT).map(|x| x.value.clone()),
                   Some(150));
        assert_eq!(t.find_node(150, FindStrategy::GT).map(|x| x.value.clone()),
                   None);
    }
}
