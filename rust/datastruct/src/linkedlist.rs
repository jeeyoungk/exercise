// linked list implementation in rust
use std::rc::Rc;

// implementing linked list via pointers.
#[derive(Debug)]
enum Node<T> {
    Empty,
    Elem(T, Box<Node<T>>),
}

struct NodeIterator<'a, T: 'a> {
    // whenever a struct has a reference as its field
    // it needs a lifetime specifier.
    node: &'a Node<T>,
}

// implementing linked list via reference counting pointers.
#[derive(Debug, Clone)]
enum RCNode<T: Copy> {
    // in order to have multiple reference, we either need:
    // T:Copy, or
    // Box<T>
    // For this case, I chose to use T:Copy.
    Empty,
    Elem(T, Rc<RCNode<T>>),
}

struct RCNodeIterator<'a, T: 'a + Copy> {
    node: &'a RCNode<T>,
}

impl<'a, T> Iterator for NodeIterator<'a, T> {
    type Item = &'a T;

    fn next(&mut self) -> Option<&'a T> {
        match *self.node {
            Node::Empty => None,
            Node::Elem(ref v, ref next) => {
                self.node = &**next;
                Some(v)
            }
        }
    }
}

impl<'a, T: Copy> Iterator for RCNodeIterator<'a, T> {
    type Item = &'a T;
    fn next(&mut self) -> Option<&'a T> {
        match *self.node {
            RCNode::Empty => None,
            RCNode::Elem(ref v, ref next) => {
                self.node = &**next;
                Some(v)
            }
        }
    }
}

impl<T> Node<T> {
    pub fn len(&self) -> usize {
        match self {
            &Node::Empty => 0,
            &Node::Elem(_, ref boxed) => 1 + boxed.len(),
        }
    }

    pub fn prepend(self, v: T) -> Node<T> {
        // TODO - this one does not borrow self.
        return Node::Elem(v, Box::new(self));
    }

    pub fn iter<'a>(&'a self) -> NodeIterator<'a, T> {
        // lifetime needs to be propagated.
        return NodeIterator { node: self };
    }
}

impl<T: Copy> RCNode<T> {
    pub fn len(&self) -> usize {
        match self {
            &RCNode::Empty => 0,
            &RCNode::Elem(_, ref rc) => 1 + rc.len(),
        }
    }

    pub fn prepend(&self, v: T) -> RCNode<T> {
        // notice "borrow" here.
        return RCNode::Elem(v, Rc::new(self.clone()));
    }

    pub fn iter<'a>(&'a self) -> RCNodeIterator<'a, T> {
        // lifetime needs to be propagated.
        return RCNodeIterator { node: self };
    }
}

#[cfg(test)]
mod tests {
    use linkedlist::Node;
    use linkedlist::RCNode;
    use std::rc::Rc;

    #[test]
    fn test_simple() {
        assert_eq!(Node::Empty::<i32>.len(), 0);
        assert_eq!(Node::Elem(32, Box::new(Node::Empty)).len(), 1);
        assert_eq!(Node::Elem(32, Box::new(Node::Elem(10, Box::new(Node::Empty)))).len(),
                   2);
    }

    #[test]
    fn test_rcnode_simple() {
        assert_eq!(RCNode::Empty::<i32>.len(), 0);
        assert_eq!(RCNode::Elem(1, Rc::new(RCNode::Empty)).len(), 1);
    }

    #[test]
    fn test_prepend() {
        let n1 = Node::Empty::<i32>;
        let n2 = n1.prepend(1);
        let n3 = n2.prepend(2);
        let n4 = n3.prepend(3);
        assert_eq!(n4.len(), 3);
        let mut it = n4.iter();
        assert_eq!(it.next(), Some(3).as_ref());
        assert_eq!(it.next(), Some(2).as_ref());
        assert_eq!(it.next(), Some(1).as_ref());
        assert_eq!(it.next(), None);
    }

    #[test]
    fn test_rcnode_prepend() {
        let n1 = RCNode::Empty;
        let n2 = n1.prepend(1);
        match n2 {
            RCNode::Elem(_, ref next) => assert_eq!(Rc::strong_count(next), 1),
            RCNode::Empty => assert!(false),
        }
        let n3 = n2.prepend(2);
        match n2 {
            RCNode::Elem(_, ref next) => assert_eq!(Rc::strong_count(next), 2),
            RCNode::Empty => assert!(false),
        }
        let n4 = n3.prepend(3);
        assert_eq!(n4.len(), 3);
        assert_eq!(n1.len(), 0); // this can be called as prepend does not move.
        let mut it = n4.iter();
        assert_eq!(it.next(), Some(3).as_ref());
        assert_eq!(it.next(), Some(2).as_ref());
        assert_eq!(it.next(), Some(1).as_ref());
        assert_eq!(it.next(), None);
    }

    #[test]
    fn test_build_iterative() {
        let mut n = Node::Empty;
        n = n.prepend(1);
        n = n.prepend(2);
        n = n.prepend(3);
        n = n.prepend(4);
        assert_eq!(n.len(), 4);
    }
}
