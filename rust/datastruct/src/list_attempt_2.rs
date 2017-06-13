use std::mem;

pub struct List<T> {
    head: Link<T>,
}

type Link<T> = Option<Box<Node<T>>>;

struct Node<T> {
    elem: T,
    next: Link<T>,
}

impl<T> Drop for List<T> {
    fn drop(&mut self) {
        let mut cur_link = mem::replace(&mut self.head, None);
        while let Some(mut boxed_node) = cur_link {
            // at this point, ownership of cur_link is lost.
            // we cannot do Some(ref ...), otherwise we'll still have
            // the ownership of cur_link.
            cur_link = mem::replace(&mut boxed_node.next, None);
            // the above line works because we don't own cur_link.
            // in fact, if we comment out the above line it will NOT work
            // as future iterations of the loop will work against unowned
            // cur_link.
        }
    }
}

impl<T> List<T> {
    fn new() -> List<T> {
        List { head: None }
    }

    fn len(&self) -> usize {
        let mut link = &self.head;
        let mut count = 0;
        while let Some(ref node) = *link {
            count += 1;
            link = &node.next;
        }
        return count;
    }

    fn push(&mut self, t: T) {
        let prev_head = mem::replace(&mut self.head, None);
        self.head = Some(Box::new(Node {
                                      elem: t,
                                      next: prev_head,
                                  }));
    }

    fn pop(&mut self) -> Option<T> {
        match mem::replace(&mut self.head, None) {
            // no need to "ref", as we have the full ownership of the object
            // after mem::replace.
            None => None,
            Some(boxed_node) => {
                let node = *boxed_node;
                self.head = node.next;
                Some(node.elem)
            }
        }
    }

    fn iter<'a>(&'a self) -> ListIter<'a, T> {
        return ListIter { head: &self.head };
    }

    fn into_iter(self) -> ListIntoIter<T> {
        // return ListIntoIter { head: self.head }
        // sadly, the above implemention does _not_ work,
        // as it forces us deconstructing self which
        // implements Drop.
        // we could've accepted mutable reference, but there's no need, because...

        // instead, we need to pass in the entire thing.
        // note that even though self is not mutable, since
        // iterator is going to be mutable everything is going to
        // be mutable!
        // O.M.G.
        return ListIntoIter { list: self };
    }

    fn iter_mut<'a>(&'a mut self) -> ListIterMut<'a, T> {
        // note:
        // type Link<T> = Option<Box<Node<T>>>
        // and we want to convert to
        // Option<&'a mut Node<T>>

        // some code to show how type inferencing works
        drop::<&Option<Box<Node<T>>>>(&self.head);
        // self.head <-> (&mut self.head)
        // BUT WHY do i need ampersand in the above line?
        // anyways, as_mut() moved mutability from outside to inside the Option<_>
        drop::<Option<&mut Box<Node<T>>>>((&mut self.head).as_mut());
        // the second as_mut() (inside .map) unwrapped the Box<_>
        // and moved the mutability.
        drop::<Option<&mut Node<T>>>(self.head.as_mut().map(|x| x.as_mut()));
        return ListIterMut { head: self.head.as_mut().map(|x| x.as_mut()) };
    }
}

struct ListIter<'a, T: 'a> {
    head: &'a Link<T>,
}

struct ListIterMut<'a, T: 'a> {
    // Why is this useful?
    // - unboxed.
    // - can return mutable reference to node's inner value.
    head: Option<&'a mut Node<T>>,
}

struct ListIntoIter<T> {
    list: List<T>,
}

impl<'a, T> Iterator for ListIter<'a, T> {
    type Item = &'a T;
    fn next(&mut self) -> Option<Self::Item> {
        match *self.head {
            // ownership of object referenced by self.head is taken,
            // but we can modify self.head (the reference).
            None => None,
            Some(ref boxed_node) => {
                let elem = &boxed_node.elem;
                self.head = &boxed_node.next;
                Some(elem)
            }
        }
    }
}

impl<'a, T> Iterator for ListIterMut<'a, T> {
    type Item = &'a mut T;
    fn next(&mut self) -> Option<Self::Item> {
        // this line is needed because we want to modify self.head
        // in the match statement.
        // we'll cause a borrow error.
        let cur: Option<&'a mut Node<T>> = self.head.take();
        match cur {
            None => None,
            Some(mut node) => {
                // no ref here, i am consuming cur.
                // if we take ref instead, then we cannot return node.elem.
                self.head = node.next.as_mut().map(|x| x.as_mut());
                Some(&mut node.elem)
            }
        }
    }
}

impl<T> Iterator for ListIntoIter<T> {
    type Item = T;
    fn next(&mut self) -> Option<Self::Item> {
        // mutability of List<T> is inherited via
        // mutability of self - thus we can just "pop" it.
        // yay!
        return self.list.pop();
    }
}

#[cfg(test)]
mod tests {
    use list_attempt_2::List;

    #[test]
    fn test_simple() {
        let mut n = List::new();
        assert_eq!(n.len(), 0);
        n.push(1);
        assert_eq!(n.len(), 1);
        assert_eq!(n.pop(), Some(1));
        assert_eq!(n.len(), 0);
        assert_eq!(n.pop(), None);
        n.push(1);
        n.push(2);
        n.push(3);
        let mut iter = n.iter();
        assert_eq!(*iter.next().unwrap(), 3);
        assert_eq!(*iter.next().unwrap(), 2);
        assert_eq!(*iter.next().unwrap(), 1);
        assert_eq!(iter.next(), None);
    }

    #[test]
    fn test_different_iterators() {
        let mut l = List::new();
        l.push(1);
        l.push(2);
        l.push(3);
        assert_eq!(l.iter().map(|x| *x).collect::<Vec<_>>(), vec![3, 2, 1]);
        for x in l.iter_mut() {
            *x = *x + 1;
        }
        assert_eq!(l.iter().map(|x| *x).collect::<Vec<_>>(), vec![4, 3, 2]);
        assert_eq!(l.into_iter().collect::<Vec<_>>(), vec![4, 3, 2]);
        // cannot use 'l' anymore.
    }

    #[test]
    fn test_stack_overflow() {
        let mut n = List::new();
        for _ in 0..100000 {
            n.push(1);
        }
        assert_eq!(n.len(), 100000);
        drop(n);
    }
}
