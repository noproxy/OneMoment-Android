package co.yishun.library.calendarlibrary;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

/**
 * Created by Carlos on 2015/8/29.
 */
public class CalendarAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private static final String TAG = "CalendarAdapter";
    int cacheSize = 5;
    private int middleInt = Integer.MAX_VALUE / 2;
    private MomentMonthView monthViews[] = new MomentMonthView[cacheSize];
    private int centerPageHolderIndex = cacheSize / 2;
    private int centerPagePosition = middleInt;
    private MomentCalendar momentCalendar;
    private Context context;

    public CalendarAdapter(Context context, MomentCalendar momentCalendar, MomentMonthView.MonthAdapter adapter) {
        this.context = context;
        this.momentCalendar = momentCalendar;

        for (int i = 0; i < cacheSize; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, i - cacheSize / 2);
            MomentMonthView view = new MomentMonthView(context, calendar, adapter, momentCalendar);
            monthViews[i] = view;
        }
        momentCalendar.setAdapter(this);
        momentCalendar.setOnPageChangeListener(this);
        momentCalendar.setCurrentItem(middleInt);
    }

    private int getRelativePosition(int position) {
        return position - middleInt;
    }

    private Calendar getCalendarAt(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, getRelativePosition(position));
        return calendar;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object instantiateItem(ViewGroup parent, final int position) {
        MomentMonthView currView = monthViews[(((position - centerPagePosition + centerPageHolderIndex) % cacheSize) + cacheSize) % cacheSize];
        parent.removeView(currView);
        parent.addView(currView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        currView.setCalendar(getCalendarAt(position));
//        momentCalendar.setObjectForPosition(, position);
        return currView;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { /* ignored*/ }

    @Override
    public void onPageSelected(int position) { /* ignored*/ }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            int old = centerPagePosition;
            centerPagePosition = momentCalendar.getCurrentItem();
            centerPageHolderIndex = (centerPagePosition - old + centerPageHolderIndex) % cacheSize;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, centerPagePosition - middleInt);
            // page change
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object obj) {
        // ignored
    }
}
