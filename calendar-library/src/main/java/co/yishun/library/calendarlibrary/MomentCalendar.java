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
    public static final float DAY_VIEW_RATIO = 0.8f;
    // Those Paint, Rect, Size and Padding are just used to measure ViewPager but won't be used in drawing of this view.
    CalendarAdapter mAdapter;

    int mDayViewWidth = 0;
    int mDayViewHeight = 0;

    Paint mWeekTitlePaint;
    String[] mWeekTitleArray;
    float mWeekTitlePadding = getResources().getDimension(R.dimen.MMV_weekTitlePadding);
    float mWeekTitleSize = getResources().getDimension(R.dimen.MMV_weekTitleSize);
    Rect mWeekTitleMeasureRect;
    float mWeekTitleHeight;

    Paint mMonthTitlePaint;
    String mExampleMonthTitle;
    float mMonthTitlePadding = getResources().getDimension(R.dimen.MMV_monthTitlePadding);
    float mMonthTitleSize = getResources().getDimension(R.dimen.MMV_monthTitleSize);
    Rect mMonthTitleMeasureRect;
    float mMonthTitleHeight;

    int GRAY = getResources().getColor(R.color.colorGray);
    int ORANGE = getResources().getColor(R.color.colorOrange);

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

        mMonthTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mMonthTitlePaint.setTextSize(mMonthTitleSize);
        mMonthTitlePaint.setColor(ORANGE);
        mMonthTitleMeasureRect = new Rect();

        mWeekTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mWeekTitlePaint.setTextSize(mWeekTitleSize);
        mWeekTitlePaint.setColor(GRAY);
        mWeekTitleMeasureRect = new Rect();

        mWeekTitleArray = getResources().getStringArray(R.array.day_of_week);
        mExampleMonthTitle = new SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(new Date());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int rw = MeasureSpec.getSize(widthMeasureSpec);
        int w = rw - getPaddingRight() - getPaddingLeft();

        mDayViewWidth = w / 7;
        mDayViewHeight = (int) (mDayViewWidth * DAY_VIEW_RATIO);

        mMonthTitlePaint.getTextBounds(mExampleMonthTitle, 0, mExampleMonthTitle.length(), mMonthTitleMeasureRect);
        mMonthTitleHeight = mMonthTitleMeasureRect.height() + mMonthTitlePadding * 2;
        mWeekTitlePaint.getTextBounds(mWeekTitleArray[0], 0, 1, mWeekTitleMeasureRect);
        mWeekTitleHeight = mWeekTitleMeasureRect.height() + mWeekTitlePadding * 2;

        int h = (int) (mDayViewHeight * MAX_WEEK_NUM + mMonthTitleHeight + mWeekTitleHeight + getPaddingTop() + getPaddingBottom());        //TODO this not include the padding of the child MonthView

        super.onMeasure(MeasureSpec.makeMeasureSpec(rw, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
//        setMeasuredDimension(w, h);
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
