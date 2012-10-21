package org.cniska.foosball.android;

import android.util.Log;

/**
 * This class provides functionality for both setting the logging level and disabling logging completely.
 */
public class Logger {

    // Static variables
    // ----------------------------------------

    private static boolean sLogging = true;
    private static int sLevel = Log.ERROR;

    // Methods
    // ----------------------------------------

    private Logger() {
    }

	/**
	 * Logs a debug message.
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int debug(String tag, String msg) {
        return sLogging && sLevel >= Log.DEBUG ? Log.d(tag, msg) : 0;
    }

	/**
	 * Logs an error message.
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int error(String tag, String msg) {
        return sLogging && sLevel >= Log.ERROR ? Log.e(tag, msg) : 0;
    }

	/**
	 * Logs an info message.
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int info(String tag, String msg) {
        return sLogging && sLevel >= Log.INFO ? Log.i(tag, msg) : 0;
    }

	/**
	 * Logs a verbose message.
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int verbose(String tag, String msg) {
        return sLogging && sLevel >= Log.VERBOSE ? Log.v(tag, msg) : 0;
    }

	/**
	 * Logs a warning message.
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int warn(String tag, String msg) {
        return sLogging && sLevel >= Log.WARN ? Log.w(tag, msg) : 0;
    }

    // Getters and setters
    // ----------------------------------------

    public static void setLogging(boolean logging) {
        Logger.sLogging = logging;
    }

    public static void setLevel(int level) {
        Logger.sLevel = level;
    }
}
