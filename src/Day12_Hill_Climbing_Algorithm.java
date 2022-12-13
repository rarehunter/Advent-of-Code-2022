import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

//
record PQNode(Point point, int distance) {}

class NodeDistanceComparator implements Comparator<PQNode> {
    @Override
    public int compare(PQNode x, PQNode y) {
        return Integer.compare(x.distance(), y.distance());
    }
}

public class Day12_Hill_Climbing_Algorithm {
    public static void main(String[] args) {
        File file = new File("./inputs/day12/day12.txt");
        int width = 0;
        int height = 0;
        char[][] grid;

        try {
            Scanner sc = new Scanner(file);
            Scanner sc2 = new Scanner(file);

            // Determine the height and width of grid.
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                width = line.length();
                height++;
            }

            // Initialize grid.
            grid = new char[height][width];

            Point start = null;
            Point end = null;

            // Iterate through the input again and populate the grid.
            int row = 0;
            while (sc2.hasNextLine()) {
                String line = sc2.nextLine();
                for (int i = 0; i < line.length(); i++) {
                    char currentChar = line.charAt(i);
                    grid[row][i] = currentChar;

                    if (currentChar == 'S')
                        start = new Point(i, row);

                    if (currentChar == 'E')
                        end = new Point(i, row);
                }
                row++;
            }

            int part1 = part1(grid, start, end);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(grid, end);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Helper method to initialize a HashMap that maps a point in the grid to its min path from the start point.
    // The path from start point to itself is initialized to 0.
    // All other points are initialized to the max int value.
    private static Map<Point, Integer> generateDistancesMap(char[][] grid, Point start) {
        Map<Point, Integer> distances = new HashMap<>();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                int initialDistance = grid[i][j] == grid[start.y][start.x] ? 0 : Integer.MAX_VALUE;
                distances.put(new Point(j,i), initialDistance);
            }
        }

        return distances;
    }

    // Returns a list of valid neighboring points from the given point.
    // Ensures that the point is within the bounds off the grid.
    // A point is considered a valid neighbor if its ASCII char value is at most one higher
    // than the given point.
    private static List<Point> getNeighbors(char[][] grid, Point p) {
        List<Point> validNeighbors = new ArrayList<>();
        int[][] directions = {{0,-1}, {0,1}, {1,0}, {-1,0}}; // representation of four cardinal directions

        for (int[] direction : directions) {
            char current = grid[p.y][p.x];
            int newRow = p.y + direction[1];
            int newCol = p.x + direction[0];

            // Boundary checking
            if (newRow >= 0 && newRow < grid.length && newCol >= 0 && newCol < grid[0].length) {
                char next = grid[newRow][newCol];

                // If we're looking for the neighbors of the start point, we treat it as if it were an 'a'.
                // Similarly, if we're looking at the end point as the next point (destination point), we treat it
                // as if it were a 'z'.
                if (current == 'S')
                    current = 'a';
                else if (next == 'E') {
                    next = 'z';
                }

                // Compare the ASCII value of the char and ensure that the difference between them is at most 1.
                if ((int)next - (int)current <= 1) {
                    validNeighbors.add(new Point(newCol, newRow));
                }
            }
        }

        return validNeighbors;
    }

    /* Part 1: Find the shortest path from the start point to the end point by implementing Dijkstra's algorithm.
        We need:
        - a set to hold visited points
        - a priority queue (PQ) of (point, distance) pairs which tells us which node to visit next based on
        its minimum distance so far.
        - a map from points to their distances to store the distances so far between the source node and all other nodes.

        While the PQ is not empty:
        1. get the next smallest distance node from the PQ that hasn't been visited yet.
        2. add that node to the visited set
        3. get the neighbors of the node and for each neighbor:
            3.1 if dist[current] + 1 < dist[neighbor], update dist[neighbor] to the new minimum value.
            3.2 add the neighbor to the PQ.

        Post-mortem: After implementing this in Dijkstra's algorithm, we realized that because all edge
        weights are 1, a simple BFS using a normal queue instead of a priority queue will suffice. This is because
        BFS is a special case of Dijkstra's algorithm on unweighted graphs.
     */
    private static int part1(char[][] grid, Point start, Point end) {
        Map<Point, Integer> distances = generateDistancesMap(grid, start);
        Set<Point> visited = new HashSet<>();

        // A min-heap is used to efficiently retrieve the vertex with the least path distance.
        PriorityQueue<PQNode> queue = new PriorityQueue<>(new NodeDistanceComparator());
        queue.add(new PQNode(start, 0));

        while (!queue.isEmpty()) {
            PQNode node = queue.poll();
            Point currentPoint = node.point();

            // If we've already visited this node, move on.
            if (visited.contains(currentPoint)) continue;

            // Add the point to our visited set indicating that this point has been visited.
            visited.add(currentPoint);

            // Get the valid neighbors (in which the destination cell is at most one higher than the source cell)
            List<Point> neighbors = getNeighbors(grid, currentPoint);

            // For each neighbor, if the distance of the source node to the current node (dist[current]) plus
            // the weight of the edge from the current node to the neighbor (weight[current, neighbor]) (in our case, it is 1)
            // is less than the best recorded distance of the source node to the neighbor (dist[neighbor]) already,
            // we update the distance to the neighbor to the new minimum distance value.
            // Otherwise, no updates are made.
            // In other words, we update if: dist[current] + 1 < dist[neighbor]
            for (Point neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    int newDistance = distances.get(currentPoint) + 1;
                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        queue.add(new PQNode(neighbor, newDistance));
                    }
                }
            }
        }

        // Find the shortest path to get to the end point.
        return distances.get(end);
    }

    // Helper method that when given a grid, returns a list of all of its starting points (i.e. points whose
    // char is 'a' or 'S')
    private static List<Point> findStartingPoints(char[][] grid) {
        List<Point> startingPoints = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == 'a' || grid[i][j] == 'S') {
                    startingPoints.add(new Point(j, i));
                }
            }
        }
        return startingPoints;
    }

    // Part 2: We repeatedly run Dijkstra's algorithm on each starting point and return the minimum of the
    // results. Because part 1 of this problem is already an implementation of Dijkstra's algorithm given a start
    // and end point, we just call the part1 method instead.
    private static int part2(char[][] grid, Point end) {
        List<Point> startingPoints = findStartingPoints(grid);

        int minSteps = Integer.MAX_VALUE;

        for (Point start : startingPoints) {
            int steps = part1(grid, start, end);
            minSteps = Math.min(steps, minSteps);
        }

        return minSteps;
    }

    // Part 2 Post-Mortem: Upon finishing this problem, we discovered that we don't necessarily need to repeatedly
    // call Dijkstra's algorithm for each starting point. Instead, in order to find the fewest steps required to
    // move starting from any starting point to the end point, we can reverse the order of the start and end points
    // and treat our end point as our source and find the minimum distance to any starting point (as our distance).
}
