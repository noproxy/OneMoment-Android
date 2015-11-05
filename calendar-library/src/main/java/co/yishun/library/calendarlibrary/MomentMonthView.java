package co.yishun.library.calendarlibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MomentMonthView extends AdapterView<InternalMonthAdapter> {
    private final InternalMonthAdapter mAdapter;

    private int mItemLength = 0;

    private int GRAY = getResources().getColor(R.color.colorGray);
    private int ORANGE = getResources().getColor(R.color.colorOrange);

    private Paint mWeekTitlePaint;
    private String[] mWeekTitleArray;
    private float mWeekTitlePadding = getResources().getDimension(R.dimen.MMV_weekTitlePadding);
    private float mWeekTitleSize = getResources().getDimension(R.dimen.MMV_weekTitleSize);
    private Rect mWeekTextMeasureRect;
    private float mWeekTitleHeight;

    private Paint mMonthTitlePaint;
    private String mMonthTitle;
    private float mMonthTitlePadding = getResources().getDimension(R.dimen.MMV_monthTitlePadding);
    private float mMonthTitleSize = getResources().getDimension(R.dimen.MMV_monthTitleSize);
    private Rect mMonthTextMeasureRect;
    private float mMonthTitleHeight;

    private Calendar mCalendar;
    private int mWeekNum;
    // for DayView
    private LayoutParams mItemParams;

    public MomentMonthView(Context context, Calendar calendar, MonthAdapter adapter) {
        super(context);
        mCalendar = calendar;
        mAdapter = new InternalMonthAdapter(calendar, adapter);
        init();
    }

    public MomentMonthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mCalendar = Calendar.getInstance();
        mAdapter = new InternalMonthAdapter(mCalendar, null);
        init();
    }

    public MomentMonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCalendar = Calendar.getInstance();
        mAdapter = new InternalMonthAdapter(mCalendar, null);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MomentMonthView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCalendar = Calendar.getInstance();
        mAdapter = new InternalMonthAdapter(mCalendar, null);
        init();
    }

    public void init() {
        setWillNotDraw(false);

        invalidateCalendar();
        mMonthTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mMonthTitlePaint.setTextSize(mMonthTitleSize);
        mMonthTitlePaint.setColor(ORANGE);
        mMonthTextMeasureRect = new Rect();

        mWeekTitleArray = getResources().getStringArray(R.array.day_of_week);
        mWeekTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mWeekTitlePaint.setTextSize(mWeekTitleSize);
        mWeekTitlePaint.setColor(GRAY);
        mWeekTextMeasureRect = new Rect();

        mItemParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    private void invalidateCalendar() {
        mMonthTitle = new SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(mCalendar.getTime());
        mWeekNum = mCalendar.getActualMaximum(Calendar.WEEK_OF_MONTH);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int rw = MeasureSpec.getSize(widthMeasureSpec);
//        int rh = MeasureSpec.getSize(heightMeasureSpec);
        int w = rw - getPaddingLeft() - getPaddingRight();

        mItemLength = w / 7;
        mMonthTitlePaint.getTextBounds(mMonthTitle, 0, mMonthTitle.length(), mMonthTextMeasureRect);
        mMonthTitleHeight = mMonthTextMeasureRect.height() + mMonthTitlePadding * 2;
        mWeekTitlePaint.getTextBounds(mWeekTitleArray[0], 0, 1, mWeekTextMeasureRect);
        mWeekTitleHeight = mWeekTextMeasureRect.height() + mWeekTitlePadding * 2;

        float h = mItemLength * mWeekNum + mMonthTitleHeight + mWeekTitleHeight + getPaddingTop() + getPaddingBottom();

        mItemParams.width = mItemLength;
        mItemParams.height = mItemLength;

        setMeasuredDimension(w, (int) h);
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
                child.measure(MeasureSpec.EXACTLY | mItemLength, MeasureSpec.EXACTLY);
                position++;
            }
        }

        positionItems();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        float ox = canvas.getWidth() / 2 - mMonthTextMeasureRect.width() / 2;
        float oy = mMonthTextMeasureRect.height() + mMonthTitlePadding;
        canvas.drawText(mMonthTitle, ox, oy, mMonthTitlePaint);

        float y = mMonthTitleHeight + mWeekTextMeasureRect.height() + mWeekTitlePadding;
        for (int i = 0; i < mWeekTitleArray.length; i++) {
            float width = mWeekTitlePaint.measureText(mWeekTitleArray[i]);
            canvas.drawText(mWeekTitleArray[i], i * mItemLength + (mItemLength - width) / 2, y, mWeekTitlePaint);
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

            int mLeft = getPaddingLeft() + (column - 1) * mItemLength;
            int mTop = (int) (getPaddingTop() + mMonthTitleHeight + mWeekTitleHeight + (row - 1) * mItemLength);

            child.layout(mLeft, mTop, mLeft + mItemLength, mTop + mItemLength);
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
