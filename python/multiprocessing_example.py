from multiprocessing import Pool
import time
import os
import signal


def normal(x):
    return x * 2


def sleep(x):
    try:
        time.sleep(1)
        return x * 2
    except KeyboardInterrupt as ke:
        print "KeyboardInterrupt"


def sleep_nohandler(x):
    signal.signal(signal.SIGINT, signal.SIG_IGN)
    time.sleep(1)
    return x * 2


def cannot_pickle(x):
    def innerfunction():
        "Inner functions are not picklable."
    return innerfunction


def exception(x):
    raise RuntimeError("I don't like %d" % x)


def main():
    print "PID: %d" % os.getpid()
    p = Pool(5)

    def wrapper(func):
        print "Test: %s" % func.__name__
        try:
            res = p.map_async(func, range(10))
            result = res.get(10)
            print " success: %s" % result
        except Exception as e:
            print " error: %s" % e

    wrapper(normal)
    wrapper(cannot_pickle)
    wrapper(exception)
    wrapper(sleep)
    wrapper(sleep_nohandler)

if __name__ == '__main__':
    main()
