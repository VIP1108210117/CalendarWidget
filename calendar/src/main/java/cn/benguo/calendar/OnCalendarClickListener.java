package cn.benguo.calendar;

/**
 * Created by pyt on 2017/3/16.
 */
public interface OnCalendarClickListener {

    /**
     * 判断指定日期是否可以被点击
     * @param year
     * @param month
     * @param day
     * @return
     */
//    boolean isClickable(int year, int month, int day);

    /**
     * 用户点击某日期时，回调该方法
     * @param year
     * @param month
     * @param day
     */
    void onClickDate(int year, int month, int day);
}
