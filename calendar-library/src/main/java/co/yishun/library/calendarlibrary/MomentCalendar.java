package co.yishun.library.calendarlibrary;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Carlos on 2015/8/28.
 */
public class MomentCalendar extends AnimationViewPager {
    public static final int MAX_WEEK_NUM = 6;
    CalendarAdapter mAdapter;
    // Those Paint, Rect, Size and Padding are just used to measure ViewPager but won't be used in drawing of this view.
    private Rect mWeekTextMeasureRect;
    private Rect mMonthTextMeasureRect;
    private float mWeekTitlePadding = getResources().getDimension(R.dimen.MMV_weekTitlePadding);
    private float mWeekTitleSize = getResources().getDimension(R.dimen.MMV_weekTitleSize);
    private Paint mMonthTitlePaint;
    private Paint mWeekTitlePaint;
    private float mMonthTitleSize = getResources().getDimension(R.dimen.MMV_monthTitleSize);
    private float mMonthTitlePadding = getResources().getDimension(R.dimen.MMV_monthTitlePadding);
    private String mMonthTitle;
    private String[] mWeekTitleArray;

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

        mWeekTitleArray = getResources().getStringArray(R.array.day_of_week);
        mMonthTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mMonthTitlePaint.setTextSize(mMonthTitleSize);
        mWeekTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mWeekTitlePaint.setTextSize(mWeekTitleSize);
        mMonthTextMeasureRect = new Rect();
        mWeekTextMeasureRect = new Rect();

        mMonthTitle = new SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(new Date());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int rw = MeasureSpec.getSize(widthMeasureSpec);
        int w = rw;

        int itemLength = w / 7;

        mMonthTitlePaint.getTextBounds(mMonthTitle, 0, mMonthTitle.length(), mMonthTextMeasureRect);
        float monthTitleHeight = mMonthTextMeasureRect.height() + mMonthTitlePadding * 2;
        mWeekTitlePaint.getTextBounds(mWeekTitleArray[0], 0, 1, mWeekTextMeasureRect);
        float weekTitleHeight = mWeekTextMeasureRect.height() + mWeekTitlePadding * 2;

        int h = (int) (itemLength * MAX_WEEK_NUM + monthTitleHeight + weekTitleHeight);        //TODO this not include the padding of the child MonthView

        super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
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
