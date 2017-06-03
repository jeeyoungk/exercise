// implementation of columnar data format in rust

trait Column<T: Copy> {
    // size of the column
    fn len(&self) -> usize;

    // determine how to make this work :(
    // fn iterator<'a>(&self) -> ColumnIterator<T>;
}

trait ColumnIterator<T: Copy> {
    fn index(&self) -> usize;
    fn peek(&self) -> T;
    fn seek(&mut self, offset: usize);
}

// column backed by raw data
struct RawColumn<T> {
    elements: Vec<T>,
}

/*
struct BitColumn {
    elements: Vec<i64>,
    size: usize,
}
*/

#[derive(Copy, Clone)]
struct RawColumnIterator<'a, T: 'a> {
    column: &'a RawColumn<T>,
    index: usize,
}

impl<T: Copy> Column<T> for RawColumn<T> {
    fn len(&self) -> usize {
        return self.elements.len()
    }
}

impl<T> RawColumn<T> {
    pub fn new() -> RawColumn<T> {
        RawColumn {
            elements: Vec::new()
        }
    }

    fn iterator<'a>(&'a self) -> RawColumnIterator<T> {
        return RawColumnIterator {
            column: self,
            index: 0,
        }
    }
}


impl<'a, T: Copy> ColumnIterator<T> for RawColumnIterator<'a, T> {
    fn index(&self) -> usize {
        return self.index;
    }

    fn peek(&self) -> T {
        return self.column.elements[self.index]
    }

    fn seek(&mut self, offset: usize) {
        self.index = offset
    }
}


impl<'a, T: Copy> Iterator for RawColumnIterator<'a, T> {
    type Item = T;

    // next() is the only required method
    fn next(&mut self) -> Option<T> {
        if self.index < self.column.elements.len() {
            let v = self.column.elements[self.index];
            self.index += 1;
            return Some(v);
        }
        return None
    }
}

#[cfg(test)]
mod tests {
    use columnar::RawColumn;
    use columnar::ColumnIterator;

    #[test]
    fn column_sample() {
        let mut rc : RawColumn<i32> = RawColumn::new();
        rc.elements.push(1);
        rc.elements.push(2);
        rc.elements.push(3);
        let mut rci = rc.iterator();
        assert_eq!(rci.index(), 0);
        assert_eq!(rci.peek(), 1);
        rci.seek(1);
        assert_eq!(rci.peek(), 2);
        rci.seek(2);
        assert_eq!(rci.peek(), 3);
    }

    #[test]
    fn column_iterator() {
        let mut rc : RawColumn<i32> = RawColumn::new();
        rc.elements.push(1);
        rc.elements.push(2);
        rc.elements.push(3);
        let mut rci = rc.iterator();
        assert_eq!(rci.next(), Some(1));
        assert_eq!(rci.next(), Some(2));
        assert_eq!(rci.next(), Some(3));
        assert_eq!(rci.next(), None);
        rci.seek(0);
        assert_eq!(rci.next(), Some(1));
    }
}