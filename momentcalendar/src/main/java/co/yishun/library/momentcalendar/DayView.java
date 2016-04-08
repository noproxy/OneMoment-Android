package co.yishun.library.momentcalendar;

import android.animation.AnimatorInflater;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * Created by yyz on 7/19/15.
 */
public class DayView extends ImageView implements View.OnClickListener {

    private static final String TAG = "DayView";
    private static DayView mSelectedDayView = null;
    private static OnMomentSelectedListener mMomentSelectedListener;
    private static boolean mMultiSelection = false;
    private static WeakReference<OnTodayAvailableListener> mTodayAvailableListener = new WeakReference<>(null);
    private Paint mBackgroundPaint;
    private TextPaint mTextPaint;
    private String day;
    private Rect mTextRect;
    private float mTextWidth;
    private TimeStatus mTimeStatus = TimeStatus.FUTURE;
    private int BLACK = getResources().getColor(R.color.colorBlack);
    private int WHITE = getResources().getColor(R.color.colorWhite);
    private int GRAY = getResources().getColor(R.color.colorGray);
    private int ORANGE = getResources().getColor(R.color.colorOrange);
    private int ORANGE_TRANSPARENT = getResources().getColor(R.color.colorOrangeTransparent);
    private float mTextSize = getResources().getDimension(R.dimen.MMV_dayNumTextSize);
    private BitmapShader mBitmapShader;
    private Paint mBitmapPaint;
    @ColorInt
    private int mOverrideTextColor = 0;

    public DayView(Context context, int day) {
        super(context);
        init(day);
    }

    public static void setMultiSelection(boolean multiSelection) {
        mMultiSelection = multiSelection;
    }

    public static void setOnMomentSelectedListener(@Nullable OnMomentSelectedListener listener) {
        mMomentSelectedListener = listener;
    }

    public static void setTodayAvailableListener(OnTodayAvailableListener todayAvailableListener) {
        mTodayAvailableListener = new WeakReference<>(todayAvailableListener);
    }

    protected static void onTodayAvailable(DayView dayView) {
        OnTodayAvailableListener listener = mTodayAvailableListener.get();
        if (listener != null) {
            listener.onTodayAvailable(dayView);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        //TODO add animation
        if (selected) {
            // avoid circular
            if (!isEnabled() || mTimeStatus == TimeStatus.FUTURE)
                return;
            if (mSelectedDayView != null && !mMultiSelection) {
                mSelectedDayView.setSelected(false);
            }
            mSelectedDayView = this;
        }
        super.setSelected(selected);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!mMultiSelection)
            super.setEnabled(mTimeStatus == TimeStatus.TODAY || enabled);
        else
            super.setEnabled(enabled);
    }

    void onBind(Calendar calendar) {
//        removeOverrideTextColor();
    }

    public String getDay() {
        return day;
    }

    private void init(int day) {

        setWillNotDraw(false);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(ORANGE_TRANSPARENT);

        this.day = String.valueOf(day);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextRect = new Rect();

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setStateListAnimator(AnimatorInflater.loadStateListAnimator(getContext(), R.drawable.btn_elevation));
        }
        super.setOnClickListener(this);
    }

    public void overrideTextColorResource(@ColorRes int colorRes) {
        overrideTextColor(getResources().getColor(colorRes));
    }

    public void overrideTextColor(@ColorInt int color) {
        mOverrideTextColor = color;
        invalidate();
    }

    public void removeOverrideTextColor() {
        overrideTextColor(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTextPaint.getTextBounds(day, 0, day.length(), mTextRect);
        mTextWidth = mTextPaint.measureText(day, 0, day.length());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //        super.onDraw(canvas);
        final float ox = canvas.getWidth() / 2;
        final float oy = canvas.getHeight() / 2;
        final float r = Math.min(ox, oy) * 0.85f;

        if (getDrawable() != null) {
            updatePaint(getBitmapFromDrawable(getDrawable()));
            canvas.drawCircle(ox, oy, r, mBitmapPaint);
        }

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


        final float x = ox - mTextWidth / 2.0f;
        final float y = oy + mTextRect.height() / 2.0f;

        if (mOverrideTextColor != 0) {
            mTextPaint.setColor(mOverrideTextColor);
        }

        canvas.drawText(day, x, y, mTextPaint);
    }

    //TODO  test performance
    //TODO change selected effect
    @Override
    public void setImageBitmap(@NonNull Bitmap bitmap) {
        super.setImageBitmap(bitmap);
    }

    private void updatePaint(Bitmap mBitmap) {
        if (mBitmap == null) {
            mBitmapPaint.setColor(Color.TRANSPARENT);
            return;
        }
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapPaint.setShader(mBitmapShader);
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new UnsupportedOperationException("You cannot call this");
    }

    public TimeStatus getTimeStatus() {
        return mTimeStatus;
    }

    public void setTimeStatus(TimeStatus time) {
        this.mTimeStatus = time;
        invalidate();
    }

    @Override
    public void onClick(View v) {
        if (mMomentSelectedListener != null) {
            mMomentSelectedListener.onSelected(this);
        }
        if (mMultiSelection)
            setSelected(!isSelected());
        else
            setSelected(true);
    }

    public enum TimeStatus {
        TODAY, PAST, FUTURE
    }

    public interface OnMomentSelectedListener {
        void onSelected(DayView dayView);
    }

    public interface OnTodayAvailableListener {
        void onTodayAvailable(DayView dayView);
    }
}
