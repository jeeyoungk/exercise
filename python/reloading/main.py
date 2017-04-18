import random
import sys
sys.dont_write_bytecode = True # disable PYC caching.
number = random.randint(0, 1024)
def write_modules(number):
    context = {
        'number': number
    }
    with open('module_a.py', 'w') as f:
        f.write('''
print "importing A"
number = {number}
'''.format(**context))
    with open('module_b.py', 'w') as f:
        f.write('''
import module_a
print "importing B"
from module_a import number
def number_from_module():
  return module_a.number

def number_from_inline():
  return number
'''.format(**context))


write_modules(number)
import module_a
import module_b
print module_a.number
assert module_a.number == number
assert module_b.number_from_module() == number
assert module_b.number_from_inline() == number
write_modules(number + 1)
reload(module_a)
print module_a.number
assert module_a.number == number + 1
assert module_b.number_from_module() == number + 1
assert module_b.number_from_inline() == number # inline imports stays
