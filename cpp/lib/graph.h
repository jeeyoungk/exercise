#pragma once
#include <vector>

namespace jee {

double dijkstra(const std::vector<std::vector<double>> adjMatrix, size_t begin,
                size_t end);

double dijkstra_with_path(const std::vector<std::vector<double>> adjMatrix,
                          size_t begin, size_t end, std::vector<size_t> *path);
}
