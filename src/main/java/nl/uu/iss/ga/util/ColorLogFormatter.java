package nl.uu.iss.ga.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.*;

public class ColorLogFormatter extends Formatter {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001b[30m";
    public static final String ANSI_BRIGHT_BLACK = "\u001b[30;1m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private final Date dat = new Date();
    private static final String format = "%1$s%2$tb %2$td, %2$tY %2$tH:%2$tM:%2$tS %3$s (%8$s) %n\t%5$s: %6$s%7$s%n";
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

        String ANSI_COLOR;

        switch (logRecord.getLevel().toString()) {
            case "FINER":
                ANSI_COLOR = ANSI_BLACK;
                break;
            case "FINE":
                ANSI_COLOR = ANSI_BRIGHT_BLACK;
                break;
            case "INFO":
                ANSI_COLOR = ANSI_CYAN;
                break;
            case "WARNING":
                ANSI_COLOR = ANSI_YELLOW;
                break;
            case "SEVERE":
                ANSI_COLOR = ANSI_RED;
                break;
            default:
                ANSI_COLOR = "";
                break;
        }

        String host;

        try {
            InetAddress ip = InetAddress.getLocalHost();
            host = ip.getHostName();
        } catch (UnknownHostException e) {
            host = "UNKNOWN-HOST";
        }

        if ("".equals(ANSI_COLOR)) {
            return String.format(
                    format, ANSI_COLOR, dat, source, logRecord.getLoggerName(),
                    logRecord.getLevel().getLocalizedName(), message, throwable,
                    host
            );
        } else {
            return String.format(
                    format, ANSI_COLOR, dat, source, logRecord.getLoggerName(),
                    logRecord.getLevel().getLocalizedName(), message + ANSI_RESET, throwable,
                    host
            );
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
