import java.io.File;
import java.io.IOException;
import java.util.*;

record PacketPair(List<Object> packetLeft, List<Object> packetRight) {}

// Because we wrote a comparator in part 1 of the problem, we call it here
// when attempting to sort a list of packets.
class PacketComparator implements Comparator<List<Object>> {
    @Override
    public int compare(List<Object> packet1, List<Object> packet2) {
        return Day13_Distress_Signal.comparePackets(packet1, packet2);
    }
}

public class Day13_Distress_Signal {
    public static void main(String[] args) {
        File file = new File("./inputs/day13/day13.txt");
        List<PacketPair> pairs = new ArrayList<>();

        try {
            Scanner sc = new Scanner(file);

            List<Object> leftPacket = null;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                Stack<Object> stack = new Stack<>();

                if (line.equals("")) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();

                // Iterate through the packet string and add each character (except for the commas)
                // to our stack. If we come across a closing bracket (']'), we pop off all
                // the characters until we hit an opening bracket ('[') and add it to a list.
                // We put this list back on the stack and repeat until a single parent list emerges.
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == ',') {
                        // We may have generated a string representing a multi-digit number so add it onto our stack.
                        if (sb.length() > 0) {
                            stack.push(sb.toString());
                            sb.setLength(0);
                        }
                        continue;
                    }

                    if (c == ']') {
                        // If we have anything left in our string builder, put it onto the stack now.
                        if (sb.length() > 0) {
                            stack.push(sb.toString());
                            sb.setLength(0);
                        }
                        List<Object> sublist = new ArrayList<>();

                        // Repeatedly pop off items in the stack until we hit an opening bracket.
                        while (!stack.peek().equals('[')) {
                            Object top = stack.pop();
                            sublist.add(top);
                        }

                        stack.pop(); // Pop off the opening bracket
                        Collections.reverse(sublist); // Because elements were added in reverse order, we reverse it here.
                        stack.push(sublist); // Add our sublist onto our stack as it may be part of a parent list.
                    } else if (c == '['){
                        stack.push(c);
                    } else {
                        // For characters that are integers, we could have numbers with more
                        // than one digit so we accumulate it here into a StringBuilder.
                        sb.append(c);
                    }
                }

                // Finally, our packet is the last remaining list in the stack.
                List<Object> packet = (List<Object>)stack.pop();

                // If we've already processed a left packet, then construct our PacketPair class with
                // the newly-processed packet being the right packet. Otherwise, the newly-procesesed packet
                // is the left packet so set it and move to parse the next line.
                if (leftPacket != null) {
                    pairs.add(new PacketPair(leftPacket, packet));
                    leftPacket = null;
                } else {
                    leftPacket = packet;
                }
            }

            int part1 = part1(pairs);
            System.out.println("Part 1 is: " + part1);

            int part2 = part2(pairs);
            System.out.println("Part 2 is: " + part2);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Helper method to try to parse the given Object into an integer.
    // If successful, returns the integer. Otherwise, return null.
    public static Integer parseIntOrNull(Object value) {
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Recursive function that compares the two given packets. Returns -1 if the left packet is "less than"
    // the right packet (meaning that the left and right packets are given in the correct order).
    // Returns 0 if the left packet is "equal to" the right packet. Returns 1 if the left packet
    // is "greater than" the right packet (meaning that the left and right packets are in the wrong order).
    public static Integer comparePackets(List<Object> left, List<Object> right) {
        int index = 0;

        while (index < left.size() && index < right.size()) {
            Object leftValue = left.get(index);
            Object rightValue = right.get(index);

            Integer leftInt = parseIntOrNull(leftValue);
            Integer rightInt = parseIntOrNull(rightValue);

            // If both values are integers, the lower integer should come first.
            // If the left integer is lower than the right integer,
            // the inputs are in the right order. If the left integer is higher
            // than the right integer, the inputs are not in the right order.
            // Otherwise, the inputs are the same integer.
            // Continue checking the next part of the input.
            if (leftInt != null && rightInt != null) {
                if (leftInt < rightInt) {
                    return -1;
                } else if (leftInt > rightInt) {
                    return 1;
                }
            }

            // If both values are lists, recursively compare their inner values.
            if (leftValue instanceof List<?> && rightValue instanceof List<?>) {
                Integer status = comparePackets((List<Object>)leftValue, (List<Object>)rightValue);
                if (status != null) {
                    return status;
                }
            }

            // If exactly one value is an integer, convert the integer to a list
            // which contains that integer as its only value, then retry the comparison.
            if (leftInt != null && rightInt == null) {
                List<Object> enclosedList = new ArrayList<>();
                enclosedList.add(leftInt);
                Integer status = comparePackets(enclosedList, (List<Object>)rightValue);
                if (status != null) {
                    return status;
                }
            } else if (rightInt != null && leftInt == null) {
                List<Object> enclosedList = new ArrayList<>();
                enclosedList.add(rightInt);
                Integer status = comparePackets((List<Object>)leftValue, enclosedList);
                if (status != null) {
                    return status;
                }
            }

            index++;
        }

        // If the left list runs out of items first, the inputs are in the right order.
        // If the right list runs out of items first, the inputs are in the wrong order.
        if (left.size() < right.size()) {
            return -1;
        } else if (left.size() > right.size()) {
            return 1;
        }

        return null;
    }

    // Part 1: Returns the sum of the indices of pairs that are in the right order.
    private static int part1(List<PacketPair> pairs) {
        // Accumulates the indices of the pairs that are in the right order.
        int rightOrderIndexAccumulator = 0;

        for (int i = 0; i < pairs.size(); i++) {
            PacketPair pair = pairs.get(i);
            Integer status = comparePackets(pair.packetLeft(), pair.packetRight());

            if (status != null && status == -1) {
                rightOrderIndexAccumulator += (i + 1);
            }
        }

        return rightOrderIndexAccumulator;
    }

    // Part 2: Sorts the list of packets and finds the sum of the index of packets [[2]] and [[6]].
    // Because we wrote a comparator in part 1, simply use it to sort a list of packets.
    private static int part2(List<PacketPair> pairs) {
        // Get rid of the pairs of packets and instead consider all the packets as one big list.
        List<List<Object>> packets = new ArrayList<>();
        for (PacketPair pair : pairs) {
            packets.add(pair.packetLeft());
            packets.add(pair.packetRight());
        }

        // Add in two additional packets: [[2]]...
        List<Object> two = new ArrayList<>();
        two.add(2);
        List<Object> twoOuter = new ArrayList<>();
        twoOuter.add(two);
        packets.add(twoOuter);

        // ...and [[6]]
        List<Object> six = new ArrayList<>();
        six.add(6);
        List<Object> sixOuter = new ArrayList<>();
        sixOuter.add(six);
        packets.add(sixOuter);

        // Sort the packets list using the comparator we wrote in part 1.
        packets.sort(new PacketComparator());

        return (packets.indexOf(twoOuter) + 1) * (packets.indexOf(sixOuter) + 1);
    }
}