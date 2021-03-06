// implementation of the dijkstra's algorithm.
#include <cmath>
#include <cstddef>
#include <cstdint>
#include <iostream>
#include <limits>
#include <queue>
#include <vector>

class Point {
  const double x;
  const double y;

public:
  Point(const double &x, const double &y) : x(x), y(y) {}

  double dist(const Point &other) const {
    double dx = x - other.x;
    double dy = y - other.y;
    return pow(pow(dx, 2) + pow(dy, 2), 0.5);
  }
};

template <typename P> class PointCluster {
  const std::vector<P> points;

public:
  PointCluster(const std::vector<P> &points) : points(points) {}
  double dist(const PointCluster &other) const {
    double smallest = std::numeric_limits<double>::max();
    for (auto i = points.begin(); i < points.end(); ++i) {
      for (auto j = other.points.begin(); j < other.points.end(); ++j) {
        smallest = std::min(smallest, i->dist(*j));
      }
    }
    return smallest;
  }
};

struct DistanceIndex {
  int index;
  double distance;
};

class DistanceIndexComparator {
public:
  bool operator()(DistanceIndex a, DistanceIndex b) {
    return a.distance > b.distance;
  }
};

template <typename P>
double dijkstra(const std::vector<P> &points, size_t begin, size_t end) {
  auto n = points.size();
  std::vector<bool> visited(n, false);
  std::vector<double> distance(n, std::numeric_limits<double>::max());
  std::priority_queue<DistanceIndex, std::vector<DistanceIndex>,
                      DistanceIndexComparator> closest;
  visited[begin] = true;
  distance[begin] = 0.0;
  for (auto i = 0; i < n; i++) {
    distance[i] = points[i].dist(points[begin]);
    closest.push(DistanceIndex{i, distance[i]});
  }

  // put a bound on iterations.
  for (auto iteration = 0; iteration < n; iteration++) {
    size_t index;
    while (!closest.empty()) {
      auto top = closest.top();
      if (visited[top.index]) {
        closest.pop();
        continue;
      } else {
        closest.pop();
        index = top.index;
        break;
      }
    }
    if (index == end) {
      return distance[index];
    }
    for (auto i = 0; i < n; i++) {
      distance[i] = std::min(distance[i],
                             distance[index] + points[i].dist(points[index]));
    }
    visited[index] = true;
  }
  return std::numeric_limits<double>::max();
}

int main() {
  std::cout << "Case 1" << std::endl;
  std::vector<Point> points1;
  points1.push_back(Point(0, 0));
  points1.push_back(Point(10, 10));
  std::cout << dijkstra(points1, 0, 1) << std::endl;

  std::cout << "Case 2" << std::endl;
  std::vector<PointCluster<Point>> points2;
  points2.push_back(PointCluster<Point>(std::vector<Point>{Point(0, 0)}));
  points2.push_back(PointCluster<Point>(std::vector<Point>{Point(10, 10)}));
  points2.push_back(
      PointCluster<Point>(std::vector<Point>{Point(0, 1), Point(100, 100)}));
  points2.push_back(
      PointCluster<Point>(std::vector<Point>{Point(100, 101), Point(10, 9)}));
  points2.push_back(
      PointCluster<Point>(std::vector<Point>{Point(0, 2), Point(10, 8)}));
  std::cout << dijkstra(points2, 0, 1) << std::endl;
}
