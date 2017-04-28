package cn.benguo.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by pyt on 2017/3/20.
 * <h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 开关条控件<br/>
 *
 */

public class SwitchBar extends View {

    private Paint mPaint;

    private GestureDetector mGestureDetector;

    private float mAngle =0f;

    private float mRadius;

    private int mCircleBackgroundColor;

    private int mLineColor;

    private int[] mArrowBitmapWidthAndHeight;

    private RectF oval = new RectF();

    private Bitmap mArrowBitmap;

    private OnClickListener onClickListener;


    public SwitchBar(Context context) {
        this(context,null);
    }

    public SwitchBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SwitchBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleBackgroundColor = Color.TRANSPARENT;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        mArrowBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_open, options);
        mArrowBitmapWidthAndHeight= new int[]{
                mArrowBitmap.getWidth(), mArrowBitmap.getHeight()};
        mGestureDetector = new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                doClickAction(e);
                return true;
            }
        });

    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SwitchBar,
                R.attr.calendarViewStyle, 0);
        mLineColor = ta.getColor(R.styleable.SwitchBar_split_line_color,
                getResources().getColor(R.color.split_line_color));
        mRadius = ta.getDimensionPixelSize(R.styleable.SwitchBar_switchBtnRadius,
                getResources().getDimensionPixelSize(R.dimen.switch_btn_radius));
        ta.recycle();
    }


    /**
     * operate the click event
     * @param e
     */
    private void doClickAction(MotionEvent e) {

        float x = e.getX();
        float y = e.getY();
        int width = getWidth();
        int height = getHeight();
        if (x > width - 3 * mRadius &&  x < width - mRadius && y > 0 && y < height) {
            onClickListener.onClick(this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(widthSize, (int) (mRadius+0.5));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //用手势处理点击事件
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        //draw first line
        mPaint.setColor(mLineColor);
        canvas.drawLine(0f, 0f, width - 3 * mRadius, 0f,mPaint);
        //draw arc
        mPaint.setColor(mCircleBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        oval.set(width - 3 * mRadius, -mRadius, width - mRadius, height);
        canvas.drawArc(oval,0f,180,true,mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(1);
        canvas.drawArc(oval,0f,180,false,mPaint);
        //draw second line
        canvas.drawLine(width -  mRadius, 0f, width, 0f,mPaint);
        //draw bitmap
        canvas.rotate(mAngle,width-2*mRadius,mRadius/2);
        canvas.drawBitmap(mArrowBitmap,width-2*mRadius-mArrowBitmapWidthAndHeight[0]/2,mRadius/2-mArrowBitmapWidthAndHeight[1]/2,mPaint);

    }

    /**
     *
     * @param angle
     */
    public void animator(float angle){
        this.mAngle = angle;
        invalidate();
    }

    public void setCircleBackgroundColor(int color) {
        this.mCircleBackgroundColor = color;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
       mArrowBitmap.recycle();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener = l;
    }
}
