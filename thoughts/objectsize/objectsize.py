import sys

class Class:
    def __init__(self):
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
