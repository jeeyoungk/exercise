package main

import "math"

// my interview question.
// black hole dijkstra question.

type Point struct {
	x       float64
	y       float64
	cluster int
}

func PointDistance(left Point, right Point) float64 {
	dx := left.x - right.x
	dy := left.y - right.y
	return math.Sqrt(dx*dx + dy*dy)
}

func ClusterDistance(leftCluster []Point, rightCluster []Point) (min float64) {
	min = math.MaxFloat64
	for _, left := range leftCluster {
		for _, right := range rightCluster {
			min = math.Min(min, PointDistance(left, right))
		}
	}
	return
}

func dijkstra(points []Point) float64 {
	// 1. categorize the points.
	// 2. run the dijikstra's algorithm.
	// the first point - starting point.
	// the last point - ending point.

	startCluster := points[0].cluster
	endCluster := points[len(points)-1].cluster
	clusterMap := make(map[int][]Point)
	distMap := make(map[int]float64)
	visitedMap := make(map[int]bool)
	for _, point := range points {
		existing, contains := clusterMap[point.cluster]
		if !contains {
			existing = make([]Point, 0, 10)
		}
		existing = append(existing, point)
		clusterMap[point.cluster] = existing
	}

	for key := range clusterMap {
		if key == startCluster {
			distMap[key] = 0
			visitedMap[key] = true
		} else {
			distMap[key] = math.MaxFloat64
			visitedMap[key] = false
		}
	}
	distMap[startCluster] = 0

	var visiting = startCluster
	for visiting != endCluster {
		var minIndex = -1
		var minValue = math.MaxFloat64
		visitedMap[visiting] = true

		for clusterKey := range clusterMap {
			if visitedMap[clusterKey] {
				continue
			}

			distMap[clusterKey] = math.Min(
				distMap[clusterKey],
				distMap[visiting]+ClusterDistance(clusterMap[visiting], clusterMap[clusterKey]),
			)
			if distMap[clusterKey] < minValue {
				minValue = distMap[clusterKey]
				minIndex = clusterKey
			}
		}
		visiting = minIndex
	}
	return distMap[endCluster]
}
