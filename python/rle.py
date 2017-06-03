# run-length encoding related algorithms

class RLECursor(object):
    '''
    Convenient structure to Navigate RLEs
    '''

    __slots__ = ('rle', 'index', 'offset')
    def __init__(self, rle):
        self.rle = rle
        self.index = 0
        self.offset = 0

    def current_value(self):
        if self.index >= len(self.rle):
            return None
        return self.rle[self.index][0]

    def current_length(self):
        if self.index >= len(self.rle):
            return 0
        return self.rle[self.index][1] - self.offset

    def increment(self, delta):
        index = self.index
        offset = self.offset
        while delta > 0 and index < len(self.rle):
            l = self.rle[index][1]
            if offset + delta < l:
                offset += delta
                delta = 0
            else:
                delta -= (l - offset)
                offset = 0
                index += 1

        self.index = index
        self.offset = offset

def to_rle(lst):
    '''
    convert a given list to run-length encoding.
    '''
    count = 0
    value = None
    for cur in lst:
        if value != cur:
            if count > 0:
                yield (value, count)
            value = cur
            count = 1
        else:
            count += 1
    if count > 0:
        yield (value, count)

def materialize(rle):
    '''
    Materialize the RLE list
    '''
    for value, count in rle:
        for i in range(count):
            yield value

def rle_product(rle_a, rle_b):
    '''combine two lists of rle'''
    cur_a = RLECursor(rle_a)
    cur_b = RLECursor(rle_b)
    while cur_a.current_value() is not None and cur_b.current_value() is not None:
        len_a = cur_a.current_length()
        len_b = cur_b.current_length()
        val_a = cur_a.current_value()
        val_b = cur_b.current_value()
        minimum = min(len_a, len_b)
        yield ((val_a, val_b), minimum)
        cur_a.increment(minimum)
        cur_b.increment(minimum)

    while cur_a.current_value() is not None:
        val_a = cur_a.current_value()
        len_a = cur_a.current_length()
        yield ((val_a, None), len_a)
        cur_a.increment(len_a)

    while cur_b.current_value() is not None:
        val_b = cur_b.current_value()
        len_b = cur_b.current_length()
        yield ((None, val_b), len_b)
        cur_b.increment(len_b)

def rle_filter(rle_filter, rle_values):
    product = rle_product(rle_filter, rle_values)
    for ((cond, value), length) in product:
        if cond:
            yield (value, length)

# Tests
import pytest

@pytest.mark.parametrize('sample', [
    'azaabbccccabab',
    '',
    'axbycz',
    'aaabbb',
])
def test_rlecursor(sample):
    c = RLECursor(list(to_rle(sample)))
    for i in range(len(sample)):
        if i != 0: c.increment(1)
        print i, c.index, c.offset
        assert sample[i] == c.current_value(), '%d th offset does not match' % i
    
def test_to_rle():
    assert list(to_rle([])) == []
    assert list(to_rle([1,2,3])) == [(1, 1), (2, 1), (3, 1)]
    assert list(to_rle([1,1,2,2,3,3,3])) == [(1, 2), (2, 2), (3, 3)]

def test_materialize():
    assert list(materialize([])) == []
    assert list(materialize([(1, 3)])) == [1, 1, 1]

def test_rle_product_1():
    a = 'abc'
    b = 'xyz'
    rle_a = list(to_rle(a))
    rle_b = list(to_rle(b))
    result = list(rle_product(rle_a, rle_b))
    assert result == [
            (('a','x'), 1),
            (('b','y'), 1),
            (('c','z'), 1),
    ]

def test_rle_product_2():
    a = 'aaabbccc'
    b = 'xxyyyyzz'
    rle_a = list(to_rle(a))
    rle_b = list(to_rle(b))
    result = list(materialize(list(rle_product(rle_a, rle_b))))
    assert result == [
            ('a','x'),
            ('a','x'),
            ('a','y'),
            ('b','y'),
            ('b','y'),
            ('c','y'),
            ('c','z'),
            ('c','z'),
    ]

def test_rle_product_3():
    a = 'abbc'
    b = '122'
    rle_a = list(to_rle(a))
    rle_b = list(to_rle(b))
    result = list(materialize(list(rle_product(rle_a, rle_b))))
    assert result == [
            ('a','1'),
            ('b','2'),
            ('b','2'),
            ('c', None),
    ]

def test_rle_product_3():
    a = 'abc'
    b = '1223'
    rle_a = list(to_rle(a))
    rle_b = list(to_rle(b))
    result = list(materialize(list(rle_product(rle_a, rle_b))))
    assert result == [
            ('a','1'),
            ('b','2'),
            ('c','2'),
            (None, '3'),
    ]

def test_rle_filter_1():
    a = [True, True, False]
    b = [1, 2, 3]
    rle_a = list(to_rle(a))
    rle_b = list(to_rle(b))
    result = list(materialize(list(rle_filter(rle_a, rle_b))))
    assert result == [1, 2]

