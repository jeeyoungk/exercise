// complex number implementation
use std::ops::Add;
use std::ops::Mul;
#[derive(PartialEq, Debug, Copy, Clone)]
struct Complex {
    re: f64,
    im: f64,
}

impl Add for Complex {
    type Output = Complex;
    fn add(self, other: Complex) -> Complex {
        Complex {
            re: self.re + other.re,
            im: self.im + other.im,
        }
    }
}

impl Mul for Complex {
    type Output = Complex;
    fn mul(self, rhs: Complex) -> Complex {
        Complex {
            re: self.re * rhs.re - self.im * rhs.im,
            im: self.re * rhs.im + self.im * rhs.re,
        }
    }
}

const ONE: Complex = Complex { re: 1.0, im: 0.0 };
const I: Complex = Complex { re: 0.0, im: 1.0 };

#[cfg(test)]
mod tests {
    use complex::Complex;
    use complex::ONE;
    use complex::I;

    #[test]
    fn test_simple() {
        assert_eq!(ONE * I, I);
        assert_eq!(Complex { re: 1.0, im: 1.0 } * Complex { re: 1.0, im: 1.0 },
                   Complex { re: 0.0, im: 2.0 })
    }
}
