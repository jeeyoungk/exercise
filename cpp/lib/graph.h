#pragma once
#include <vector>

namespace jee {

class graph;
class vertex;
class edge;

class graph {
public:
  vertex &add();
  std::vector<vertex> &vertices() const;
};

class vertex {
public:
  void add(const vertex &u);
  std::vector<edge> &edges() const;
};

class edge {
public:
  // source of the edge
  vertex &src() const;
  // destination of the edge
  vertex &dest() const;

  double distance() const;
};
}
