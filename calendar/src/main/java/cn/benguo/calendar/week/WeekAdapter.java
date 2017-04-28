package cn.benguo.calendar.week;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;

/**
 *
 * Created by pyt on 2017/3/16.<br/>
 * <h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 单行日历适配器<br/>
 */
public class WeekAdapter extends PagerAdapter {

    private SparseArray<WeekView> mViews;
    private Context mContext;
    private OnWeekClickListener mOnWeekClickListener;
    /**
     * 一共有多少周
     */
    private int mWeekCount;
    /**
     * "今天"所对应的日期在第几页
     */
    private int mTodayPagePosition;
    /**
     * 今天
     */
    private final DateTime mToday;

    /**
     *
     * @param context
     * @param listener
     * @param preWeeks 日历可显示今日之前的多少周
     * @param nextWeeks 日历可显示今日之后的多少周
     */
    public WeekAdapter(Context context, OnWeekClickListener listener,
                       int preWeeks, int nextWeeks) {
        mContext = context;
        mOnWeekClickListener = listener;
        mWeekCount = preWeeks + 1 + nextWeeks;
        mTodayPagePosition = preWeeks;
        //今天
        mToday = new DateTime();
        mViews = new SparseArray<>();
    }

    @Override
    public int getCount() {
        return mWeekCount;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mViews.get(position) == null) {
            instanceWeekView(position);
        }
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public SparseArray<WeekView> getViews() {
        return mViews;
    }

    public WeekView instanceWeekView(int position) {
        WeekView weekView = new WeekView(mContext);
        weekView.setDateForDraw(mToday.plusWeeks(position - mTodayPagePosition));
        weekView.setId(position);
        weekView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        weekView.setOnWeekClickListener(mOnWeekClickListener);
        weekView.invalidate();
        mViews.put(position, weekView);
        return weekView;
    }

    /**
     * "今天"所对应的日期在第几页
     * @return
     */
    public int getTodayPagePosition(){
        return mTodayPagePosition;
    }
}
