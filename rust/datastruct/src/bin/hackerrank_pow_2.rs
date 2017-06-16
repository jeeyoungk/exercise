use std::io;

const N: usize = 300;
const HASH_MUL: usize = 31; // TODO - change
type Largenum = [u8; N];

fn read(stdin: &io::Stdin, buffer: &mut String) {
    buffer.truncate(0);
    stdin.read_line(buffer).ok().unwrap();
}

struct RabinKarp {
    hash: usize,
    mul: usize,
    len: usize,
    mul_end: usize,
}

struct HashedPattern {
    hash: usize,
    pattern: Vec<u8>,
}

impl RabinKarp {
    fn new(mul: usize, len: usize) -> RabinKarp {
        assert!(len >= 1);
        let mut mul_end = 1usize;
        for _ in 1..len {
            mul_end = mul_end.wrapping_mul(mul);
        }
        return RabinKarp {
                   hash: 0,
                   mul: mul,
                   len: len,
                   mul_end: mul_end,
               };
    }

    // add and subtract a character.
    fn add_sub(&mut self, added: u8, subtracted: u8) {
        let subtracted = self.mul_end.wrapping_mul(subtracted as usize);
        self.hash = ((self.hash.wrapping_sub(subtracted)).wrapping_mul(self.mul))
            .wrapping_add(added as usize);
    }
}

impl HashedPattern {
    fn new(pattern: Vec<u8>) -> HashedPattern {
        let mut hasher = RabinKarp::new(HASH_MUL, pattern.len());
        for c in &pattern {
            hasher.add_sub(*c, 0);
        }
        return HashedPattern {
                   hash: hasher.hash,
                   pattern: pattern,
               };
    }
}

fn multiply_two(largenum: &mut Largenum) {
    let mut output = [0; N];
    for i in 0..N {
        let tmp = largenum[i] * 2;
        if tmp < 10 {
            output[i] += tmp;
        } else {
            // panic on overflow
            output[i + 1] += 1;
            output[i] += tmp - 10;
        }
    }
    *largenum = output;
}

fn to_string(input: &[u8]) -> String {
    let mut result = String::new();
    let mut found_nonzero = false;
    for i in 0..input.len() {
        found_nonzero = true;
        result.push(char::from(48 + input[i]));
    }
    return result;
}

fn to_truncated(input: &Largenum) -> Vec<u8> {
    let mut result = Vec::new();
    let mut found_nonzero = false;
    for i in 0..N {
        let rev_i = N - i - 1;
        if input[rev_i] == 0 && !found_nonzero {
            continue;
        }
        found_nonzero = true;
        result.push(input[rev_i]);
    }
    return result;
}

fn test_eq(x:&[u8], y:&[u8]) -> bool {
    assert_eq!(x.len(), y.len());
    for i in 0..x.len() {
        if x[i] != y[i] {
            return false;
        }
    }
    return true;
}

fn test_for_powers(dataset: &Vec<HashedPattern>, input: &str) -> usize {
    let mut result = 0;
    let mut input_bytes = Vec::new();
    for b in input.as_bytes() {
        input_bytes.push(b - 48);
    }
    let len = input_bytes.len();
    // println!("Running");
    for hashed_pattern in dataset {
        let ptrn = &hashed_pattern.pattern;
        let ptrn_len = ptrn.len();
        let mut hasher = RabinKarp::new(HASH_MUL, ptrn_len);
        if len + 1 < ptrn_len {
            break;
        }

        for index in 0..len {
            // println!("index={}/{}, pattern={} string={}", index, len, to_string(ptrn), to_string(&input_bytes));
            if index >= ptrn_len {
                // println!("Adding {} Removing {}", input_bytes[index], input_bytes[index - ptrn_len]);
                hasher.add_sub(input_bytes[index], input_bytes[index - ptrn_len]);
            } else {
                // println!("Adding {}", input_bytes[index]);
                hasher.add_sub(input_bytes[index], 0);
            }
            if index + 1 >= ptrn_len {
                if hashed_pattern.hash == hasher.hash {
                    if test_eq(&input_bytes[index + 1 - ptrn_len..index + 1], ptrn) {
                        result += 1;
                    }
                }
            }
        }
    }
    return result;
}

fn prep() -> Vec<HashedPattern> {
    let mut result = Vec::new();
    let mut ary: Largenum = [0; N];
    ary[0] = 1;
    result.push(HashedPattern::new(to_truncated(&ary)));
    for _ in 0..800 {
        multiply_two(&mut ary);
        result.push(HashedPattern::new(to_truncated(&ary)));
    }
    return result;
}

fn main() {
    let mut buffer = String::new();
    let stdin = io::stdin();
    read(&stdin, &mut buffer);
    let n = buffer.trim().parse::<isize>().ok().unwrap();
    let dataset = prep();
    for _f in 0..n {
        read(&stdin, &mut buffer);
        let result = test_for_powers(&dataset, buffer.trim());
        println!("{}", result);
    }
}

#[cfg(test)]
mod hackerrank_pow_2 {
    use Largenum;
    use N;
    use to_string;
    use multiply_two;
    use prep;
    use test_for_powers;
    use RabinKarp;
    #[test]
    fn simple() {
        /*
        let mut ary: Largenum = [0; N];
        ary[0] = 1;
        assert_eq!(to_string(&ary), "1");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "2");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "4");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "8");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "16");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "32");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "64");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "128");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "256");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "512");
        multiply_two(&mut ary);
        assert_eq!(to_string(&ary), "1024");
        */
    }

    #[test]
    fn test_case() {
        let dataset = prep();
        // assert_eq!(test_for_powers(&dataset, "2222222"), 7);
        assert_eq!(test_for_powers(&dataset, "24256"), 4);
        // assert_eq!(test_for_powers(&dataset, "65536"), 1);
        // assert_eq!(test_for_powers(&dataset, "023223"), 4);
        // assert_eq!(test_for_powers(&dataset, "33579"), 0);
    }

    #[test]
    fn test_rabinkarp() {
        let mut rk = RabinKarp::new(10, 3);
        assert_eq!(rk.hash, 0);
        rk.add_sub(3, 0);
        assert_eq!(rk.hash, 3);
        rk.add_sub(4, 0);
        assert_eq!(rk.hash, 34);
        rk.add_sub(5, 0);
        assert_eq!(rk.hash, 345);
        rk.add_sub(2, 3);
        assert_eq!(rk.hash, 452);
    }
}
