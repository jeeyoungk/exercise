# Donald Clark's interview question.

# consts
UP = "u"
DOWN = "d"
LEFT = "l"
RIGHT = "r"
COMMIT = "!"

def get_coord(character):
    width = 5
    assert 'a' <= character <= 'z', "invalid character"
    ref = ord('a')
    x = (ord(character) - ref) % width
    y = (ord(character) - ref) / width
    return (x, y)

def get_command(dx, dy):
    result = []
    if dy != 0:
        cmd = UP if dy < 0 else DOWN
        result.append((cmd, abs(dy)))
    if dx != 0:
        cmd = RIGHT if dx > 0 else LEFT
        result.append((cmd, abs(dx)))
    return result

def move(string):
    cur_coord = (0, 0)
    cmds = []
    for character in string:
        new_coord = get_coord(character)
        (x, y) = cur_coord
        (nx, ny) = new_coord
        dx = nx - x
        dy = ny - y
        cmds += get_command(dx, dy)
        cmds += ["!"]
        cur_coord = new_coord
    return cmds

# test code
def flatten(commands): return "".join("".join(map(str, parts)) for parts in commands)
assert flatten(move("aid")) == "!d1r3!u1!"
assert flatten(move("")) == ""
assert flatten(move("zoo")) == "d5!u3r4!!"
assert flatten(move("oz")) == "d2r4!d3l4!"
