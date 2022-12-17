import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Class to represent a sensor.
// It stores the sensor's absolute position and the absolute position of its closest beacon.
// It also calculates the Manhattan distance between the sensor and its closest beacon.
class Sensor {
    private final Point position;
    private final Point closestBeacon;
    private final int distanceToClosestBeacon;

    public Sensor(Point position, Point closestBeacon) {
        this.position = position;
        this.closestBeacon = closestBeacon;
        this.distanceToClosestBeacon = Math.abs(position.x - closestBeacon.x) + Math.abs(position.y - closestBeacon.y);
    }

    public Point getPosition() { return position; }
    public Point getClosestBeacon() { return closestBeacon; }
    public int getDistanceToClosestBeacon() { return distanceToClosestBeacon; }
}

public class Day15_Beacon_Exclusion_Zone {
    public static void main(String[] args) {
        File file = new File("./inputs/day15/day15.txt");
        List<Sensor> sensors = new ArrayList<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                // compile the regex to create pattern
                // using compile() method
                Pattern pattern = Pattern.compile("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)");

                // get a matcher object from pattern
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    int x1 = Integer.parseInt(matcher.group(1));
                    int y1 = Integer.parseInt(matcher.group(2));
                    int x2 = Integer.parseInt(matcher.group(3));
                    int y2 = Integer.parseInt(matcher.group(4));

                    maxX = Math.max(maxX, x1);
                    maxX = Math.max(maxX, x2);
                    minX = Math.min(minX, x1);
                    minX = Math.min(minX, x2);

                    maxY = Math.max(maxY, y1);
                    maxY = Math.max(maxY, y2);
                    minY = Math.min(minY, y1);
                    minY = Math.min(minY, y2);

                    Point sensor = new Point(x1, y1);
                    Point beacon = new Point(x2, y2);
                    sensors.add(new Sensor(sensor, beacon));
                }
            }

            int part1 = part1(sensors, 2000000, minX, maxX);
            System.out.println("Part 1 is: " + part1);

            long part2 = part2(sensors);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Calculates the Manhattan distance between two points.
    private static int getManhattanDistance(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    // Returns true if the given point is already occupied by a sensor or by a beacon.
    // Returns false otherwise.
    private static boolean isPointOccupied(List<Sensor> sensors, Point point) {
        for (Sensor sensor : sensors) {
            if (point.equals(sensor.getPosition()) || point.equals(sensor.getClosestBeacon())) {
                return true;
            }
        }

        return false;
    }

    // Returns true if the given point is within a sensor's coverage area.
    // Returns false otherwise.
    private static boolean isPointWithinSensorArea(Sensor sensor, Point point) {
        int distFromRowToSensor = getManhattanDistance(point, sensor.getPosition());
        return distFromRowToSensor <= sensor.getDistanceToClosestBeacon();
    }

    // Part 1: Returns the number of points in a given row where the hidden beacon *cannot* be present.
    // We do this by taking every point in the given row (in which the boundaries are expanded to accommodate
    // for extra points) and calculating its distance to every sensor. As long as that point is not presently occupied,
    // and that the distance from that point to a sensor is less than or equal to the sensor's distance to its own
    // closest beacon, then we know that that point cannot be the location of the hidden beacon. In other words,
    // the sensor knows its closest beacon position so if there's a point closer to the sensor, it can't possibly be
    // another beacon. We add all of these points to a set which removes duplicates for us and return the size of the set.
    private static int part1(List<Sensor> sensors, int row, int minX, int maxX) {
        Set<Point> pointsWhereBeaconCannotBePresent = new HashSet<>();

        // Arbitrary extension of row search range.
        for (int i = minX * 2; i <= maxX * 2; i++) {
            Point pointOnRow = new Point(i, row);

            if (isPointOccupied(sensors, pointOnRow))
                continue;

            for (Sensor sensor : sensors) {
                if (isPointWithinSensorArea(sensor, pointOnRow)) {
                    pointsWhereBeaconCannotBePresent.add(pointOnRow);
                }
            }
        }

        return pointsWhereBeaconCannotBePresent.size();
    }

    // Part 2: Finds the single point in an area the range of points (0,0) to (4000000, 4000000) in which
    // the sensor/beacon coverage area does not cover. Naively, we can run the same approach to part 1
    // for every single point in our range. However, that is wildly inefficient and may take several hours to days to
    // run. We observe that each sensor radiates a diamond-shaped coverage area such that the closest beacon is
    // at the edge of that area. This single point we're trying to find must be at the edge of these coverage areas.
    // Therefore, instead of searching every single point, we can simply search the points along the perimeter
    // of each diamond-shaped coverage area which reduces our search space. However, another approach to this
    // is that if we know that a point is within the area covered by a sensor, do we really need to iterate through
    // all the rest of the points in that row? No, we can actually "hop" to the edge of the sensor's area and continue
    // from there which is what this algorithm implements.
    private static long part2(List<Sensor> sensors) {
        int row = 0;
        int col = 0;

        boolean distressBeaconFound = false;
        boolean pointWithinSensorArea = false;

        Point pointConsidering = new Point(0,0);

        // If we haven't found our point yet, keep going.
        while (!distressBeaconFound) {
            pointConsidering.x = col;
            pointConsidering.y = row;

            // Find the sensor whose coverage area this point is in.
            for (Sensor sensor : sensors) {
                // If we found one, then our point is clearly not the distress beacon.
                if (isPointWithinSensorArea(sensor, pointConsidering)) {
                    // Calculate the new 'x' position on the edge of the sensor's area.
                    int yDistAwayFromSensor = Math.abs(sensor.getPosition().y - row);
                    col = sensor.getDistanceToClosestBeacon() + sensor.getPosition().x - yDistAwayFromSensor + 1;

                    // If we've passed the bounds of what we need to consider,
                    // move to the next row and start again from the beginning.
                    if (col >= 4000000) {
                        col = 0;
                        row++;
                    }

                    pointWithinSensorArea = true;
                    break;
                }
            }

            if (pointWithinSensorArea) {
                pointWithinSensorArea = false;
                continue;
            };

            // If our point is not in any sensor's coverage area, then we've found our distress beacon!
            distressBeaconFound = true;
        }

        return (long)pointConsidering.x * 4000000 + pointConsidering.y;
    }
}