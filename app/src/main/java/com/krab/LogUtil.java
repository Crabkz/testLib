package com.krab;

import android.util.Log;


public class LogUtil {
    private static final String DEFAULT_TAG = "krab";
    private static final int MAX_LENGTH = 3000;
    public static boolean DEBUG = BuildConfig.DEBUG;
    private static String SEPARATOR = "|";

    public static void setSep(String separator) {
        SEPARATOR = separator;
    }

    public static void v(Object... logs) {
        if (DEBUG) {
            for (String s : formatLog(logs)) {
                Log.v(DEFAULT_TAG, s);
            }
        }
    }

    public static void d(Object... logs) {
        if (DEBUG) {
            for (String s : formatLog(logs)) {
                Log.d(DEFAULT_TAG, s);
            }
        }
    }

    public static void i(Object... logs) {
        if (DEBUG) {
            for (String s : formatLog(logs)) {
                Log.i(DEFAULT_TAG, s);
            }
        }
    }


    public static void w(Object... logs) {
        if (DEBUG) {
            for (String s : formatLog(logs)) {
                Log.w(DEFAULT_TAG, s);
            }
        }
    }


    public static void e(Object... logs) {
        for (String s : formatLog(logs)) {
            if (DEBUG) {
                Log.e(DEFAULT_TAG, s);
            }
        }
    }

    public static void vt(String tag, Object... logs) {
        if (DEBUG) {
            for (String s : formatLog(logs)) {
                Log.v(tag, s);
            }
        }
    }

    public static void dt(String tag, Object... logs) {
        if (DEBUG) {
            for (String s : formatLog(logs)) {
                Log.d(tag, s);
            }
        }
    }

    public static void it(String tag, Object... logs) {
        if (DEBUG) {
            for (String s : formatLog(logs)) {
                Log.i(tag, s);
            }
        }
    }


    public static void wt(String tag, Object... logs) {
        if (DEBUG) {
            for (String s : formatLog(logs)) {
                Log.w(tag, s);
            }
        }
    }


    public static void et(String tag, Object... logs) {
        for (String s : formatLog(logs)) {
            if (DEBUG) {
                Log.e(tag, s);
            }
        }
    }


    private static String[] formatLog(Object... logs) {
        if (logs.length <= 0) {
            return new String[]{"logs null"};
        }
        StringBuilder sb = new StringBuilder();
        //拼接
        for (int i = 0; i < logs.length; i++) {
            Object log = logs[i];
            if (i == 0) {
                sb.append(log);
            } else {
                sb.append(SEPARATOR).append(log);
            }
        }
        //按最大长度切割
        String[] array = new String[sb.length() / MAX_LENGTH + 1];
        String totalLog = sb.toString();
        int totalLength = totalLog.length();
        if (totalLength <= 0) {
            array[0] = " ";
        } else if (totalLength <= MAX_LENGTH) {
            array[0] = totalLog;
        } else {
            int index = 0;
            String temp;
            while (index < array.length) {
                if (totalLength <= (index + 1) * MAX_LENGTH) {
                    temp = totalLog.substring(index * MAX_LENGTH);
                } else {
                    temp = totalLog.substring(index * MAX_LENGTH, (index + 1) * MAX_LENGTH);
                }
                array[index] = temp;
                index++;
            }
        }
        return array;
    }
}
