

class StableDNA(object):
    def __init__(self, string):
        assert len(string) % 4 == 0
        self.string = string
        self.expected = len(string) / 4
        self.a = 0
        self.t = 0
        self.g = 0
        self.c = 0
        self.start = 0
        self.end = len(string) - 1

    def consume_left(self):
        assert self.remaining()
        char = self.string[self.start]
        self.consume(char, 1)
        self.start += 1

    def consume_right(self):
        assert self.remaining()
        char = self.string[self.end]
        self.consume(char, 1)
        self.end -= 1
        assert self.end >= -1

    def spit_left(self):
        assert self.remaining() < len(self.string)
        self.start -= 1
        assert self.start >= 0
        char = self.string[self.start]
        self.consume(char, -1)

    def spit_right(self):
        assert self.remaining() < len(self.string)
        self.end += 1
        char = self.string[self.end]
        self.consume(char, -1)

    def consume(self, char, value):
        if char == 'A': self.a += value
        elif char == 'T': self.t += value
        elif char == 'G': self.g += value
        elif char == 'C': self.c += value

    def is_valid(self):
        if (
            self.a <= self.expected and
            self.t <= self.expected and
            self.g <= self.expected and
            self.c <= self.expected and
            self.remaining >= 0
        ):
            return True
        return False

    def remaining(self):
        return self.end - self.start + 1

def run(string):
    dna = StableDNA(string)
    min_dist = len(string)

    while dna.is_valid() and dna.remaining() > 0:
        dna.consume_left() # consume everything on the left side.

    while dna.start > 0:
        # iteratively give up the left side 1+ times until it is valid,
        # and consume as many as possible on the rigit side.

        dna.spit_left() # give up at least 1.
        while not dna.is_valid() and dna.start > 0:
            dna.spit_left()
        while dna.is_valid() and dna.remaining() > 0:
            min_dist = min(min_dist, dna.remaining())
            dna.consume_right()

        if dna.is_valid():
            min_dist = min(min_dist, dna.remaining())
    return min_dist

def main():
    q = int(raw_input().strip())
    s = raw_input().strip()
    result = run(s)
    print(result)
main()
