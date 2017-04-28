package cn.benguo.calendar;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lzh on 2017/4/13 19:10.
 */
@Deprecated
public class BaseCalendarView extends View {
	public BaseCalendarView(Context context) {
		super(context);
	}

	public BaseCalendarView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public BaseCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

}
