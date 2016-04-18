#include <iostream>

using namespace std;

int& sample(int value) {
  int foo = value;
  return foo;
}

int main() {
  cout << sample(3) << endl;
  cout << sample(4) << endl;
  int& x = sample(5);
  int& y = sample(6);
  cout << x << endl;
  cout << y << endl;
}

