package cn.benguo.calendar.week;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


import cn.benguo.calendar.utils.CalendarUtils;
import cn.benguo.calendar.utils.JODAUtils;
import cn.benguo.calendar.utils.LunarCalendarUtils;
import cn.benguo.calendar.R;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by pyt on 2017/3/16.<br/>
 * <h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 星期日历控件，绘制某星期的内容，第一天为周末<br/>
 */
public class WeekView extends View {

    private static final int NUM_COLUMNS = 7;
    private Paint mPaint;
    private Paint mLunarPaint;
    private int mNormalDayColor;
    private int mSelectDayColor;
    private int mSelectBGColor;
    private int mSelectBGTodayColor;
    private int mHintCircleColor;
    private int mLunarTextColor;
    private int mHolidayTextColor;

    /**
     * 今天的年月日
     */
//    private int mYearOfToday, mMonthOfToday, mDayOfToday;
    /**
     * 当前选中的年月日
     */
//    private int mSelYear, mSelMonth, mSelDay;
    private int mColumnSize, mRowSize, mSelectCircleSize;
    private float mDaySize;
    private float mLunarTextSize;
    private int mCircleRadius = 6;
    private int[] mHolidays;
    private String mHolidayOrLunarText[];
    private boolean mIsShowLunar;
    private boolean mIsShowHint;
    private boolean mIsShowHolidayHint;
    private OnWeekClickListener mOnWeekClickListener;
    private GestureDetector mGestureDetector;
    private List<Integer> mTaskHintList;
    private Bitmap mRestBitmap, mWorkBitmap;
    /**
     * 当前被选中的日期
     * TODO 提取到父类
     */
    protected static DateTime mSelectedDate;
    /**
     * 当前日历的第一天(周末)
     */
    private DateTime mCurrentWeekFirstDay;

    public WeekView(Context context) {
        this(context, null);
    }

    public WeekView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initPaint();
        initGestureDetector();
        initDateTime();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs,
                R.styleable.CalendarView, R.attr.calendarViewStyle, 0);
        mSelectDayColor = array.getColor(R.styleable.CalendarView_selectedDayTextColor, Color.parseColor("#FFFFFF"));
        mSelectBGColor = array.getColor(R.styleable.CalendarView_selectedDayCircleColor, Color.parseColor("#4588E3"));
        mSelectBGTodayColor = array.getColor(R.styleable.CalendarView_selectedTodayCircleColor, mSelectBGColor);
        mNormalDayColor = array.getColor(R.styleable.CalendarView_normalDayTextColor, Color.parseColor("#575471"));
//        mCurrentDayColor = array.getColor(R.styleable.CalendarView_todayTextColor, Color.parseColor("#FF8594"));
        mHintCircleColor = array.getColor(R.styleable.CalendarView_hintCircleColor, Color.parseColor("#FE8595"));
        mLunarTextColor = array.getColor(R.styleable.CalendarView_dayLunarTextColor, Color.parseColor("#ACA9BC"));
        mHolidayTextColor = array.getColor(R.styleable.CalendarView_holidayColor, Color.parseColor("#A68BFF"));
        mDaySize = array.getDimensionPixelSize(R.styleable.CalendarView_dayTextSize,
                getResources().getDimensionPixelSize(R.dimen.day_text_size));
        mLunarTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_dayLunarTextSize,
                getResources().getDimensionPixelSize(R.dimen.lunar_day_text_size));
        mIsShowHint = array.getBoolean(R.styleable.CalendarView_showTaskHintEnable, true);
        mIsShowLunar = array.getBoolean(R.styleable.CalendarView_showLunarEnable, true);
        mIsShowHolidayHint = array.getBoolean(R.styleable.CalendarView_showHolidayHintEnable, true);
        if(mIsShowHolidayHint){
            // 初始化是否为假期的图片标记
            mRestBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rest_day);
            mWorkBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_work_day);
        }
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mDaySize);

        mLunarPaint = new Paint();
        mLunarPaint.setAntiAlias(true);
        mLunarPaint.setTextSize(mLunarTextSize);
        mLunarPaint.setColor(mLunarTextColor);
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                doClickAction((int) e.getX(), (int) e.getY());
                return true;
            }
        });
    }

    /**
     * 初始化当前绘制的日期(默认为本周)
     */
    private void initDateTime() {
        if(mSelectedDate == null){
            // 默认选中今天
            mSelectedDate = new DateTime();
        }
        //设置默认绘制星期为本周
        setDateForDraw(new DateTime());
    }

    /**
     * 初始化当前绘制的日期
     * @param date
     */
    public void setDateForDraw(DateTime date) {
        // 获取指定日期所在星期的第一天
        mCurrentWeekFirstDay = date.dayOfWeek().withMinimumValue().minusDays(1);
        if(mIsShowHolidayHint){
            int holidays[] = CalendarUtils.getHolidays(
                    mCurrentWeekFirstDay.getYear(), mCurrentWeekFirstDay.getMonthOfYear());
            int row = JODAUtils.getWeekRow(mCurrentWeekFirstDay);
            mHolidays = new int[7];
            System.arraycopy(holidays, row * 7, mHolidays, 0, mHolidays.length);
        }
        if (mIsShowHint) {
            // 初始化任务提示
            initTaskHint(mCurrentWeekFirstDay, mCurrentWeekFirstDay.plusDays(7));
        }
    }


    /**
     * 初始化任务提示
     * @param startDate
     * @param endDate
     */
    private void initTaskHint(DateTime startDate, DateTime endDate) {
//        ScheduleDao dao = ScheduleDao.getInstance(getContext());
//        mTaskHintList = dao.getTaskHintByWeek(startDate.getYear(), startDate.getMonthOfYear() - 1,
//                startDate.getDayOfMonth(), endDate.getYear(), endDate.getMonthOfYear() - 1, endDate.getDayOfMonth());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = getResources().getDimensionPixelSize(R.dimen.calendar_week_view_height);
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = getResources().getDimensionPixelSize(R.dimen.calendar_width);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initSize();
        clearData();
        drawThisWeek(canvas);
        drawLunarText(canvas);
        drawHoliday(canvas);
    }

    private void clearData() {
        mHolidayOrLunarText = new String[7];
    }

    private void initSize() {
        mColumnSize = getWidth() / NUM_COLUMNS;
        mRowSize = getHeight();
        mSelectCircleSize = (int) (mColumnSize / 3.2);
        while (mSelectCircleSize > mRowSize / 2) {
            mSelectCircleSize = (int) (mSelectCircleSize / 1.3);
        }
    }

    private void drawThisWeek(Canvas canvas) {
        DateTime today = new DateTime();
        DateTime date;
        int day;
        String dayString;
        for (int i = 0; i < 7; i++) {
            date = mCurrentWeekFirstDay.plusDays(i);
            day = date.getDayOfMonth();
            dayString = String.valueOf(day);
            int startX = (int) (mColumnSize * i + (mColumnSize - mPaint.measureText(dayString)) / 2);
            int startY = (int) (mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            int startRecX = mColumnSize * i;
            int endRecX = startRecX + mColumnSize;
            if (JODAUtils.isEqualDate(date, mSelectedDate)) {
                // 是被选中的日期，绘制实心圆
                if (JODAUtils.isEqualDate(date, today)) {
                    // 被选中的日期是今天，无透明色
                    mPaint.setColor(mSelectBGTodayColor);
                } else {
                    // 被选中的日期是普通的日期，透明色
                    mPaint.setColor(mSelectBGColor);
                }
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle((startRecX + endRecX) / 2, mRowSize / 2, mSelectCircleSize, mPaint);
                mPaint.setColor(mSelectDayColor);
            }else if (JODAUtils.isEqualDate(date, today)) {
                // 当今天没被选中时，绘制空心圆
                mPaint.setColor(mSelectBGTodayColor);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle((startRecX + endRecX) / 2, mRowSize / 2, mSelectCircleSize, mPaint);
                mPaint.setColor(mNormalDayColor);
            } else {
                mPaint.setColor(mNormalDayColor);
            }
            canvas.drawText(dayString, startX, startY, mPaint);
            mHolidayOrLunarText[i] = JODAUtils.getHolidayFromSolar(date);
            drawHintCircle(i, day, canvas);
        }
    }

    /**
     * 绘制农历
     *
     * @param canvas
     */
    private void drawLunarText(Canvas canvas) {
        if (mIsShowLunar) {
            LunarCalendarUtils.Lunar lunar = LunarCalendarUtils.solarToLunar(
                    new LunarCalendarUtils.Solar(
                            mCurrentWeekFirstDay.getYear(),
                            mCurrentWeekFirstDay.getMonthOfYear(),
                            mCurrentWeekFirstDay.getDayOfMonth()));
            int leapMonth = LunarCalendarUtils.leapMonth(lunar.lunarYear);
            int days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
            int day = lunar.lunarDay;
            for (int i = 0; i < 7; i++) {
                if (day > days) {
                    day = 1;
                    if (lunar.lunarMonth == 12) {
                        lunar.lunarMonth = 1;
                        lunar.lunarYear = lunar.lunarYear + 1;
                    }
                    if (lunar.lunarMonth == leapMonth) {
                        days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
                    } else {
                        lunar.lunarMonth++;
                        days = LunarCalendarUtils.daysInLunarMonth(lunar.lunarYear, lunar.lunarMonth);
                    }
                }
                mLunarPaint.setColor(mHolidayTextColor);
                String dayString = mHolidayOrLunarText[i];
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day);
                }
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarDayString(day);
                    mLunarPaint.setColor(mLunarTextColor);
                }
                int startX = (int) (mColumnSize * i + (mColumnSize - mLunarPaint.measureText(dayString)) / 2);
                int startY = (int) (mRowSize * 0.72 - (mLunarPaint.ascent() + mLunarPaint.descent()) / 2);
                canvas.drawText(dayString, startX, startY, mLunarPaint);
                day++;
            }
        }
    }

    private void drawHoliday(Canvas canvas) {
        if (mIsShowHolidayHint && mHolidays != null) {
            Rect rect = new Rect(0, 0, mRestBitmap.getWidth(), mRestBitmap.getHeight());
            Rect rectF = new Rect();
            int distance = (int) (mSelectCircleSize / 2.5);
            for (int i = 0; i < mHolidays.length; i++) {
                int column = i % 7;
                rectF.set(mColumnSize * (column + 1) - mRestBitmap.getWidth() - distance, distance, mColumnSize * (column + 1) - distance, mRestBitmap.getHeight() + distance);
                if (mHolidays[i] == 1) {
                    canvas.drawBitmap(mRestBitmap, rect, rectF, null);
                } else if (mHolidays[i] == 2) {
                    canvas.drawBitmap(mWorkBitmap, rect, rectF, null);
                }
            }
        }
    }

    /**
     * 绘制圆点提示
     *
     * @param column
     * @param day
     * @param canvas
     */
    private void drawHintCircle(int column, int day, Canvas canvas) {
        if (mIsShowHint && mTaskHintList != null && mTaskHintList.size() > 0) {
            if (!mTaskHintList.contains(day)) return;
            mPaint.setColor(mHintCircleColor);
            float circleX = (float) (mColumnSize * column + mColumnSize * 0.5);
            float circleY = (float) (mRowSize * 0.75);
            canvas.drawCircle(circleX, circleY, mCircleRadius, mPaint);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 根据触摸位置计算点击的日期
     * @param x
     * @param y
     */
    private void doClickAction(int x, int y) {
        if (y > getHeight())
            return;
        int column = x / mColumnSize;
        column = Math.min(column, 6);
        DateTime date = mCurrentWeekFirstDay.plusDays(column);
        clickDate(date);
    }

    /**
     * 点击某日期
     *
     * @param dateTime
     */
    public void clickDate(DateTime dateTime) {
        setSelectedDate(dateTime);
        if (mOnWeekClickListener != null) {
            mOnWeekClickListener.onClickDate(dateTime.getYear(),
                    dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        }
    }

    /**
     * 设置选中的日期，
     * @param date
     */
    public void setSelectedDate(DateTime date) {
        if(JODAUtils.isEqualDate(mSelectedDate, date)){
            return;
        }
        mSelectedDate = date;
        invalidate();
    }

    public void setOnWeekClickListener(OnWeekClickListener onWeekClickListener) {
        mOnWeekClickListener = onWeekClickListener;
    }

    /**
     * 设置圆点提示的集合
     *
     * @param taskHintList
     */
    public void setTaskHintList(List<Integer> taskHintList) {
        mTaskHintList = taskHintList;
        invalidate();
    }

    /**
     * 添加一个圆点提示
     *
     * @param day
     */
    public void addTaskHint(Integer day) {
        if (mTaskHintList != null) {
            if (!mTaskHintList.contains(day)) {
                mTaskHintList.add(day);
                invalidate();
            }
        }
    }

    /**
     * 删除一个圆点提示
     *
     * @param day
     */
    public void removeTaskHint(Integer day) {
        if (mTaskHintList != null) {
            if (mTaskHintList.remove(day)) {
                invalidate();
            }
        }
    }

    public DateTime getSelectedDate() {
        return mSelectedDate;
    }

    /**
     * 当前日历的第一天(周末)
     * @return
     */
    public DateTime getFirstDay(){
        return mCurrentWeekFirstDay;
    }

    /**
     * 当前日历的最后一天(周六)
     * @return
     */
    public DateTime getLastDay(){
        return mCurrentWeekFirstDay.plusDays(6);
    }


    /**
     * 获取当前日历对应的年份
     *
     * @return
     */
    public int getYear() {
        return mCurrentWeekFirstDay.getYear();
    }

    /**
     * 获取当前日历对应的月份
     *
     * @return
     */
    public int getMonth() {
        return mCurrentWeekFirstDay.getMonthOfYear();
    }

}
