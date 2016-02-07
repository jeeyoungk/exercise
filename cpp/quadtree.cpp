#include <cstdint>
#include <iostream>
#include <memory>
#include <utility>
#include <vector>

using namespace std;

class QuadNode;

class QuadNode {
  const double x;
  const double y;
  unique_ptr<QuadNode> northwest;
  unique_ptr<QuadNode> northeast;
  unique_ptr<QuadNode> southwest;
  unique_ptr<QuadNode> southeast;

public:
  QuadNode(const double &x, const double &y) : x(x), y(y) {
  }

  size_t size() const {
    return 1
      + (northwest ? northwest->size() : 0) 
      + (northeast ? northeast->size() : 0)
      + (southwest ? southwest->size() : 0)
      + (southeast ? southeast->size() : 0);
  }

  bool insert(const double newx, const double newy) {
    unique_ptr<QuadNode>* quad;
    if (x == newx && y == newy) {
      return false;
    } else if (newy >= y) {
      if (newx >= x) {
        quad = &northwest;
      } else {
        quad = &northeast;
      }
    } else {
      if (newx >= x) {
        quad = &southwest;
      } else {
        quad = &southeast;
      }
    }
    if (*quad) {
      return (*quad)->insert(newx, newy);
    } else {
      quad->reset(new QuadNode(newx, newy));
      return true;
    }
  }

  void range(const double x1, const double x2, const double y1, const double y2, vector<pair<double, double>>& output) {
    // TODO - implement this.
  }
};

int main() {
  QuadNode qn(0, 0);
  qn.insert(3, 5);
  qn.insert(3, 5);
  qn.insert(10, 5);
  cout << qn.size() << endl;
}
