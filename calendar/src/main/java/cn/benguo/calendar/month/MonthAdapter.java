package cn.benguo.calendar.month;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import org.joda.time.DateTime;


/**
 * Created by pyt on 2017/3/16.
 * <h1>Modified by lzh on 2017/4/10.</h1>
 * <h2>描述：</h2>
 * 月份日历控件的页面适配器<br/>
 */
public class MonthAdapter extends PagerAdapter {

    private SparseArray<MonthView> mViews;
    private Context mContext;
    private OnMonthClickListener mOnMonthClickListener;
    private int mMonths;
    /**
     * 今天的日期
     */
    private DateTime mToday;

    /**
     * "今天"所对应的日期在第几页
     */
    private int mTodayPagePosition;


    /**
     *
     * @param context
     * @param listener
     * @param preMonths 日历可显示今日之前的几个月
     * @param nextMonths 日历可显示今日之后的几个月
     */
    public MonthAdapter(Context context, OnMonthClickListener listener,
                        int preMonths, int nextMonths) {
        mContext = context;
        mOnMonthClickListener = listener;
        mToday = new DateTime();
        mMonths = preMonths + 1 + nextMonths;
        mTodayPagePosition = preMonths;
        mViews = new SparseArray<MonthView>();
    }

    @Override
    public int getCount() {
        return mMonths;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mViews.get(position) == null) {
            instanceMonthView(position);
        }
        container.addView(mViews.get(position));
        return mViews.get(position);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public SparseArray<MonthView> getViews() {
        return mViews;
    }

    public MonthView instanceMonthView(int position) {
        DateTime dateTime = mToday.plusMonths(position - mTodayPagePosition);
        MonthView monthView = new MonthView(mContext);
        monthView.setDateForDraw(dateTime);
        monthView.setId(position);
        monthView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        monthView.invalidate();
        monthView.setOnDateClickListener(mOnMonthClickListener);
        mViews.put(position, monthView);
        return monthView;
    }

    /**
     * "今天"所对应的日期在第几页
     * @return
     */
    public int getTodayPagePosition(){
        return mTodayPagePosition;
    }
}
