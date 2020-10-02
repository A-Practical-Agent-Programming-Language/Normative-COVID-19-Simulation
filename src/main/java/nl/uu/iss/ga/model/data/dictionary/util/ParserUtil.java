package main.java.nl.uu.iss.ga.model.data.dictionary.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParserUtil {
    public static final String SPLIT_CHAR = ",";
    public static final char ESCAPE_CHAR = '"';

    public static Map<String, String> zipLine(String[] headers, String line) {
        String[] values = line.split(SPLIT_CHAR, -1);
        return zipLine(headers, values);
    }

    public static Map<String, String> zipLine(String[] headers, String[] values) {
        if(values.length != headers.length)
            throw new IllegalArgumentException("Headers and values require the same amount of values");

        Map<String, String> zipped = new HashMap<>();
        for(int i = 0; i < headers.length; i++) {
            zipped.put(headers[i], values[i]);
        }
        return zipped;
    }

    public static Map<String, String> zipEscapedCSVLine(String[] headers, String line) {
        String[] values = splitEscapedCSVLine(line);
        return zipLine(headers, values);
    }

    // Yes, at some point using an existing library for CSV would be easier
    public static String[] splitEscapedCSVLine(String line) {
        ArrayList<String> values = new ArrayList<>();
        int startIndex = 0;
        boolean inEscape = false;
        for(int i = 0; i < line.length(); i++) {
            if(line.charAt(i) == ESCAPE_CHAR) {
                inEscape = !inEscape;
            } else if(line.charAt(i) == SPLIT_CHAR.charAt(0) && !inEscape) {
                values.add(getEscapedCSVValue(line, startIndex, i));
                startIndex = i+1;
            }
        }
        values.add(getEscapedCSVValue(line, startIndex, line.length()));
        return values.toArray(new String[0]);
    }

    private static String getEscapedCSVValue(String line, int start, int end) {
        String substr = line.substring(start, end);
        if(substr.startsWith("\""))
            substr = substr.substring(1);
        if(substr.endsWith("\""))
            substr = substr.substring(0, substr.length()-1);
        return substr.replace("\"\"", "\"");
    }

    public static int parseIntInString(String stringWithInt) {
        return ParserUtil.parseAsInt(stringWithInt.replaceAll("[^0-9]", ""));
    }

    public static int parseAsInt(String intValue) {
        try {
            return Integer.parseInt(intValue);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static long parseAsLong(String longValue) {
        try {
            return Long.parseLong(longValue);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static double parseAsDouble(String doubleValue) {
        return Double.parseDouble(doubleValue);
    }

    /**
     * TODO, this is used for flag parameters, with 1 = True, 0 = False. However, driver and passenger flag both sometimes appear as -1 or 2???
     * @param intValue
     * @return
     */
    public static boolean parseIntAsBoolean(String intValue) {
        try {
            int i = parseAsInt(intValue);
            return i > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
