use std::io;
use std::mem;

fn read(stdin: &io::Stdin, buffer: &mut String) {
    buffer.truncate(0);
    stdin.read_line(buffer).ok().unwrap();
}

fn solve(vec: &Vec<isize>) -> Option<usize> {
    let mut cur_0;
    let mut cur_1 = 1isize;
    let mut cur_2 = 2isize;
    let mut count = 0;
    for (i, item) in vec.iter().enumerate() {
        cur_0 = cur_1;
        cur_1 = cur_2;
        cur_2 = i as isize + 3;
        if *item == cur_0 {
            continue;
        } else if i + 1 < vec.len() && *item == cur_1 {
            mem::swap(&mut cur_0, &mut cur_1);
            count += 1;
        } else if i + 2 < vec.len() && *item == cur_2 {
            mem::swap(&mut cur_2, &mut cur_1);
            mem::swap(&mut cur_1, &mut cur_0);
            count += 2;
        } else {
            return None;
        }
    }
    return Some(count as usize);
}

fn main() {
    let mut buffer = String::new();
    let stdin = io::stdin();
    read(&stdin, &mut buffer);
    let n = buffer.trim().parse::<isize>().ok().unwrap();
    assert!(n > 0);
    for _ in 0..n {
        read(&stdin, &mut buffer);
        let len = buffer.trim().parse::<usize>().ok().unwrap();
        buffer.trim();
        read(&stdin, &mut buffer);
        let vec: Vec<_> = buffer
            .trim()
            .split(" ")
            .map(|x| x.parse::<isize>().ok().unwrap())
            .collect();
        assert_eq!(vec.len(), len);
        match solve(&vec) {
            None => println!("Too chaotic"),
            Some(val) => println!("{}", val),
        }
    }
}

#[cfg(test)]
mod tests {
    use solve;
    #[test]
    fn test_sample() {
        assert_eq!(solve(&vec![2, 1, 5, 3, 4]), Some(3));
        assert_eq!(solve(&vec![2, 5, 1, 3, 4]), None);
        assert_eq!(solve(&vec![1, 2, 3]), Some(0));
        assert_eq!(solve(&vec![1, 3, 2]), Some(1));
        assert_eq!(solve(&vec![3, 1, 2]), Some(2));
        assert_eq!(solve(&vec![3, 2, 1]), Some(3));
        assert_eq!(solve(&vec![1, 2, 3, 4]), Some(0));
        assert_eq!(solve(&vec![1, 3, 2, 4]), Some(1));
        assert_eq!(solve(&vec![3, 1, 2, 4]), Some(2));
        assert_eq!(solve(&vec![3, 1, 4, 2]), Some(3));
        assert_eq!(solve(&vec![3, 4, 1, 2]), Some(4));
        assert_eq!(solve(&vec![3, 4, 1, 2]), Some(4));
        assert_eq!(solve(&vec![3, 4, 2, 1]), Some(5));
    }
}
