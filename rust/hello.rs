use std::ops::Add;

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
    println!("> References");
    references();
    println!("> Enums");
    enums();
}

fn primitives() {
    // List of Rust primitive types
    // https://doc.rust-lang.org/book/primitive-types.html
    println!("1 + 2 = {}", 1 + 2);
    println!("1 - 2 = {}", 1 - 2);
    println!("true and true = {}", true && true);
    let x: char = 'x';
    println!("x = {}", x);
    let y: f32 = 3.14;
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
    fn sum(slice: &[i32]) -> i32 {
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
    // this is used to override move semantics.
    #[derive(Copy, Clone)]
    struct Point {
        x: i32,
        y: i32,
    }
    fn add(p1: Point, p2: Point) -> Point {
        Point {
            x: p1.x + p2.x,
            y: p1.y + p2.y,
        }
    }
    impl Add for Point {
        // operator overloading!
        type Output = Point;
        fn add(self, other: Point) -> Point {
            Point {
                x: self.x + other.x,
                y: self.y + other.y,
            }
        }
    }

    fn print_point(p: Point) {
        println!("({} ,{})", p.x, p.y);
    }
    let p1 = Point { x: 1, y: 2 };
    let p2 = Point { x: 4, y: 5 };
    let p3 = add(p1, p2);
    let p4 = p1 + p2;
    print_point(p3);
    print_point(p4);
}

fn references() {
    // testing mutable reference.
    let mut x: i32 = 5;
    println!("x: {}", x);
    fn increment(x: &mut i32) {
        *x = *x + 1;
    }
    increment(&mut x);
    println!("x: {}", x);
    // testing immutable references
    fn print_twice(x: &i32) {
        println!("first:  {}", x);
        println!("second: {}", x);
    }
    print_twice(&3);
    print_twice(&x);
    /*
    this code does not compile - taking multiple mutable references.
    {
        let y = &mut x;
        let z = &mut x;
    }
    */
    /*
    this code does not compile - taking mutable & immutable references (they form read-write-lock).
    {
        let y = & x;
        let z = &mut x;
    }
    */
}

fn enums() {
    // standard enum.
    enum Color {
        RED,
        GREEN,
        BLACK,
    };
    fn categorize_color(c: Color) {
        match c {
            Color::RED => println!("color is red!"),
            Color::GREEN => println!("color is green!"),
            _ => println!("Unknown color."),
        }
    }
    categorize_color(Color::RED);
    categorize_color(Color::GREEN);
    categorize_color(Color::BLACK);
    // pattern matching enum.
    enum Option {
        None,
        Some(i32),
    }
    fn app(o: Option, function: fn(i32) -> i32) -> Option {
        match o {
            Option::None => Option::None,
            Option::Some(x) => Option::Some(function(x)),
        }
    }
    fn add_one(x: i32) -> i32 {
        x + 1
    }
    fn print_value(x: i32) -> i32 {
        println!("value: {}", x);
        return 0;
    }
    app(app(Option::Some(10), add_one), print_value);
    app(app(Option::None, add_one), print_value);
}
