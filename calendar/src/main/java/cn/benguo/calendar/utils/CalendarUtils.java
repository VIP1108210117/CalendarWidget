package cn.benguo.calendar.utils;


import java.util.Map;

/**
 * Created by pyt on 2017/3/16.
 */
public class CalendarUtils {
    /**
     * TODO 没有初始化（缺少数据）
     */
    private static Map<String, int[]> sAllHolidays;

    /**
     * 目前没什么用
     * @param year
     * @param month
     * @return
     */
    public static int[] getHolidays(int year, int month) {
        int holidays[];
        if (sAllHolidays != null) {
            holidays = sAllHolidays.get(year + "" + month);
            if (holidays == null) {
                holidays = new int[42];
            }
        } else {
            holidays = new int[42];
        }
        return holidays;
    }

}

