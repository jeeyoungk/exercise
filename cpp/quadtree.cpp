#include <cstdint>
#include <iostream>
#include <memory>
#include <utility>
#include <vector>

using namespace std;

/* Range is representation of [x, y) */
class Range {
  double _begin;
  double _end;

public:
  Range(const double begin, const double end) : _begin(begin), _end(end) {}

  bool valid() const { return _begin < _end; }

  Range intersect(const Range &other) const {
    return Range(max(_begin, other._begin), min(_end, other._end));
  }

  double begin() const { return _begin; }

  double end() const { return _end; }

  bool contains(const double point) const {
    return _begin <= point && point < _end;
  }
};

ostream &operator<<(ostream &os, const Range &range) {
  os << "[" << range.begin() << "," << range.end() << ")";
  return os;
}

enum Direction { NW, NE, SW, SE };

const int NUM_QUADS = 4;

class QuadNode {
  const double x;
  const double y;
  unique_ptr<QuadNode> quads[NUM_QUADS];

  Range subrange_y(const Direction direction) const {
    switch (direction) {
    case NW:
    case NE:
      return Range(y, numeric_limits<double>::infinity());
    case SW:
    case SE:
      return Range(-numeric_limits<double>::infinity(), y);
    }
  }

  Range subrange_x(const Direction direction) const {
    switch (direction) {
    case NW:
    case SW:
      return Range(-numeric_limits<double>::infinity(), x);
    default:
      return Range(x, numeric_limits<double>::infinity());
    }
  }

public:
  QuadNode(const double &x, const double &y) : x(x), y(y) {}

  size_t size() const {
    size_t sum = 1;
    for (int i = 0; i < NUM_QUADS; i++) {
      if (quads[i]) {
        sum += quads[i]->size();
      }
    }
    return sum;
  }

  bool insert(const double newx, const double newy) {
    unique_ptr<QuadNode> *quad;
    if (x == newx && y == newy) {
      return false;
    } else if (newy >= y) {
      if (newx >= x) {
        quad = &quads[NE];
      } else {
        quad = &quads[NW];
      }
    } else {
      if (newx >= x) {
        quad = &quads[SE];
      } else {
        quad = &quads[SW];
      }
    }
    if (*quad) {
      // recurse into the given quad.
      return (*quad)->insert(newx, newy);
    } else {
      // blank quad - insert to it.
      quad->reset(new QuadNode(newx, newy));
      return true;
    }
  }

  // count # of points within the given range.
  size_t count(const Range xrange, const Range yrange) const {
    size_t sum = 0;
    range(xrange, yrange, [&sum](double x, double y) { sum++; });
    return sum;
  }

  // run the given iterator function.
  void range(const Range &xrange, const Range &yrange,
             function<void(double, double)> f) const {
    if (xrange.contains(x) && yrange.contains(y)) {
      f(x, y);
    }
    for (size_t d = 0; d < NUM_QUADS; d++) {
      Direction dir = static_cast<Direction>(d);
      if (quads[dir]) {
        QuadNode &subquad = *quads[dir];
        auto subxrange = xrange.intersect(subrange_x(dir));
        auto subyrange = yrange.intersect(subrange_y(dir));
        if (subxrange.valid() && subyrange.valid()) {
          subquad.range(subxrange, subyrange, f);
        }
      }
    }
  }
};

int main() {
  QuadNode qn(0, 0);
  qn.insert(1, 1);
  qn.insert(-1, -1);
  qn.insert(1, -1);
  qn.insert(-1, 1);
  cout << qn.size() << endl;
  cout << qn.count(Range(-10, 10), Range(-40, 40)) << endl;
  cout << qn.count(Range(0, 10), Range(0, 10)) << endl;
}
