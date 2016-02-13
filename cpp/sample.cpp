#include <string>
#include <iostream>

using namespace std;

template <typename T> T triple(T t) { return t * 3; }

int main() {
  cout << triple<int>(3) << endl;
  // cout << triple<string>("3") << endl; // causes compilation error.
}
