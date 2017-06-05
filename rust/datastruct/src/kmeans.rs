// k-means via rust.
use std::f64;

#[derive(PartialEq, Debug, Copy, Clone)]
struct Point {
    x: f64,
    y: f64,
}

impl Point {
    fn new(x: f64, y: f64) -> Point {
        return Point { x: x, y: y };
    }
}

fn dist(p: &Point, q: &Point) -> f64 {
    return ((p.x - q.x).powi(2) + (p.y - q.y).powi(2)).sqrt();
}

fn run(pts: &Vec<Point>, num_clusters: usize, max_iteration: usize) -> Vec<Point> {
    let mut clusters = Vec::with_capacity(num_clusters);
    for i in 0..num_clusters {
        clusters.push(pts[i])
    }
    for _ in 0..max_iteration {
        let assignments = iteration_assignment(pts, &clusters);
        let new_clusters = iteration_cluster(pts, &assignments, num_clusters);
        if new_clusters == clusters {
            return new_clusters;
        }
        clusters = new_clusters;
    }
    return clusters;
}

fn iteration_assignment(pts: &Vec<Point>, clusters: &Vec<Point>) -> Vec<usize> {
    let mut result = Vec::with_capacity(pts.len());
    for pt in pts {
        let mut min_dist: f64 = f64::MAX;
        let mut min_cluster: Option<usize> = None;
        for (i, cluster) in clusters.iter().enumerate() {
            let d = dist(pt, cluster);
            if d < min_dist {
                min_dist = d;
                min_cluster = Some(i);
            }
        }
        result.push(min_cluster.unwrap());
    }
    return result;
}

fn iteration_cluster(pts: &Vec<Point>,
                     assignments: &Vec<usize>,
                     num_clusters: usize)
                     -> Vec<Point> {
    let mut result = Vec::with_capacity(num_clusters);
    for cluster_idx in 0..num_clusters {
        let mut x: f64 = 0.0;
        let mut y: f64 = 0.0;
        let mut n: i64 = 0;
        for (i, pt) in pts.iter().enumerate() {
            if assignments[i] == cluster_idx {
                n += 1;
                x += pt.x;
                y += pt.y;
            }
        }
        let nf = n as f64;
        result.push(Point::new(x / nf, y / nf));
    }
    return result;
}

#[cfg(test)]
mod tests {
    use kmeans;
    use kmeans::Point;

    #[test]
    fn test_dist() {
        assert_eq!(kmeans::dist(&Point { x: 0.0, y: 0.0 }, &Point { x: 0.0, y: 0.0 }),
                   0.0);
        assert_eq!(kmeans::dist(&Point { x: 0.0, y: 1.0 }, &Point { x: 0.0, y: 0.0 }),
                   1.0);
        assert_eq!(kmeans::dist(&Point { x: 1.0, y: 1.0 }, &Point { x: 0.0, y: 0.0 }),
                   1.4142135623730951);
    }

    #[test]
    fn test_kmeans() {
        let data = vec![Point::new(0.0, 0.0),
                        Point::new(1.0, 1.0),
                        Point::new(10.0, 10.0),
                        Point::new(11.0, 11.0)];
        let c1 = vec![Point::new(0.0, 0.0), Point::new(1.0, 1.0)];
        let a1 = kmeans::iteration_assignment(&data, &c1);
        assert_eq!(a1, vec![0, 1, 1, 1]);
        let c2 = kmeans::iteration_cluster(&data, &a1, 2);
        assert_eq!(c2,
                   vec![Point::new(0.0, 0.0),
                        Point::new(7.333333333333333, 7.333333333333333)]);
        let a2 = kmeans::iteration_assignment(&data, &c2);
        assert_eq!(a2, vec![0, 0, 1, 1]);
        let c3 = kmeans::iteration_cluster(&data, &a2, 2);
        assert_eq!(c3, vec![Point::new(0.5, 0.5), Point::new(10.5, 10.5)]);
    }

    #[test]
    fn test_kmeans_full() {
        let data = vec![Point::new(0.0, 0.0),
                        Point::new(1.0, 1.0),
                        Point::new(10.0, 10.0),
                        Point::new(11.0, 11.0)];
        let clusters = kmeans::run(&data, 2, 10);
        assert_eq!(clusters, vec![Point::new(0.5, 0.5), Point::new(10.5, 10.5)]);
    }
}
