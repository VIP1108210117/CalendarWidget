package cn.benguo.calendar.month;

/**
 * Created by pyt on 2017/3/16.
 */
public interface OnMonthClickListener {
    /**
     * 点击本月的日期
     * @param year
     * @param month
     * @param day
     */
    void onClickThisMonth(int year, int month, int day);
    /**
     * 点击上月的日期
     * @param year
     * @param month
     * @param day
     */
    void onClickLastMonth(int year, int month, int day);
    /**
     * 点击下月的日期
     * @param year
     * @param month
     * @param day
     */
    void onClickNextMonth(int year, int month, int day);
}
