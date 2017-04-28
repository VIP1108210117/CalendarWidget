package cn.benguo.calendar.month;

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
 * 月份日历控件，绘制某月份的日历内容<br/>
 */
public class MonthView extends View {

    private static final int NUM_COLUMNS = 7;
    private static final int NUM_ROWS = 6;
    /**
     * 用于画圆的画笔
     */
    private Paint mCirclePaint;
    private Paint mDayPaint;
    private Paint mLunarPaint;
    private int mNormalDayColor;
    private int mSelectDayColor;
    private int mSelectBGColor;
    private int mSelectBGTodayColor;
    private int mHintCircleColor;
    private int mLunarTextColor;
    private int mHolidayTextColor;
    private int mOtherMonthTextColor;
    private int mColumnSize, mRowSize, mSelectCircleSize;
    private float mDaySize;
    private float mLunarTextSize;
    private int mCircleRadius = 6;
    private int[][] mDaysText;
    private int[] mHolidays;
    private String[][] mHolidayOrLunarText;
    private boolean mIsShowLunar;
    private boolean mIsShowHint;
    private boolean mIsShowHolidayHint;
    private OnMonthClickListener mDateClickListener;
    private GestureDetector mGestureDetector;
    private List<Integer> mTaskHintList;
    private Bitmap mRestBitmap, mWorkBitmap;
    /**
     * 记录该控件当前绘制的是那年哪月的日历
     */
    private int mYear, mMonth;
    /**
     * 当前日历所属月份的1号
     */
    private DateTime mCurrentMonthFirstDay;
    /**
     * 当前日历第一天的日期
     */
    private DateTime mDateOfCalendarFirstDay;
    /**
     * 当前日历最后一天的日期
     */
    private DateTime mDateOfCalendarLastDay;
    /**
     * 当前被选中的日期
     */
    protected static DateTime mSelectedDate;

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initPaint();
        initGestureDetector();
        initData();
    }


    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CalendarView,
                R.attr.calendarViewStyle, 0);
        mSelectDayColor = array.getColor(R.styleable.CalendarView_selectedDayTextColor, Color.parseColor("#FFFFFF"));
        mSelectBGColor = array.getColor(R.styleable.CalendarView_selectedDayCircleColor, Color.parseColor("#4588E3"));
        mSelectBGTodayColor = array.getColor(R.styleable.CalendarView_selectedTodayCircleColor, mSelectBGColor);
        mNormalDayColor = array.getColor(R.styleable.CalendarView_normalDayTextColor, Color.parseColor("#575471"));
//        mCurrentDayColor = array.getColor(R.styleable.CalendarView_todayTextColor, Color.parseColor("#FF8594"));
        mHintCircleColor = array.getColor(R.styleable.CalendarView_hintCircleColor, Color.parseColor("#FE8595"));
        mOtherMonthTextColor = array.getColor(R.styleable.CalendarView_otherMonthDayTextColor, Color.parseColor("#ACA9BC"));
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
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mSelectBGColor);

        mDayPaint = new Paint();
        mDayPaint.setAntiAlias(true);
        mDayPaint.setTextSize(mDaySize);

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
     * 初始化数据
     */
    private void initData() {
        if(mSelectedDate == null){
            // 默认选中今天
            mSelectedDate = new DateTime();
        }
        mDaysText = new int[NUM_ROWS][NUM_COLUMNS];
        mHolidayOrLunarText = new String[NUM_ROWS][NUM_COLUMNS];
        //设置默认绘制月份为本月
        setDateForDraw(new DateTime());
    }

    /**
     * 设置需要绘制某月份的日历（如：dateTime为2017年4月12日时，则绘制的是2017年4月的日历）
     * @param dateTime
     */
    public void setDateForDraw(DateTime dateTime) {
        mCurrentMonthFirstDay = dateTime.dayOfMonth().withMinimumValue();//本月第一天
        mDateOfCalendarFirstDay = JODAUtils.getCalendarFirstDay(mCurrentMonthFirstDay);
        mDateOfCalendarLastDay = JODAUtils.getCalendarLastDay(mCurrentMonthFirstDay);
        mYear = mCurrentMonthFirstDay.getYear();
        mMonth = mCurrentMonthFirstDay.getMonthOfYear();
        if(mIsShowHolidayHint){
            mHolidays = CalendarUtils.getHolidays(mYear, mMonth);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = getResources().getDimensionPixelSize(R.dimen.calendar_month_view_height);
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = getResources().getDimensionPixelSize(R.dimen.calendar_width);
        }
        setMeasuredDimension(widthSize, heightSize);
        initSize();
    }

    private void initSize() {
        mColumnSize = getMeasuredWidth() / NUM_COLUMNS;
        mRowSize = getMeasuredHeight() / NUM_ROWS;
        mSelectCircleSize = (int) (mColumnSize / 3.2);
        while (mSelectCircleSize > mRowSize / 2) {
            mSelectCircleSize = (int) (mSelectCircleSize / 1.3);
        }
    }

    private void clearData() {
        mDaysText = new int[NUM_ROWS][NUM_COLUMNS];
        mHolidayOrLunarText = new String[NUM_ROWS][NUM_COLUMNS];
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        initSize();
//        clearData();
        drawMonth(canvas);
        drawLunarText(canvas);
        drawHoliday(canvas);
    }

    private void drawMonth(Canvas canvas) {
        DateTime today = new DateTime();
        int thisMonth = mCurrentMonthFirstDay.getMonthOfYear();
        DateTime date;
        int day;
        String dayString;
        int i = 0;
        for(int row = 0; row < NUM_ROWS; row++){
            for(int column = 0; column < NUM_COLUMNS; column++){
                date = mDateOfCalendarFirstDay.plusDays(i++);
                day = date.getDayOfMonth();
                mDaysText[row][column] = day;
                mHolidayOrLunarText[row][column] = JODAUtils.getHolidayFromSolar(date);
                dayString = String.valueOf(mDaysText[row][column]);
                int startX = (int) (mColumnSize * column + (mColumnSize - mDayPaint.measureText(dayString)) / 2);
                int startY = (int) (mRowSize * row + mRowSize / 2 - (mDayPaint.ascent() + mDayPaint.descent()) / 2);
                // 选中日期的背景圆的外切矩形的四个顶点
                int startRecX = mColumnSize * column;
                int startRecY = mRowSize * row;
                int endRecX = startRecX + mColumnSize;
                int endRecY = startRecY + mRowSize;
                if (JODAUtils.isEqualDate(date, mSelectedDate)) {
                    // 是被选中的日期，绘制实心圆
                    mCirclePaint.setStyle(Paint.Style.FILL);
                    if (JODAUtils.isEqualDate(date, today)) {
                        // 被选中的日期是今天，无透明色
                        mCirclePaint.setColor(mSelectBGTodayColor);
                    } else {
                        // 被选中的日期是普通的日期，透明色
                        mCirclePaint.setColor(mSelectBGColor);
                    }
                    canvas.drawCircle((startRecX + endRecX) / 2, (startRecY + endRecY) / 2,
                            mSelectCircleSize, mCirclePaint);
                }else if (JODAUtils.isEqualDate(date, today)) {
                    // 当今天没被选中时，绘制空心圆
                    mCirclePaint.setStyle(Paint.Style.STROKE);
                    mCirclePaint.setColor(mSelectBGTodayColor);
                    canvas.drawCircle((startRecX + endRecX) / 2, (startRecY + endRecY) / 2,
                            mSelectCircleSize, mCirclePaint);
                }
                if (JODAUtils.isEqualDate(date, mSelectedDate)) {
                    mDayPaint.setColor(mSelectDayColor);
                }else if(date.getMonthOfYear() == thisMonth){
                    mDayPaint.setColor(mNormalDayColor);
                }else{
                    mDayPaint.setColor(mOtherMonthTextColor);
                }
                canvas.drawText(dayString, startX, startY, mDayPaint);
                // 绘制提示小圆点
                drawHintCircle(row, column, day, canvas);
            }
        }
    }

    /**
     * 绘制农历
     *
     * @param canvas
     */
    private void drawLunarText(Canvas canvas) {
        if (mIsShowLunar) {
            int year = mYear;
            //month = 一月：0，十二月：11
            int month = mMonth - 1;
            int firstYear, firstMonth, firstDay;
            // weekNumber = 周日：1，周六：7
            int weekNumber = JODAUtils.getFirstDayWeek(year, month + 1) % 7 +1;
            if (weekNumber == 1) {
                firstYear = year;
                firstMonth = month + 1;
                firstDay = 1;
            } else {
                int monthDays;
                if (month == 0) {
                    firstYear = year - 1;
                    firstMonth = 11;
                    monthDays = JODAUtils.getMonthDays(firstYear, firstMonth + 1);
                    firstMonth = 12;
                } else {
                    firstYear = year;
                    firstMonth = month - 1;
                    monthDays = JODAUtils.getMonthDays(firstYear, firstMonth + 1);
                    firstMonth = month;
                }
                firstDay = monthDays - weekNumber + 2;
            }
            LunarCalendarUtils.Lunar lunar = LunarCalendarUtils.solarToLunar(new LunarCalendarUtils.Solar(firstYear, firstMonth, firstDay));
            int days;
            int day = lunar.lunarDay;
            int leapMonth = LunarCalendarUtils.leapMonth(lunar.lunarYear);
            days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
            for (int i = 0; i < 42; i++) {
                int column = i % 7;
                int row = i / 7;
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
                if (row == 0 && mDaysText[row][column] >= 23 || row >= 4 && mDaysText[row][column] <= 14) {
                    mLunarPaint.setColor(mLunarTextColor);
                } else {
                    mLunarPaint.setColor(mHolidayTextColor);
                }
                String dayString = mHolidayOrLunarText[row][column];
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day);
                }
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarDayString(day);
                    mLunarPaint.setColor(mLunarTextColor);
                }
                int startX = (int) (mColumnSize * column + (mColumnSize - mLunarPaint.measureText(dayString)) / 2);
                int startY = (int) (mRowSize * row + mRowSize * 0.72 - (mLunarPaint.ascent() + mLunarPaint.descent()) / 2);
                canvas.drawText(dayString, startX, startY, mLunarPaint);
                day++;
            }
        }
    }

    /**
     * 绘制是否上班的标记
     * @param canvas
     */
    private void drawHoliday(Canvas canvas) {
        if (mIsShowHolidayHint) {
            Rect rect = new Rect(0, 0, mRestBitmap.getWidth(), mRestBitmap.getHeight());
            Rect rectF = new Rect();
            int distance = (int) (mSelectCircleSize / 2.5);
            for (int i = 0; i < mHolidays.length; i++) {
                int column = i % 7;
                int row = i / 7;
                rectF.set(mColumnSize * (column + 1) - mRestBitmap.getWidth() - distance, mRowSize * row + distance, mColumnSize * (column + 1) - distance, mRowSize * row + mRestBitmap.getHeight() + distance);
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
    private void drawHintCircle(int row, int column, int day, Canvas canvas) {
        if (mIsShowHint && mTaskHintList != null && mTaskHintList.size() > 0) {
            if (!mTaskHintList.contains(day)) return;
            mDayPaint.setColor(mHintCircleColor);
            float circleX = (float) (mColumnSize * column + mColumnSize * 0.5);
            float circleY = (float) (mRowSize * row + mRowSize * 0.75);
            canvas.drawCircle(circleX, circleY, mCircleRadius, mDayPaint);
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
     * 处理点击事件
     * @param x
     * @param y
     */
    private void doClickAction(int x, int y) {
        if (y > getHeight())
            return;
        int row = y / mRowSize;
        int column = x / mColumnSize;
        column = Math.min(column, 6);
        int clickYear = mYear, clickMonth = mMonth;
        if (row == 0) {
            if (mDaysText[row][column] >= 23) {
                if (mMonth == 1) {
                    clickYear = mYear - 1;
                    clickMonth = 12;
                } else {
                    clickYear = mYear;
                    clickMonth = mMonth - 1;
                }
                setSelectedDate(clickYear, clickMonth, mDaysText[row][column]);
                if (mDateClickListener != null) {
                    mDateClickListener.onClickLastMonth(
                            clickYear, clickMonth, mDaysText[row][column]);
                }
            } else {
                clickThisMonth(clickYear, clickMonth, mDaysText[row][column]);
            }
        } else {
            int monthDays = JODAUtils.getMonthDays(mYear, mMonth);
            int weekNumber = JODAUtils.getFirstDayWeek(mYear, mMonth);
            int nextMonthDays = 42 - monthDays - weekNumber%7 + 1;
            if (mDaysText[row][column] <= nextMonthDays && row >= 4) {
                if (mMonth == 12) {
                    clickYear = mYear + 1;
                    clickMonth = 1;
                } else {
                    clickYear = mYear;
                    clickMonth = mMonth + 1;
                }
                setSelectedDate(clickYear, clickMonth, mDaysText[row][column]);
                if (mDateClickListener != null) {
                    mDateClickListener.onClickNextMonth(
                            clickYear, clickMonth, mDaysText[row][column]);
                }

            } else {
                clickThisMonth(clickYear, clickMonth, mDaysText[row][column]);
            }
        }
    }

    /**
     * 点击本月的某天
     *
     * @param year
     * @param month
     * @param day
     */
    public void clickThisMonth(int year, int month, int day) {
        setSelectedDate(year, month, day);
        if (mDateClickListener != null) {
            mDateClickListener.onClickThisMonth(year, month, day);
        }
    }

    /**
     * 设置选中的日期
     * @param date
     */
    public void setSelectedDate(DateTime date) {
        if(JODAUtils.isEqualDate(mSelectedDate, date)){
            return;
        }
        mSelectedDate = date;
        invalidate();
    }

    /**
     * 设置选中的日期
     * @param year
     * @param month
     * @param day
     */
    public void setSelectedDate(int year, int month, int day) {
        setSelectedDate(mSelectedDate.withDate(year, month, day));
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

    /**
     * 设置点击日期监听
     *
     * @param dateClickListener
     */
    public void setOnDateClickListener(OnMonthClickListener dateClickListener) {
        this.mDateClickListener = dateClickListener;
    }


    /**
     * 获取当前选中的日期
     * @return
     */
    public DateTime getSelectedDate(){
        return mSelectedDate;
    }

    /**
     * 当前日历所属月份的1号
     * @return
     */
    public DateTime getCurrentMonthFirstDay(){
        return mCurrentMonthFirstDay;
    }

    /**
     * 当前日历的第一天
     * @return
     */
    public DateTime getFirstDay(){
        return mDateOfCalendarFirstDay;
    }

    /**
     * 当前日历的最后一天
     * @return
     */
    public DateTime getLastDay(){
        return mDateOfCalendarLastDay;
    }

    /**
     * 获取当前日历对应的年份
     *
     * @return
     */
    public int getYear() {
        return mYear;
    }

    /**
     * 获取当前日历对应的月份
     *
     * @return
     */
    public int getMonth() {
        return mMonth;
    }

}

