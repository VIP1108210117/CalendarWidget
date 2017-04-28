package cn.benguo.calendar.indicate;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.benguo.calendar.OnSwitchListener;
import cn.benguo.calendar.R;

/**
 * Created by pyt on 2017/3/16.<h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 日历组件指示器，有翻页按钮<br/>
 */
public class CalendarIndicateView extends FrameLayout implements View.OnClickListener {

	private static final String DATE_FORMAT = "%d年%02d月";
    private ImageView ivLeftArrow,ivRightArrow;
    private TextView tvCalendarIndicate;
    private OnSwitchListener onSwitchListener;

    public CalendarIndicateView(@NonNull Context context) {
        this(context,null);
    }

    public CalendarIndicateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CalendarIndicateView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initEvent();
    }

    private void initView(@NonNull Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.calendar_indicate_widget, this, false);
        addView(view);
        ivLeftArrow = (ImageView) view.findViewById(R.id.iv_left_arrow);
        ivRightArrow = (ImageView) view.findViewById(R.id.iv_right_arrow);
        tvCalendarIndicate = (TextView) view.findViewById(R.id.tv_calendar_indicate);
    }

    private void initEvent(){
        ivLeftArrow.setOnClickListener(this);
        ivRightArrow.setOnClickListener(this);
    }

    public void setOnSwitchListener(OnSwitchListener onSwitchListener){
        this.onSwitchListener = onSwitchListener;
    }

    public void refreshCalendarIndicate(int year,int month){
        tvCalendarIndicate.setText(String.format(DATE_FORMAT, year, month));
    }

    @Override
    public void onClick(View v) {
        if (onSwitchListener != null) {
            if (v == ivLeftArrow) {
				boolean result = onSwitchListener.onLeftClick();
				if(result){
					ivRightArrow.setVisibility(View.VISIBLE);
				}else{
					// 设置"上一页"按钮不可见
					ivLeftArrow.setVisibility(View.GONE);
				}
			} else if (v == ivRightArrow) {
				boolean result = onSwitchListener.onRightClick();
				if(result){
					ivLeftArrow.setVisibility(View.VISIBLE);
				}else{
					// 设置"下一页"按钮不可见
					ivRightArrow.setVisibility(View.GONE);
				}
            }
        }
    }

	/**
	 * 控制“翻页按钮”的显示状态
	 * @param leftBtnVisibility 左侧按钮是否可视
	 * @param rightBtnVisibility 右侧按钮是否可视
	 */
	public void setButtonVisibility(boolean leftBtnVisibility, boolean rightBtnVisibility) {
		ivLeftArrow.setVisibility(leftBtnVisibility?View.VISIBLE:View.GONE);
		ivRightArrow.setVisibility(rightBtnVisibility?View.VISIBLE:View.GONE);
	}
}
