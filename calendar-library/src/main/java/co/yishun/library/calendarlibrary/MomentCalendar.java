package co.yishun.library.calendarlibrary;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by Carlos on 2015/8/28.
 */
public class MomentCalendar extends AnimationViewPager {
    CalendarAdapter mAdapter;


    public MomentCalendar(Context context) {
        super(context);
        init();
    }

    public MomentCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.setTransitionEffect(TransitionEffect.CubeIn);
    }

    public void setAdapter(MomentMonthView.MonthAdapter adapter) {
        mAdapter = new CalendarAdapter(getContext(), this, adapter);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (adapter instanceof CalendarAdapter) {
            super.setAdapter(adapter);
            this.setTransitionEffect(TransitionEffect.CubeIn);
        } else
            Log.e(TAG, "You cannot set adapter yourself!");
    }
}
