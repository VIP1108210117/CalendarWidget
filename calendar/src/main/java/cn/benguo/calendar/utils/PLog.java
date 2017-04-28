package cn.benguo.calendar.utils;

import android.util.Log;

/**
 * Created by Administrator on 2017/3/17.
 */

public class PLog {

    private static final String TAG = "PLog";


        public static void log() {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            StringBuilder builder = new StringBuilder();
            boolean isStartAppend = false;
            for (int i = 0; i < elements.length; i++) {
                if (isStartAppend) {
                    builder.append("--").append(" at  ").append(elements[i].toString()).append("\n");
                }
                if (elements[i].getClassName().equals(PLog.class.getName())) {
                    isStartAppend = true;
                }
            }
            Log.e(TAG, builder.toString());
        }




}
