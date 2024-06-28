package fr.epita.assistants.myide.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class Logger {

    private static final String RESET_TEXT = "\u001B[0m";
    private static final String RED_TEXT = "\u001B[31m";

    private static int lastTestMileage = 0;
    private static boolean inTest = false;
    private static int lastTestMileageError = 0;

    private static String lastError = "";

    private static String getDate() {
        return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public static void log(String message) {
        String toLog = "[" + getDate() + "] " + message;
        if (inTest) {
            lastTestMileage += toLog.length();
        }

        System.out.println(toLog);
    }

    public static void logError(String message)
    {
        lastError = message;
        String errorMessage = RED_TEXT + "[" + getDate() + "] " + message + RESET_TEXT;

        if (inTest) {
            lastTestMileageError += errorMessage.length();
        }

        System.err.println(errorMessage);
    }

    public static String getLastError() {
        return lastError;
    }

    public static void startTesting() {
        inTest = true;
        lastTestMileageError = 0;
        lastTestMileage = 0;
    }

    public static void stopTesting() {
        inTest = false;
        lastTestMileageError = 0;
        lastTestMileage = 0;
    }

    public static boolean isInTest() {
        return inTest;
    }

    public static int getCurrentTestMileage() {
        if (!inTest) {
            return -1;
        }

        return lastTestMileage;
    }

    public static int getCurrentTestMileageError() {
        if (!inTest) {
            return -1;
        }
        return lastTestMileageError;
    }


}