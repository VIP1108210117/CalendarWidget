package cn.benguo.calendar.month;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import cn.benguo.calendar.OnCalendarClickListener;

import org.joda.time.DateTime;

/**
 * Created by pyt on 2017/3/16.<br/>
 * <h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 标准日历控件，每页为月份日历控件<br/>
 * @see MonthAdapter
 * @see MonthView
 */
public class MonthViewPager extends ViewPager implements OnMonthClickListener {

    private MonthAdapter mMonthAdapter;

	private OnCalendarClickListener mOnCalendarClickListener;

	public MonthViewPager(Context context) {
        this(context, null);
    }

    public MonthViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

	/**
	 * 初始化月历适配器
	 * @param preMonths 日历可显示今日之前的几个月
	 * @param nextMonths 日历可显示今日之后的几个月
	 */
    public void initAdapter(int preMonths, int nextMonths){
		mMonthAdapter = new MonthAdapter(getContext(), this,
				preMonths, nextMonths);
		setAdapter(mMonthAdapter);
		setCurrentItem(preMonths, false);
	}

	/**
	 * 设置点击日期监听
	 * @param onCalendarClickListener
	 */
	public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
		mOnCalendarClickListener = onCalendarClickListener;
	}

	@Override
	public void onClickThisMonth(int year, int month, int day) {
		if (mOnCalendarClickListener != null) {
			mOnCalendarClickListener.onClickDate(year, month, day);
		}
	}

	@Override
	public void onClickLastMonth(int year, int month, int day) {
		MonthView monthDateView = mMonthAdapter.getViews().get(getCurrentItem() - 1);
		if (monthDateView != null) {
			monthDateView.setSelectedDate(year, month, day);
			setCurrentItem(getCurrentItem() - 1, true);
		}
		onClickThisMonth(year, month, day);
	}

	@Override
	public void onClickNextMonth(int year, int month, int day) {
		MonthView monthDateView = mMonthAdapter.getViews().get(getCurrentItem() + 1);
		if (monthDateView != null) {
			monthDateView.setSelectedDate(year, month, day);
			setCurrentItem(getCurrentItem() + 1, true);
		}
		onClickThisMonth(year, month, day);
	}

	/**
	 * 获取指定位置上对应的月份控件
	 * @param position
	 * @return
	 */
	public MonthView getMonthView(int position) {
		MonthView monthView = mMonthAdapter.getViews().get(position);
		if (monthView == null) {
			monthView = mMonthAdapter.instanceMonthView(position);
		}
		return monthView;
	}

	/**
	 * 获取当前页面对应的月份控件
	 * @return
	 */
    public MonthView getCurrentMonthView() {
		return getMonthView(getCurrentItem());
    }

	/**
	 * 跳转到"今天"所在的页面
	 * @param isClick 是否执行点击事件
	 */
	public void showTodayPage(boolean isClick) {
		setCurrentItem(mMonthAdapter.getTodayPagePosition(), false);
		MonthView monthView = getCurrentMonthView();
		if (monthView != null) {
			DateTime today = new DateTime();
			if(isClick){
				monthView.clickThisMonth(today.getYear(),
						today.getMonthOfYear(), today.getDayOfMonth());
			}else{
				monthView.setSelectedDate(today);
			}
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
    public boolean scrollPreviousMonth() {
        setCurrentItem(getCurrentItem() - 1, true);
        return hasPreviousPage();
    }

    /**
     * 下一页
     * @return 是否还有下一页
     */
    public boolean scrollNextMonth() {
        setCurrentItem(getCurrentItem() + 1, true);
        return hasNexPage();
    }

	/**
	 * 刷新当前页、前一页、后一页的日历视图内容
	 */
	public void invalidateCalendarView() {
		int currentItem = getCurrentItem();
		try {
			getMonthView(getCurrentItem()).invalidate();
			if(currentItem == 0){
				getMonthView(1).invalidate();
			}else if(currentItem == mMonthAdapter.getCount() - 1){
				getMonthView(currentItem-1).invalidate();
			}else{
				getMonthView(currentItem-1).invalidate();
				getMonthView(currentItem+1).invalidate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
