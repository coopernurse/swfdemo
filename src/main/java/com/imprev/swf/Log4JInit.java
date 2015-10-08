package com.imprev.swf;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SyslogAppender;

/**
 * Author: James Cooper - james@bitmechanic.com
 * Date: 10/7/15
 */
public class Log4JInit {

    private static final Logger log = Logger.getLogger(Log4JInit.class);

    public static void removeAppendersAndInit() {
        Logger.getRootLogger().removeAllAppenders();
        init();
    }

    public static void init() {
        Logger rootLogger = Logger.getRootLogger();
        if (!rootLogger.getAllAppenders().hasMoreElements()) {

            String output         = "stdout";
            String syslogHost     = System.getenv("SYSLOG_HOST");
            String syslogFacility = env("SYSLOG_FACILITY", "LOCAL6").toUpperCase();
            String logLevel       = env("LOG4J_LEVEL", "DEBUG").toUpperCase();
            String logPrefix      = env("LOG_PREFIX", "");

            Logger.getLogger("org.eclipse.jetty").setLevel(Level.WARN);
            Logger.getLogger("org.apache").setLevel(Level.INFO);
            Logger.getLogger("org.springframework").setLevel(Level.WARN);
            Logger.getLogger("com.sun").setLevel(Level.INFO);
            Logger.getLogger("sun.net.www").setLevel(Level.INFO);
            Logger.getLogger("javax").setLevel(Level.INFO);
            Logger.getLogger("spark").setLevel(Level.INFO);
            Logger.getLogger("com.mchange").setLevel(Level.INFO);
            Logger.getLogger("com.googlecode.flyway").setLevel(Level.INFO);
            Logger.getLogger("httpclient").setLevel(Level.INFO);
            Logger.getLogger("org.apache.http").setLevel(Level.INFO);
            Logger.getLogger("com.amazonaws").setLevel(Level.INFO);
            Logger.getLogger("jdbc.sqlonly").setLevel(Level.INFO);
            Logger.getLogger("java.sql").setLevel(Level.INFO);

            Level lev = Level.toLevel(logLevel, Level.DEBUG);
            Logger.getLogger("com.imprev").setLevel(lev);
            rootLogger.setLevel(lev);

            if (syslogHost != null && !syslogHost.isEmpty()) {
                output = "syslog host: " + syslogHost + " facility: " + syslogFacility;
                if (!logPrefix.isEmpty()) {
                    logPrefix += ": ";
                }
                String layout = logPrefix + "%5p [%t] %c - %m%n";

                int facility = SyslogAppender.getFacility(syslogFacility);
                if (facility == -1) {
                    facility = SyslogAppender.LOG_LOCAL6;
                }
                rootLogger.addAppender(new SyslogAppender(new PatternLayout(layout), syslogHost, facility));
            }
            else {
                rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%d %5p %x [%t] (%F:%L) - %m%n")));
            }

            log.info("Default logging configuration enabled to: " + output + " with level: " + lev);
        }
    }

    static String env(String key, String defaultVal) {
        String val = System.getenv(key);
        if (val == null) {
            val = defaultVal;
        }
        return val;
    }

}
