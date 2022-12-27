import java.io.File;
import java.io.IOException;
import java.util.*;

public class Day25_Full_of_Hot_Air {
    public static void main(String[] args) {
        File file = new File("./inputs/day25/day25.txt");
        List<String> snafus = new ArrayList<>();

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                snafus.add(sc.nextLine());
            }

            String part1 = part1(snafus);
            System.out.println("Part 1 is: " + part1);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Given a snafu number, converts it to a decimal (base 10).
    // Iterate from 0 to the highest power of 5 represented in the snafu number
    // and determine the coefficient based off of the value of the character.
    private static long snafuToDecimal(String snafu) {
        long decimal = 0L;

        for (int i = snafu.length() - 1; i >= 0; i--) {
            int power = (snafu.length() - 1) - i;
            long base = (long)Math.pow(5, power);

            char c = snafu.charAt(i);

            int multiplier;
            if (c == '-') {
                multiplier = -1;
            } else if (c == '=') {
                multiplier = -2;
            } else {
                multiplier = Integer.parseInt(String.valueOf(c));
            }
            decimal += (multiplier * base);
        }

        return decimal;
    }

    // Given a decimal number, converts it to quinary (base 5).
    private static String decimalToQuinary(long decimal) {
        int power = 0;
        StringBuilder sb = new StringBuilder();

        // Start by finding the largest exponent that when 5 is raised to that exponent,
        // the resulting number is less than or equal to the given decimal.
        while (Math.pow(5, power) < decimal) {
            power++;
        }

        // At this point, the power is too big, so we drop it down by one.
        power -= 1;

        // Now, we start from the largest power and work our way down.
        // We determine what is the maximum coefficient we need to not go over our decimal value.
        // Once we determine that, we subtract the value from our decimal value and repeat.
        while (power >= 0) {
            long base = (long) Math.pow(5, power);
            int multiplier = (int)(decimal / base);

            sb.append(multiplier);

            decimal = decimal - (multiplier * base);

            power--;
        }

        return sb.toString();
    }

    // Given a quinary (base 5) number, convert it to balanced quinary.
    // Balanced quinary is a quinary number but using -2,-1,0,1,2 instead of 0,1,2,3,4.
    private static String convertQuinaryToBalancedQuinary(String quinary) {
        StringBuilder sb = new StringBuilder();
        boolean carryOne = false;

        // Moving from right to left, after applying any carry-over,
        // if we encounter or have the carry-over result be a 0, 1, or 2, leave it be.
        // If we encounter or have the carry-over result be a 3 replace it with a '=' and add 1 to the next digit.
        // If we encounter or have the carry-over result be a 4 replace it with a '-' and add 1 to the next digit.
        // If we encounter or have the carry-over result be a 5 replace it with a '0' and add 1 to the next digit.
        for (int i = quinary.length() - 1; i >= 0; i--) {
            char c = quinary.charAt(i);

            if (carryOne) {
                if (c == '0') {
                    sb.append("1");
                    carryOne = false;
                } else if (c == '1') {
                    sb.append("2");
                    carryOne = false;
                } else if (c == '2') {
                    sb.append("=");
                } else if (c == '3') {
                    sb.append("-");
                } else if (c == '4') {
                    sb.append("0");
                }
            } else {
                if (c == '0') {
                    sb.append("0");
                } else if (c == '1') {
                    sb.append("1");
                } else if (c == '2') {
                    sb.append("2");
                } else if (c == '3') {
                    sb.append("=");
                    carryOne = true;
                } else if (c == '4') {
                    sb.append("-");
                    carryOne = true;
                }
            }
        }

        if (carryOne) {
            sb.append("1");
        }

        return sb.reverse().toString();
    }


    // Part 1: We start by converting all the snafu numbers to decimal and summing them up.
    // Then we convert our final decimal back into balanced quinary. We do this in a two-step process.
    // First, we convert the decimal to a quinary (base 5) and then from quinary to balanced quinary.
    private static String part1(List<String> snafus) {
        long sum = 0L;

        for (String snafu : snafus) {
            sum += snafuToDecimal(snafu);
        }

        String quinary = decimalToQuinary(sum);
        return convertQuinaryToBalancedQuinary(quinary);
    }
}
