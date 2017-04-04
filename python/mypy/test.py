from typing import Iterator

def fib(n: int) -> Iterator[int]:
    a, b = 0, 1
    while a < n:
        yield a
        a, b = b, a + b

for i in fib(10):
    print(i)
for i in fib('foo'):
    print(i)
