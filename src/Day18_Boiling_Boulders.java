import java.io.File;
import java.io.IOException;
import java.util.*;

// Represents a cube with x, y, and z dimensions.
class Cube {
    private final int x;
    private final int y;
    private final int z;

    public Cube (int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cube cube = (Cube) o;
        return x == cube.x && y == cube.y && z == cube.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public String toString() { return "(" + x + "," + y + "," + z + ")"; }
}

public class Day18_Boiling_Boulders {
    public static void main(String[] args) {
        File file = new File("./inputs/day18/day18.txt");
        List<Cube> cubes = new ArrayList<>();

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] tokens = line.split(",");
                int x = Integer.parseInt(tokens[0]);
                int y = Integer.parseInt(tokens[1]);
                int z = Integer.parseInt(tokens[2]);

                cubes.add(new Cube(x, y, z));
            }

            int part1 = part1(cubes);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(cubes);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Two cubes are touching if they share the same plane (two of the dimensions are the same)
    // and the other coordinate value is only one away from the other.
    private static boolean areTwoCubesTouching(Cube c1, Cube c2) {
        if (c1.getX() == c2.getX() && c1.getY() == c2.getY() && Math.abs(c1.getZ() - c2.getZ()) == 1) {
            return true;
        }

        if (c1.getY() == c2.getY() && c1.getZ() == c2.getZ() && Math.abs(c1.getX() - c2.getX()) == 1) {
            return true;
        }

        if (c1.getX() == c2.getX() && c1.getZ() == c2.getZ() && Math.abs(c1.getY() - c2.getY()) == 1) {
            return true;
        }

        return false;
    }

    // Returns the surface area of the structure represented by the list of cubes.
    // First, we start off by counting the number of sides that are touching.
    // Two cubes touching counts as two sides touching.
    // Because each cube has six sides, if two of them are touching, then it would subtract
    // two sides from the total number of sides. That is the formula used to calculate the surface area.
    private static int calculateSurfaceArea(List<Cube> cubes) {
        int numSidesTouching = 0;

        for (Cube cube1 : cubes) {
            for (Cube cube2 : cubes) {
                if (cube1.equals(cube2))
                    continue;

                if (areTwoCubesTouching(cube1, cube2)) {
                    numSidesTouching++;
                }
            }
        }

        return 6 * cubes.size() - numSidesTouching;
    }

    // Part 1: Returns the surface area of the structure represented by the list of cubes which forms the lava droplet.
    // (including interior surface area).
    private static int part1(List<Cube> cubes) {
        return calculateSurfaceArea(cubes);
    }

    // Returns a triplet of three dimensions represented as a cube of the largest x, y, and z dimensions.
    private static Cube findBoundingBoxDimensions(List<Cube> cubes) {
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;

        for (Cube c : cubes) {
            maxX = Math.max(maxX, c.getX());
            maxY = Math.max(maxY, c.getY());
            maxZ = Math.max(maxZ, c.getZ());
        }

        return new Cube(maxX+1, maxY+1, maxZ+1);
    }

    // Given a cube, returns the neighbors of that cube (excluding diagonal neighbors).
    // This means that a cube has at most six neighbors, one for each of its six faces.
    // We bound our search space from (0,0,0) to (maxCube.x, maxCube.y, maxCub.z).
    private static List<Cube> getNeighbors(Cube cube, Cube maxCube) {
        List<Cube> neighbors = new ArrayList<>();

        // Create the six directions that a neighbor could be in.
        List<Cube> directions = new ArrayList<>();
        directions.add(new Cube(1,0,0));
        directions.add(new Cube(0,1,0));
        directions.add(new Cube(0,0,1));
        directions.add(new Cube(-1,0,0));
        directions.add(new Cube(0,-1,0));
        directions.add(new Cube(0,0,-1));

        for (Cube direction : directions) {
            int newX = cube.getX() + direction.getX();
            int newY = cube.getY() + direction.getY();
            int newZ = cube.getZ() + direction.getZ();

            // Bounds checking
            if (newX >= 0 && newX < maxCube.getX() &&
                newY >= 0 && newY < maxCube.getY() &&
                newZ >= 0 && newZ < maxCube.getZ()){
                neighbors.add(new Cube(newX, newY, newZ));
            }
        }

        return neighbors;
    }

    // Given a max cube representing the boundaries of the search space,
    // from (0,0,0) to (maxCube.x, maxCube.y, maxCube.z), return a list of every single cube unit in that space.
    private static List<Cube> generateAllGridCubes(Cube maxCube) {
        List<Cube> all = new ArrayList<>();

        for (int x = 0; x < maxCube.getX(); x++) {
            for (int y = 0; y < maxCube.getY(); y++) {
                for (int z = 0; z < maxCube.getZ(); z++) {
                    all.add(new Cube(x, y, z));
                }
            }
        }

        return all;
    }

    // Part 2: Because part 1 calculated the surface area of the lava droplet including pockets of air
    // in the interior of the water droplet, we want to know the exterior surface area instead. The approach
    // here is that we need to know the cubes that make up the interior of the droplet. To do that, we find the
    // bounding box containing the lava droplet in its entirety. Let's call the number of cubes in that bounding
    // box, A. The number of cubes making up the droplet as D. We run a breadth-first-search (BFS) algorithm in the
    // empty space surrounding the droplet, marking cubes as visited and using the droplet itself as a boundary. Let's
    // call the number of cubes in the empty space surrounding the droplet as E.
    // Then, in order for us to find the cubes in the interior of the droplet, it would be A - D - E.
    // However, that would simply be the number of cubes. In order for us to find the surface area of the exterior
    // of the droplet, we would calculate the surface area of the entire droplet (incl. interior) and then calculate
    // the surface area of just the cubes in the interior and subtract them.
    private static int part2(List<Cube> cubes) {
        Cube maxCube = findBoundingBoxDimensions(cubes);

        Set<Cube> visited = new HashSet<>(); // Keep track of visited cubes.

        Queue<Cube> queue = new LinkedList<>();
        queue.add(new Cube(0,0,0)); // Initialize our cube with a cube that we know is not touching our lava droplet.

        // Run BFS on our grid search space.
        while (!queue.isEmpty()) {
            Cube cube = queue.poll();

            visited.add(cube);

            List<Cube> neighbors = getNeighbors(cube, maxCube);

            for (Cube neighbor : neighbors) {
                // We only consider a neighbor if we haven't visited it yet
                // AND if it's not a cube that is part of our lava droplet.
                if (!visited.contains(neighbor) && !cubes.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }

        // Generate all the cubes in the bounding box and subtract out ones
        // that we've visited (empty space outside the droplet) and ones that make up the droplet itself.
        // What's left is the cubes on the inside.
        List<Cube> allCubes = generateAllGridCubes(maxCube);
        allCubes.removeAll(visited);
        allCubes.removeAll(cubes);

        // The exterior surface area is calculated by taking the surface area of the entire droplet
        // (including interior) and subtracting out the surface area of the interior air space.
        return calculateSurfaceArea(cubes) - calculateSurfaceArea(allCubes);
    }
}