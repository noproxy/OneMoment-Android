package co.yishun.library.momentcalendar;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.AdapterView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MomentMonthView extends AdapterView<InternalMonthAdapter> {
    private final InternalMonthAdapter mAdapter;

    // we measure most size in parent, we need just read it instead repeat measuring
    private MomentCalendar mParent;
    private String mMonthTitle;
    private Calendar mCalendar;
    private int mWeekNum;
    // for DayView
    private LayoutParams mItemParams;

    public MomentMonthView(Context context, Calendar calendar, MonthAdapter adapter, MomentCalendar parent) {
        super(context);
        mCalendar = calendar;
        mAdapter = new InternalMonthAdapter(calendar, adapter);
        mParent = parent;
        setWillNotDraw(false);
        invalidateCalendar();

        mItemParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * @return the copy of calendar of this month, only year and month is vaild.
     */
    public Calendar getMonthCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mCalendar.getTime());
        return calendar;
    }

    private void invalidateCalendar() {
        mMonthTitle = new SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(mCalendar.getTime());
        mWeekNum = mCalendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //        int rw = MeasureSpec.getSize(widthMeasureSpec);
        ////        int rh = MeasureSpec.getSize(heightMeasureSpec);
        //        int w = rw - getPaddingLeft() - getPaddingRight();
        //
        //        mItemLength = w / 7;
        //        mMonthTitlePaint.getTextBounds(mMonthTitle, 0, mMonthTitle.length(), mMonthTextMeasureRect);
        //        mMonthTitleHeight = mMonthTextMeasureRect.height() + mMonthTitlePadding * 2;
        //        mWeekTitlePaint.getTextBounds(mWeekTitleArray[0], 0, 1, mWeekTextMeasureRect);
        //        mWeekTitleHeight = mWeekTextMeasureRect.height() + mWeekTitlePadding * 2;
        //
        //        float h = mItemLength * mWeekNum + mMonthTitleHeight + mWeekTitleHeight + getPaddingTop() + getPaddingBottom();
        //
        //        mItemParams.width = mItemLength;
        //        mItemParams.height = mItemLength;
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if (mAdapter == null) {
            return;
        }

        if (getChildCount() == 0) {
            int position = 0;
            while (position < mAdapter.getCount()) {
                View child = mAdapter.getView(position, null, this);
                addViewInLayout(child, -1, mItemParams, true);
                child.measure(MeasureSpec.makeMeasureSpec(mParent.mDayViewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mParent.mDayViewHeight, MeasureSpec.EXACTLY));
                position++;
            }
        }

        positionItems();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        float ox = canvas.getWidth() / 2 - mParent.mMonthTitleMeasureRect.width() / 2;
        float oy = mParent.mMonthTitleMeasureRect.height() + mParent.mMonthTitlePadding;
        canvas.drawText(mMonthTitle, ox, oy, mParent.mMonthTitlePaint);

        float y = mParent.mMonthTitleHeight + mParent.mWeekTitleMeasureRect.height() + mParent.mWeekTitlePadding;
        for (int i = 0; i < mParent.mWeekTitleArray.length; i++) {
            float width = mParent.mWeekTitlePaint.measureText(mParent.mWeekTitleArray[i]);
            canvas.drawText(mParent.mWeekTitleArray[i], i * mParent.mDayViewWidth + (mParent.mDayViewWidth - width) / 2, y, mParent.mWeekTitlePaint);
        }
    }

    /**
     * Positions the children at the "correct" positions
     */
    private void positionItems() {
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            mCalendar.set(Calendar.DAY_OF_MONTH, index + 1);
            int column = mCalendar.get(Calendar.DAY_OF_WEEK);// start 1 == Sunday
            int row = mCalendar.get(Calendar.WEEK_OF_MONTH);// start 1

            int mLeft = getPaddingLeft() + (column - 1) * mParent.mDayViewWidth;
            int mTop = (int) (getPaddingTop() + mParent.mMonthTitleHeight + mParent.mWeekTitleHeight + (row - 1) * mParent.mDayViewHeight);

            child.layout(mLeft, mTop, mLeft + mParent.mDayViewWidth, (int) (mTop + mParent.mDayViewHeight));
        }
    }

    @Override
    public InternalMonthAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(InternalMonthAdapter adapter) {
        throw new UnsupportedOperationException("You cannot call this.");
    }

    @Override
    public View getSelectedView() {
        return null;
    }

    @Override
    public void setSelection(int position) {

    }

    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
        invalidateCalendar();
        removeAllViewsInLayout();
        mAdapter.updateCalendar(calendar);
        this.invalidate();
    }

    public interface MonthAdapter {
        /**
         * set day view
         *
         * @param calendar indicate day of the view
         * @param dayView  view of displayed
         */
        void onBindView(Calendar calendar, DayView dayView);
    }
}
