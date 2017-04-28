package cn.benguo.calendar.week;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import cn.benguo.calendar.OnCalendarClickListener;

import org.joda.time.DateTime;


/**
 *
 * Created by pyt on 2017/3/16.<br/>
 * <h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 单行日历控件，每页为星期日历控件<br>
 * @see WeekAdapter
 * @see WeekView
 */
public class WeekViewPager extends ViewPager implements OnWeekClickListener {

    private OnCalendarClickListener mOnCalendarClickListener;
    private WeekAdapter mWeekAdapter;

    public WeekViewPager(Context context) {
        this(context, null);
    }

    public WeekViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 初始化适配器
     * @param preWeeks 日历可显示今日之前的多少周
     * @param nextWeeks 日历可显示今日之后的多少周
     */
    public void initAdapter(int preWeeks, int nextWeeks){
        mWeekAdapter = new WeekAdapter(getContext(), this, preWeeks, nextWeeks);
        setAdapter(mWeekAdapter);
        setCurrentItem(preWeeks, false);
    }

    @Override
    public void onClickDate(int year, int month, int day) {
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(year, month, day);
        }
    }

    /**
     * 设置点击日期监听
     * @param onCalendarClickListener
     */
    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }

    /**
     * 获取指定位置上对应的星期控件
     * @param position
     * @return
     */
    public WeekView getWeekView(int position) {
        WeekView weekView = mWeekAdapter.getViews().get(position);
        if (weekView == null) {
            weekView = mWeekAdapter.instanceWeekView(position);
        }
        return weekView;
    }

    /**
     * 获取当前页面对应的星期控件
     * @return
     */
    public WeekView getCurrentWeekView() {
        return getWeekView(getCurrentItem());
    }

    /**
     * 跳转到"今天"所在的页面
     * @param isClick 是否执行点击事件
     */
    public void showTodayPage(boolean isClick) {
        setCurrentItem(mWeekAdapter.getTodayPagePosition(), false);
        WeekView weekView = getCurrentWeekView();
        if (weekView != null) {
            weekView.clickDate(new DateTime());
        }
    }

    /**
     * 是否还有前一页
     * @return
     */
    public boolean hasPreviousPage() {
        return getCurrentItem() > 0;
    }

    /**
     * 是否还有下一页
     * @return
     */
    public boolean hasNexPage() {
        return getCurrentItem() < getAdapter().getCount() - 1;
    }

    /**
     * 上一页
     * @return 是否还有上一页
     */
    public boolean scrollPreviousWeek() {
        setCurrentItem(getCurrentItem() - 1,true);
        return hasPreviousPage();
    }

    /**
     * 下一页
     * @return 是否还有下一页
     */
    public boolean scrollNextWeek() {
        setCurrentItem(getCurrentItem() + 1, true);
        return hasNexPage();
    }


    /**
     * 刷新当前页、前一页、后一页的日历视图内容
     */
    public void invalidateCalendarView() {
        int currentItem = getCurrentItem();
        try {
            getWeekView(getCurrentItem()).invalidate();
            if(currentItem == 0){
                getWeekView(1).invalidate();
            }else if(currentItem == mWeekAdapter.getCount() - 1){
                getWeekView(currentItem-1).invalidate();
            }else{
                getWeekView(currentItem-1).invalidate();
                getWeekView(currentItem+1).invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
