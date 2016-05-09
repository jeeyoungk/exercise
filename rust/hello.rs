fn main() {
    println!("hello, world");
    println!("> Primitives");
    primitives();
    println!("> Tuples");
    tuples();
    println!("> Arrays");
    arrays();
    println!("> Structs");
    structs();
}

fn primitives() {
    // List of Rust primitive types
    // https://doc.rust-lang.org/book/primitive-types.html
    println!("1 + 2 = {}", 1 + 2);
    println!("1 - 2 = {}", 1 - 2);
    println!("true and true = {}", true && true);
    let x:char = 'x';
    println!("x = {}", x);
    let y:f32 = 3.14;
    println!("y = {}", y);
}

fn tuples() {
    fn reverse(pair: (i32, i32)) -> (i32, i32) {
        let (x, y) = pair;
        (y, x)
    }
    let pair = (3, 2);
    let reversed = reverse(pair);
    println!("reversed: ({}, {})", reversed.0, reversed.1);
}

fn arrays() {
    fn sum(slice:&[i32]) -> i32 {
        let mut total = 0;
        for i in 0..slice.len() {
            total += slice[i]
        }
        total
    }
    let x = [1, 2, 3];
    println!("total: {}", sum(&x));
    let y = [1, 2, 3, 4, 5];
    println!("total: {}", sum(&y));
}

fn structs() {
    struct Point {
        x: i32,
        y: i32
    }
    fn add(p1: Point, p2: Point) -> Point {
        Point {
            x: p1.x + p2.x,
            y: p1.y + p2.y
        }
    }
    fn print_point(p: Point) {
        println!("({} ,{})", p.x, p.y);
    }
    let p1 = Point { x: 1, y: 2 };
    let p2 = Point { x: 4, y: 5 };
    let p3 = add(p1, p2);
    print_point(p3);
}

