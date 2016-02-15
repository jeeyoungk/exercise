#include <limits>
#include <queue>

#include "graph.h"

namespace jee {
double dijkstra(const std::vector<std::vector<double>> adj, size_t begin,
                size_t end) {
  return 1;
}

struct distance_index {
  int index;
  double distance;
};

class distance_index_comparator {
public:
  bool operator()(distance_index a, distance_index b) {
    return a.distance > b.distance;
  }
};

double dijkstra_with_path(const std::vector<std::vector<double>> adj,
                          size_t begin, size_t end, std::vector<size_t> *path) {
  const size_t n = adj.size();
  // used to construct
  std::vector<bool> visited(n, false);
  std::vector<size_t> previous(n, -1);
  std::vector<double> distance(n, std::numeric_limits<double>::max());
  // used to traverse the next node in the list.
  std::priority_queue<distance_index, std::vector<distance_index>,
                      distance_index_comparator> closest;

  // initialize
  for (auto i = 0; i < n; ++i) {
    if (i != begin) {
      distance[i] = adj[begin][i];
      closest.push(distance_index{i, distance[i]});
    } else {
      visited[i] = true;
      distance[i] = 0.0;
    }
  }

  for (auto iter = 0; iter < n; ++iter) {
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
      // done - fill the vector.
      if (path != nullptr) {
        while (index != -1) {
          path.push(index);
          index = previous[index];
        }
      }
      return distance[index];
    }
    for (auto i = 0; i < n; i++) {
      if (distance[index] + adj[i][j] < distance[i]) {
        distance[i] = distance[index] + adj[i][j];
        previous[i] = index;
      }
    }
    visited[index] = true;
  }
  return std::numeric_limits<double>::max();
}
}
