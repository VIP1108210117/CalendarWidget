package cn.benguo.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by pyt on 2017/3/16.
 * <h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 星期条控件，用于绘制星期日到星期六的文本<br/>
 */
public class WeekBar extends View {

    private int mWeekTextColor;
    private int mSplitLineColor;
    private int mWeekSize;
    private Paint mPaint;
    private String[] mWeekString;

    public WeekBar(Context context) {
        this(context, null);
    }

    public WeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWeekString = context.getResources().getStringArray(R.array.calendar_week);
        initAttrs(attrs);
        initPaint();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs,
                R.styleable.WeekBar, R.attr.calendarViewStyle, 0);
        mWeekSize = ta.getDimensionPixelSize(R.styleable.WeekBar_week_text_size,
                getResources().getDimensionPixelSize(R.dimen.week_text_size));
        mWeekTextColor = ta.getColor(R.styleable.WeekBar_week_text_color,
                getResources().getColor(R.color.week_text_color));
        mSplitLineColor = ta.getColor(R.styleable.WeekBar_split_line_color,
                getResources().getColor(R.color.split_line_color));
        ta.recycle();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(mWeekTextColor);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mWeekSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = getResources().getDimensionPixelSize(R.dimen.calendar_week_bar_height);
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = getResources().getDimensionPixelSize(R.dimen.calendar_width);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mWeekTextColor);
        int width = getWidth();
        int height = getHeight();
        int columnWidth = width / 7;
        for (int i = 0; i < mWeekString.length; i++) {
            String text = mWeekString[i];
            int fontWidth = (int) mPaint.measureText(text);
            int startX = columnWidth * i + (columnWidth - fontWidth) / 2;
            int startY = (int) (height / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            canvas.drawText(text, startX, startY, mPaint);
        }
        mPaint.setColor(mSplitLineColor);
         //draw line
        canvas.drawLine(0f,height,width,height,mPaint);

    }

}
