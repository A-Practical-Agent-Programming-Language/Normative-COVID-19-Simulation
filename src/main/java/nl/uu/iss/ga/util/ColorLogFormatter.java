package main.java.nl.uu.iss.ga.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.*;

public class ColorLogFormatter extends Formatter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private final Date dat = new Date();
    private static final String format = "%2$tb %2$td, %2$tY %2$tH:%2$tM:%2$tS %3$s%n%1$s%5$s: %6$s%7$s%n";
//    private static final String format = "%5$s: [%2$tb %2$td, %2$tY %2$tl:%2$tM:%2$tS] %3$s %4$s %6$s %7$s%n";

    @Override
    public String format(LogRecord logRecord) {
        dat.setTime(logRecord.getMillis());

        String source = sourceToAbbreviation(logRecord);

        String message = formatMessage(logRecord);
        String throwable = "";
        if (logRecord.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            logRecord.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }

        switch (logRecord.getLevel().toString()) {
            case "INFO":
                return String.format(format, ANSI_CYAN, dat, source, logRecord.getLoggerName(),
                        logRecord.getLevel().getLocalizedName(), message + ANSI_RESET, throwable);
            case "WARNING":
                return String.format(format, ANSI_YELLOW, dat, source, logRecord.getLoggerName(),
                        logRecord.getLevel().getLocalizedName(), message + ANSI_RESET, throwable);
            case "SEVERE":
                return String.format(format, ANSI_RED, dat, source, logRecord.getLoggerName(),
                        logRecord.getLevel().getLocalizedName(), message + ANSI_RESET, throwable);
            default:
                return String.format(format, "", dat, source, logRecord.getLoggerName(),
                        logRecord.getLevel().getLocalizedName(), message, throwable);
        }
    }

    private String sourceToAbbreviation(LogRecord logRecord) {
        StringBuilder b = new StringBuilder();
        if (logRecord.getSourceClassName() != null) {
            String[] segments = logRecord.getSourceClassName().split("\\.");
            for(int i = 0; i < segments.length - 1; i++) {
                b.append(segments[i].charAt(0)).append(".");
            }
            b.append(segments[segments.length-1]);
            if (logRecord.getSourceMethodName() != null) {
                b.append(" ").append(logRecord.getSourceMethodName());
            }
        } else {
            b.append(logRecord.getLoggerName());
        }
        return b.toString();
    }
}
