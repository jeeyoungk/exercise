use std::cmp;
use std::io;

fn wrap(i: usize, n: usize) -> usize {
    return i % n;
}

fn palindrome(string: &[u8]) -> Vec<usize> {
    let n = string.len();
    let mut max_length = Vec::with_capacity(n);
    // prev[i] = i .. len being palindrome.
    let mut prev_2 = Vec::with_capacity(n);
    for _ in 0..n {
        prev_2.push(true);
    }
    let mut prev_1 = Vec::with_capacity(n);
    for i in 0..n {
        let j = wrap(i + 1, n);
        let cond = string[i] == string[j];
        prev_1.push(cond);
        max_length.push(match cond {
                            true => 2,
                            false => 1,
                        });
    }
    // println!("0 {:?}", prev_2);
    // println!("1 {:?}", prev_1);
    // println!("1 {:?}", max_length);
    for len in 2..n {
        let mut cur = Vec::with_capacity(n);
        let mut cur_max_len = Vec::with_capacity(n);
        for i in 0..n {
            let j = wrap(i + len, n);
            let cond = string[i] == string[j] && prev_2[wrap(i + 1, n)];
            cur.push(cond);

            let new_max : usize = match cond {
                true => len + 1,
                false => cmp::max(max_length[i], max_length[wrap(i + 1, n)]),
            };
            cur_max_len.push(new_max);
        }
        // println!("{} {:?}", len, cur);
        // println!("{} {:?}", len, cur_max_len);
        prev_2 = prev_1;
        prev_1 = cur;
        max_length = cur_max_len;
    }
    return max_length;
}

pub fn main() {
    let mut buffer = String::new();
    let stdin = io::stdin();
    stdin.read_line(&mut buffer).ok().unwrap();
    let n = buffer.trim().parse::<usize>().unwrap();
    buffer.truncate(0);
    stdin.read_line(&mut buffer).ok().unwrap();
    assert_eq!(n, buffer.trim().len());
    let palindromes = palindrome(buffer.trim().as_bytes());
    for v in palindromes {
        println!("{}", v);
    }    
}

#[cfg(test)]
mod tests {
    use circular_palindrome::palindrome;
    #[test]
    fn test() {
        let input = "aaaaabbbbaaaa";
        let bytes = input.as_bytes();
        palindrome(bytes);
    }
}
