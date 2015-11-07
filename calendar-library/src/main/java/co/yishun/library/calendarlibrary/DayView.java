package co.yishun.library.calendarlibrary;

import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by yyz on 7/19/15.
 */
public class DayView extends View implements View.OnClickListener {

    private static final String TAG = "DayView";
    private static DayView mSelectedDayView = null;
    private Paint mBackgroundPaint;
    private TextPaint mTextPaint;
    private String day;
    private Rect mTextRect;
    private TimeStatus mTimeStatus = TimeStatus.FUTURE;
    private int BLACK = getResources().getColor(R.color.colorBlack);
    private int WHITE = getResources().getColor(R.color.colorWhite);
    private int GRAY = getResources().getColor(R.color.colorGray);
    private int ORANGE = getResources().getColor(R.color.colorOrange);
    private float mTextSize = getResources().getDimension(R.dimen.MMV_dayNumTextSize);

    public DayView(Context context, int day) {
        super(context);
        init(day);
    }

    public DayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(12);
    }

    public DayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(12);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(12);
    }

    public static DayView getSelectedDayView() {
        return mSelectedDayView;
    }

    @Override
    public void setSelected(boolean selected) {
        //TODO add animation
        if (selected) {
            // avoid circular
            if (!isEnabled() || mTimeStatus == TimeStatus.FUTURE) return;
            if (mSelectedDayView != null) {
                mSelectedDayView.setSelected(false);
            }
            mSelectedDayView = this;
        }
        super.setSelected(selected);
    }

    public void setTimeStatus(TimeStatus time) {
        this.mTimeStatus = time;
        invalidate();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(mTimeStatus == TimeStatus.TODAY || enabled);
    }

    private void init(int day) {
        setWillNotDraw(false);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(ORANGE);

        this.day = String.valueOf(day);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextRect = new Rect();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setStateListAnimator(AnimatorInflater.loadStateListAnimator(getContext(), R.anim.btn_elevation));
        }
        super.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int h = getMeasuredHeight();
        int w = getMeasuredWidth();
        Log.i(TAG, "h: " + h + ", w: " + w);

        mTextPaint.getTextBounds(day, 0, day.length(), mTextRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float ox = canvas.getWidth() / 2;
        final float oy = canvas.getHeight() / 2;
        final float r = Math.min(ox, oy);

        if (!isEnabled() || mTimeStatus == TimeStatus.FUTURE) {
            // today should be enable
            mTextPaint.setColor(GRAY);
        } else if (isSelected()) {
            // if can be selected, it of priority to others
            mTextPaint.setColor(WHITE);
            canvas.drawCircle(ox, oy, r, mBackgroundPaint);
        } else if (mTimeStatus == TimeStatus.TODAY)
            mTextPaint.setColor(ORANGE);
        else {
            mTextPaint.setColor(BLACK);
        }


        final float x = ox - mTextRect.width() / 2;
        final float y = oy + mTextRect.height() / 2;

        canvas.drawText(day, x, y, mTextPaint);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new UnsupportedOperationException("You cannot call this");
    }

    @Override
    public void onClick(View v) {
        setSelected(true);
    }

    public enum TimeStatus {
        TODAY, PAST, FUTURE
    }
}
