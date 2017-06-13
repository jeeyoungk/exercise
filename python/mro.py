class A(object):
    def __init__(self, a, *args, **kwargs):
        print "a=%s" % a

class B(object):
    def __init__(self, b1, b2, *args, **kwargs):
        print "b1=%s;b2=%s" % (b1, b2)

class AB(A, B):
    def __init__(self, *args, **kwargs):
        A.__init__(self, *args, **kwargs)
        B.__init__(self, *args, **kwargs)

class C(AB):
    def __init__(self, *args, **kwargs):
        super(C, self).__init__(*args, **kwargs)

ab = AB('x', 'z')
c = AB('y', 'z')
