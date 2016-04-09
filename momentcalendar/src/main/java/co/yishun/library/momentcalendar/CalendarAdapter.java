package co.yishun.library.momentcalendar;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

/**
 * Created by Carlos on 2015/8/29.
 */
public class CalendarAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
    private static final String TAG = "CalendarAdapter";
    int cacheSize = 5;
    // try to solve page scroll problem. change size to 65535, problem solved, not clear the reason.
    int mSize = 0xffff;
    private int middleInt = mSize / 2;
    private MomentMonthView monthViews[] = new MomentMonthView[cacheSize];
    private int centerPageHolderIndex = cacheSize / 2;
    private int centerPagePosition = middleInt;
    private MomentCalendar momentCalendar;
    private Context context;
    private MomentMonthView currentMonthView;


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
        momentCalendar.addOnPageChangeListener(this);
        momentCalendar.setCurrentItem(middleInt);
    }

    private int getRelativePosition(int position) {
        return position - middleInt;
    }

    protected Calendar getCalendarAt(int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, getRelativePosition(position));
        return calendar;
    }

    @Override
    public int getCount() {
        return mSize;
    }

    @Override
    public Object instantiateItem(ViewGroup parent, final int position) {
        int index = (((position - centerPagePosition + centerPageHolderIndex) % cacheSize) + cacheSize) % cacheSize;
        MomentMonthView currView = monthViews[index];

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



    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        currentMonthView = (MomentMonthView) object;
        super.setPrimaryItem(container, position, object);
    }


    public MomentMonthView getCurrentMonthView(){
        return currentMonthView;
    }

}
