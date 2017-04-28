package cn.benguo.calendar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import cn.benguo.calendar.indicate.CalendarIndicateView;
import cn.benguo.calendar.month.MonthViewPager;
import cn.benguo.calendar.month.MonthView;
import cn.benguo.calendar.utils.JODAUtils;
import cn.benguo.calendar.week.WeekViewPager;
import cn.benguo.calendar.week.WeekView;

import org.joda.time.DateTime;

/**
 * the calendar view for benguo
 * <p>
 * Created by pyt on 2017/3/16.
 * <h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 可在单行日历和标准日历之间切换的日历组件<br/>
 * 设置日历是否可点击{@link #setClickable(boolean)}，false日历组件不可用<br/>
 * @see CalendarIndicateView
 * @see WeekBar
 * @see MonthViewPager
 * @see WeekViewPager
 */
public class CalendarWidget extends RelativeLayout implements View.OnClickListener, OnSwitchListener, ViewPager.OnPageChangeListener, OnCalendarClickListener {

    private static final String TAG = "BenGuoCalendarView";

    private static final long ANIMATOR_TIME = 300;

    //the year and month indicate
    private CalendarIndicateView calendarIndicateView;

    //the week indicate bar
    private WeekBar weekBar;

    //the month view
    private MonthViewPager monthViewPager;

    //the week view
    private WeekViewPager weekViewPager;

	// 开关条控件
	private SwitchBar switchBar;

    //the calendar container
    private View fl_calendar;

    //the value animator for open
    private ValueAnimator openAnimator;

    //the value animator for close
    private ValueAnimator closeAnimator;

    private boolean isOpen;

    private int mCalendarBgColor;

    private int mWeekBarHeight;
    private int mWeekViewHeight;
    private int mMonthViewHeight;

    private OnCalendarClickListener mOnCalendarClickListener;

	/**
	 * 日历可显示今日之前的几个月
	 */
	private int mTodayPreMonths;
	/**
	 * 日历可显示今日之后的几个月
	 */
	private int mTodayNextMonths;
	/**
	 * 当前被选中的日期(默认为今天的日期)
	 */
	private DateTime mSelectedDate;


	public CalendarWidget(@NonNull Context context) {
        this(context, null);
    }

    public CalendarWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarWidget(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        initView();
		initListener();
        initAnimator();
		showTodayPage(false);
		setClickable(true);
    }

    private void initAttr(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarWidget,
                R.attr.calendarViewStyle, 0);
		mTodayPreMonths = ta.getInteger(R.styleable.CalendarWidget_preMonths, 12);
		mTodayNextMonths = ta.getColor(R.styleable.CalendarWidget_nextMonths, 12);
        mCalendarBgColor = ta.getColor(R.styleable.CalendarWidget_calenderBGColor, Color.WHITE);
		mWeekBarHeight = ta.getDimensionPixelSize(R.styleable.CalendarWidget_weekBarHeight,
				getResources().getDimensionPixelSize(R.dimen.calendar_week_bar_height));
		mWeekViewHeight = ta.getDimensionPixelSize(R.styleable.CalendarWidget_weekViewHeight,
				getResources().getDimensionPixelSize(R.dimen.calendar_week_view_height));
		mMonthViewHeight = ta.getDimensionPixelSize(R.styleable.CalendarWidget_monthViewHeight,
				getResources().getDimensionPixelSize(R.dimen.calendar_month_view_height));
        ta.recycle();
    }

	private void initView() {
		inflate(getContext(), R.layout.calendar_widget, this);
		// 初始化控件
        calendarIndicateView = (CalendarIndicateView) findViewById(R.id.calendarIndicateView);
        weekBar = (WeekBar) findViewById(R.id.weekBarView);
        weekViewPager = (WeekViewPager) findViewById(R.id.weekViewPager);
        monthViewPager = (MonthViewPager) findViewById(R.id.monthViewPager);
		switchBar = (SwitchBar) findViewById(R.id.switchBar);
		fl_calendar = findViewById(R.id.fl_calendar);

		// 设置控件的高度
		weekBar.getLayoutParams().height = mWeekBarHeight;
		weekViewPager.getLayoutParams().height = mWeekViewHeight;
		monthViewPager.getLayoutParams().height = mMonthViewHeight;
		// 设置背景色
		calendarIndicateView.setBackgroundColor(mCalendarBgColor);
		weekBar.setBackgroundColor(mCalendarBgColor);
		fl_calendar.setBackgroundColor(mCalendarBgColor);
		switchBar.setCircleBackgroundColor(mCalendarBgColor);

		// 初始化标准日历的页面适配器
		monthViewPager.initAdapter(mTodayPreMonths, mTodayNextMonths);
		// 根据月份个数计算一共有多少周，并初始化单行日历的页面适配器
		DateTime dateOfToday = new DateTime();
		DateTime startCalendarFirstDay = JODAUtils.getCalendarFirstDay(dateOfToday.plusMonths(-mTodayPreMonths));
		DateTime lastCalendarLastDay = JODAUtils.getCalendarLastDay(dateOfToday.plusMonths(mTodayNextMonths));
		int todayPreWeeks = JODAUtils.weeksBetween(startCalendarFirstDay, dateOfToday);
		int todayNextWeeks = JODAUtils.weeksBetween(dateOfToday, lastCalendarLastDay);
		weekViewPager.initAdapter(todayPreWeeks, todayNextWeeks);
		//init current year and month display
		changeSelectedDate(dateOfToday);
    }

	/**
	 * 初始化事件监听
	 */
	private void initListener() {
		switchBar.setOnClickListener(this);
		calendarIndicateView.setOnSwitchListener(this);
		monthViewPager.addOnPageChangeListener(this);
		weekViewPager.addOnPageChangeListener(this);
		monthViewPager.setOnCalendarClickListener(this);
		weekViewPager.setOnCalendarClickListener(this);
	}

    private void initAnimator() {
		final float animatorLength = mMonthViewHeight - mWeekViewHeight;
        openAnimator = ValueAnimator.ofFloat(1f);
        openAnimator.setDuration(ANIMATOR_TIME);
        openAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        openAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float animatedValue = (Float) animation.getAnimatedValue();
                float value = animatedValue;
                if (value <= 1.0f) {
                    //the container height animator
                    View containerView = findViewById(R.id.fl_calendar);
                    ViewGroup.LayoutParams layoutParams = containerView.getLayoutParams();
                    layoutParams.height = (int) (mWeekViewHeight + animatorLength * value);
                    containerView.setLayoutParams(layoutParams);
                    invalidate();
                    //the month and week alpha animator
                    weekViewPager.setAlpha(1f - value);
                    monthViewPager.setAlpha(value);
                    //the indicate rotation animator
                    switchBar.animator(180 * value);

                }
            }
        });

        openAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isOpen = true;
                weekViewPager.setVisibility(GONE);
            }
        });

        closeAnimator = ValueAnimator.ofFloat(1f);
        closeAnimator.setDuration(ANIMATOR_TIME);
        closeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        closeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float animatedValue = (Float) animation.getAnimatedValue();
                float value = animatedValue;
                if (value <= 1.0f) {
                    //the container height animator
                    View containerView = findViewById(R.id.fl_calendar);
                    ViewGroup.LayoutParams layoutParams = containerView.getLayoutParams();
                    layoutParams.height = (int) (mWeekViewHeight + animatorLength * (1f - value));
                    containerView.setLayoutParams(layoutParams);
                    invalidate();
                    //the month and week alpha animator
                    weekViewPager.setAlpha(value);
                    monthViewPager.setAlpha(1f - value);
                    //the indicate rotation animator
                    switchBar.animator(180 - 180 * value);

                }
            }
        });

        closeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isOpen = false;
                monthViewPager.setVisibility(GONE);

            }
        });
    }

	/**
	 * 设置日历是否可点击
	 * @param clickable false : 拦截触摸事件，true 不拦截
	 */
	@Override
	public void setClickable(boolean clickable) {
		super.setClickable(clickable);
	}

	/**
	 * 日历不可点击时，拦截触摸事件
	 * @param ev
	 * @return
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return !isClickable();
	}

	/**
	 * when you change year or month or date
	 * you call this method to refresh calendar indicate
	 */
	private void refreshCalendarIndicate(int year, int month) {
		calendarIndicateView.refreshCalendarIndicate(year, month);
	}

	/**
	 * 刷新日历指示器组件中“翻页按钮”的显示状态
	 * @param leftBtnVisibility 左侧按钮是否可见，没有上一页时不可见
	 * @param rightBtnVisibility 右侧按钮是否可见，没有下一页时不可见
	 */
	private void refreshButtonVisibility(boolean leftBtnVisibility, boolean rightBtnVisibility) {
		calendarIndicateView.setButtonVisibility(leftBtnVisibility, rightBtnVisibility);
	}

	@Override
	public void onClick(View v) {
		switchOpenStatus();
	}

    @Override
    public boolean onLeftClick() {
        boolean result;
        if (isOpen) {
            result = monthViewPager.scrollPreviousMonth();
        } else {
            result = weekViewPager.scrollPreviousWeek();
        }
        return result;
    }


    @Override
    public boolean onRightClick() {
        boolean result;
        if (isOpen) {
            result = monthViewPager.scrollNextMonth();
        } else {
            result = weekViewPager.scrollNextWeek();
        }
        return result;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.i(TAG, "onPageSelected ： position=" + position);
        if (isOpen) {
            MonthView currentMonthView = monthViewPager.getCurrentMonthView();
            refreshCalendarIndicate(currentMonthView.getYear(), currentMonthView.getMonth());
			refreshButtonVisibility(monthViewPager.hasPreviousPage(), monthViewPager.hasNexPage());
        } else {
            WeekView currentWeekView = weekViewPager.getCurrentWeekView();
			refreshCalendarIndicate(currentWeekView.getYear(), currentWeekView.getMonth());
            refreshButtonVisibility(weekViewPager.hasPreviousPage(), weekViewPager.hasNexPage());
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

	@Override
	public void onClickDate(int year, int month, int day) {
		DateTime newSelectedDate = mSelectedDate.withDate(year, month, day);
		if(changeSelectedDate(newSelectedDate)){
			// 避免重复点击同一日期
			monthViewPager.invalidateCalendarView();
			weekViewPager.invalidateCalendarView();
			if (mOnCalendarClickListener != null) {
				mOnCalendarClickListener.onClickDate(year, month, day);
			}
		}
	}

	/**
	 * 设置日历的点击事件监听
	 * @param listener
	 */
	public void setOnCalendarClickListener(OnCalendarClickListener listener) {
		this.mOnCalendarClickListener = listener;
	}

	/**
	 * 单行日历和标准日历的切换
	 */
	public void switchOpenStatus() {
		if (openAnimator.isRunning() || closeAnimator.isRunning()) {
			return;
		}
		if (isOpen) {
			refreshWeekCalendarView();
			closeAnimator.start();
			weekViewPager.setVisibility(VISIBLE);
		} else {
			refreshMonthCalendarView();
			openAnimator.start();
			monthViewPager.setVisibility(VISIBLE);
		}
	}

	/**
	 * 从单行日历切换到标准日历，刷新月份日历控件：<br/>
	 * 如果单行日历当前页有被选中的日期，<br/>
	 * 则切换到选中日期所在月份对应的标准日历页，否则切换到本周第一天所在月份对应的标准日历页；<br/>
	 */
	private void refreshMonthCalendarView() {
		//当前页面对应的星期控件
		WeekView currentWeekView = weekViewPager.getCurrentWeekView();
		//当前 WeekView 的第一天
		DateTime currentWeekFirstDay = currentWeekView.getFirstDay();
		if(currentWeekFirstDay.isBefore(monthViewPager.getCurrentMonthView().getFirstDay()) ||
				currentWeekFirstDay.isAfter(monthViewPager.getCurrentMonthView().getLastDay())){
			//当前 WeekView 的第一天不在当前 MonthView 中，需要计算月份偏移量

			DateTime targetDate;//目标日期
			/** 当 mSelectedDate 在当前 WeekView 中时，
			 *  targetDate = mSelectedDate所在日历的第一天,
			 *  否则targetDate = WeekView 的第一天所在日历的第一天
			 **/
			if(mSelectedDate.isAfter(currentWeekView.getFirstDay().minusDays(1)) &&
					mSelectedDate.isBefore(currentWeekView.getLastDay().plusDays(1))){
				targetDate = JODAUtils.getCalendarFirstDay(mSelectedDate);
			}else{
				targetDate = JODAUtils.getCalendarFirstDay(currentWeekView.getFirstDay());
			}


			// originDate 为切换视图之前，当前 MonthView 所对应的日历的第一天
			DateTime originDate = monthViewPager.getCurrentMonthView().getFirstDay();
			int days = JODAUtils.daysBetween(originDate, targetDate);
			if(days < 0){
				// 目标日期在源日期前面，令originDate指向原日历页的第二天
				originDate = originDate.plusDays(1);
			}else{
				// 目标日期在源日期前面，令originDate指向前一页日历的最后一天
				originDate = originDate.minusDays(1);
			}

			// 计算MonthViewPager页面索引值的偏移量
			int offsetMonths = JODAUtils.monthsBetween(originDate, targetDate);
			monthViewPager.setCurrentItem(monthViewPager.getCurrentItem()
					+ offsetMonths, false);
		}


		//重设MonthView当前选中日期，并重绘日历
		MonthView monthView = monthViewPager.getCurrentMonthView();
		if (monthView != null) {
			monthView.setSelectedDate(mSelectedDate);
			//刷新指示器标题
			refreshCalendarIndicate(monthView.getYear(),
					monthView.getMonth());
		}
		//刷新指示器按钮
		refreshButtonVisibility(monthViewPager.hasPreviousPage(), monthViewPager.hasNexPage());
	}

	/**
	 * 从标准日历切换到单行日历，刷新星期日历控件:<br/>
	 * 如果标准日历当前页有被选中的日期，<br/>
	 * 则切换到被选中日期所在的那个星期对应的单行日历，否则切换到本月第一周对应的单行日历；<br/>
	 */
	private void refreshWeekCalendarView() {
		//当前页面对应的星期控件
		WeekView currentWeekView = weekViewPager.getCurrentWeekView();
		//当前页面对应的月份控件
		MonthView currentMonthView = monthViewPager.getCurrentMonthView();

		DateTime targetDate;// 目标日期，用于计算日历页面的索引值的偏移量
		if(mSelectedDate.isAfter(currentMonthView.getFirstDay().minusDays(1)) &&
				mSelectedDate.isBefore(currentMonthView.getLastDay().plusDays(1))){
			// 被选中的日期在当前日历页中
			targetDate = mSelectedDate;
		}else{
			targetDate = currentMonthView.getCurrentMonthFirstDay();
		}
		// 当targetDate为周末时，使其指向下周一，否则指向本周一
		targetDate = targetDate.plusDays(1).dayOfWeek().withMinimumValue();

		// originDate 切换视图之前，当前 WeekView 的周一
		DateTime originDate = currentWeekView.getFirstDay().plusDays(1);
		int offsetWeeks = JODAUtils.weeksBetween(originDate, targetDate);
		if(JODAUtils.daysBetween(originDate, targetDate)%7 == 6){
			offsetWeeks++;
		}
		if (offsetWeeks != 0) {
			int position = weekViewPager.getCurrentItem() + offsetWeeks;
			weekViewPager.setCurrentItem(position, false);
		}

		//重设单行日历的当前选中日期，并重绘日历
		WeekView weekView = weekViewPager.getCurrentWeekView();
		if (weekView != null) {
			weekView.setSelectedDate(mSelectedDate);
		}

		//刷新指示器
		refreshButtonVisibility(weekViewPager.hasPreviousPage(), weekViewPager.hasNexPage());
		refreshCalendarIndicate(currentMonthView.getYear(),
				currentMonthView.getMonth());
	}

	/**
	 * 跳转到"今天"所在的页面
	 * @param isClick 是否执行点击事件
	 */
	public void showTodayPage(boolean isClick) {
		if (isOpen) {
			monthViewPager.showTodayPage(isClick);
		}else{
			weekViewPager.showTodayPage(isClick);
		}
	}

	/**
	 * 改变被选中的日期
	 * @param date
	 * @return 是否真的改变 ：和之前的一样，则返回false
	 */
	public boolean changeSelectedDate(DateTime date){
		if(JODAUtils.isEqualDate(mSelectedDate, date)){
			return false;
		}
		mSelectedDate = date;
		refreshCalendarIndicate(mSelectedDate.getYear(), mSelectedDate.getMonthOfYear());
		return true;
	}

	/**
	 * 获取当前日历选中的日期
	 * @return
	 */
	public DateTime getSelectedDate(){
		return mSelectedDate;
	}

	/**
	 * 日历是否展开
	 * @return
	 */
	public boolean isOpen() {
		return isOpen;
	}

}