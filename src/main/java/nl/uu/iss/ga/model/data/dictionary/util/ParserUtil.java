package main.java.nl.uu.iss.ga.model.data.dictionary.util;

import java.util.HashMap;
import java.util.Map;

public class ParserUtil {
    public static final String SPLIT_CHAR = ",";

    public static Map<String, String> zipLine(String[] headers, String line) {
        String[] values = line.split(SPLIT_CHAR, -1);
        if(values.length != headers.length)
            throw new IllegalArgumentException("Headers and values require the same amount of values");

        Map<String, String> zipped = new HashMap<>();
        for(int i = 0; i < headers.length; i++) {
            zipped.put(headers[i], values[i]);
        }
        return zipped;
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
