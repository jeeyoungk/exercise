One interesting way to analyze different programming language is _how many additional bytes are needed for a X-byte of information_?

This is one of the first question I ask while I learn a new programming language. Believe me, it offers surprising amount of details on the language's underlying model of computing.

## C

This is one of the reason why C is great and relevant even after many years, because there’s no or very little overhead. You use precisely what your program declares. 

char uses a single byte. A single 32-bit integer takes up exactly 4 bytes.

I wrote a small code snippet:

```C
#include <iostream>
struct point_struct {
  int x;
  int y;
};

int main() {
  printf("Size of char is %lu\n", sizeof(char));
  printf("Size of char[1] is %lu\n", sizeof(char[1]));
  printf("Size of int is %lu\n", sizeof(int));
  printf("Size of int[1] is %lu\n", sizeof(int[1]));
  printf("Size of point_struct is %lu\n", sizeof(struct point_struct));

  return0;
}

// output
Size of char is 1
Size of char[1] is 1
Size of int is 4
Size of int[1] is 4
Size of point_struct is 8
```

Even composite types, such as structs and arrays, require no additional bytes. 

`point_struct`, composed of two 32bit integers, requires 8 bytes to represent. This is as efficient as we can get, and that’s why a lot of people love C.

(Note: Memory alignment makes this a bit complicated, but I’m ignoring it for now)

## C++

C++ is mostly like C except classes with virtual functions (functions that can be override by a subclass). These objects require additional metadata regarding its type to correctly dispatch virtual functions at runtime.

```C++
#include <iostream>

class point_class {
  private:
    int x;
    int y;
  public:
    int sum() {
      return x + y;
    }
};

class point_virtual {
  private:
    int x;
    int y;
  public:
    virtualint sum() {
      return x + y;
    }
};

int main() {
  printf("Size of point_class is %lu\n", sizeof(class point_class));
  printf("Size of point_virtual is %lu\n", sizeof(class point_virtual));
  return0;
}

// output
Size of point_class is 8
Size of point_virtual is 16
```

`point_virtual` is bigger by 8 bytes, but why? It’s because the function `point_virtual.sum()` requires information about  
point_virtual‘s type. The type information is used at runtime to dispatch the correct method. `point_class.sum()` always refers to a single function, so type information doesn’t need to be encoded. Thus it is 8 bytes, just like the original C struct.

## Python

Now let’s move farther away from the metal and take a look at Python, a popular high-level language. Python is fun to program in, but uses a lot of bytes to represent its internal data structures.

```python
# using cpython 3.3
import sys

class Class:
    def__init__(self):
        pass

instance = Class()

print("size of 0 is %d" % sys.getsizeof(0))
print("size of 1 is %d" % sys.getsizeof(1))
print("size of 0x3fffffff is %d" % sys.getsizeof(0x3fffffff))
print("size of 0x40000000 is %d" % sys.getsizeof(0x40000000))
print("size of [] is %d" % sys.getsizeof([]))
print("size of [0] is %d" % sys.getsizeof([0]))
print("size of {} is %d" % sys.getsizeof({}))
print("size of Class is %d" % (sys.getsizeof(instance) + sys.getsizeof(instance.__dict__)))

// output
size of 0 is 24
size of 1 is 28
size of 0x3fffffff is 28
size of 0x40000000 is 32
size of [0] is 72
size of (0,) is 56
size of {} is 288
size of Class() is 152
```

Observations:

- Everything is larger bigger in general.
- A single zero uses 24 bytes. Other integers use 28 and 32 bytes of data depending on their size because Python encodes integers in a variable-length encoding.
- Composite types, such as an array or tuple require additional bytes. An empty hashmap is 288 bytes (!!!).
- Instance of an empty class is 152 bytes. (The math for this is a bit complicated, because python internally maintains its field in a hashmap). It’s kinda funny how this smaller than an empty dictionary. I’m not sure, but it’s either that hashmaps of objects are optimized to have smaller default size, or I’m forgetting to add something up.

In general, why are Python objects so big? Because each Python object tracks a lot of metadata. Just like c++, information about an object’s type is encoded. Moreover, CPython (the standard Python) is reference counted, and each objectneeds to track the number of reference other objects have to it. All these metadata increases the total size, making them quite large relative to the actual data they’re representing.

## Other languages & summary

I’m cutting it short, but other languages are placed in this spectrum. However, in many high-level languages, it is hard to measure the exact size of objects. Even the Python example is running against CPython 3, and other versions of Python may have different implementation. Some high level languages don’t expose this information at all!

This obviously doesn’t matter for small programs, but the overhead becomes larger as you need to process a large amount of data. If a language supports dynamic dispatching, types need to be stored along with the data. Same goes for reference-counted garbage collection and other language features which needs additional metadata on every object within the runtime.
