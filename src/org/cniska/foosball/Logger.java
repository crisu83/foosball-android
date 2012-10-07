package org.cniska.foosball;

import android.util.Log;

public class Logger {

    // Static variables
    // ----------------------------------------

    private static boolean logging = true;
    private static int level = Log.ERROR;

    // Methods
    // ----------------------------------------

    private Logger() {
    }

	/**
	 * Logs a debug message.
	 *
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int debug(String tag, String msg) {
        return logging && level >= Log.DEBUG ? Log.d(tag, msg) : 0;
    }

	/**
	 * Logs an error message.
	 *
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int error(String tag, String msg) {
        return logging && level >= Log.ERROR  ? Log.e(tag, msg) : 0;
    }

	/**
	 * Logs an info message.
	 *
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int info(String tag, String msg) {
        return logging && level >= Log.INFO ? Log.i(tag, msg) : 0;
    }

	/**
	 * Logs a verbose message.
	 *
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int verbose(String tag, String msg) {
        return logging && level >= Log.VERBOSE ? Log.v(tag, msg) : 0;
    }

	/**
	 * Logs a warning message.
	 *
	 * @param tag The message source.
	 * @param msg The message to log.
	 * @return True if the message was logged, otherwise false.
	 */
    public static int warn(String tag, String msg) {
        return logging && level >= Log.WARN ? Log.w(tag, msg) : 0;
    }

    // Getters and setters
    // ----------------------------------------

    public static void setLogging(boolean logging) {
        Logger.logging = logging;
    }

    public static void setLevel(int level) {
        Logger.level = level;
    }
}
