package co.yishun.library.calendarlibrary;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by yyz on 7/19/15.
 */
public class DayView extends ImageView implements View.OnClickListener {

    private static final String TAG = "DayView";
    private static DayView mSelectedDayView = null;
    private static OnMomentSelectedListener mMomentSelectedListener;
    private static boolean mMultiSelection = false;
    private Paint mBackgroundPaint;
    private TextPaint mTextPaint;
    private String day;
    private Rect mTextRect;
    private TimeStatus mTimeStatus = TimeStatus.FUTURE;
    private int BLACK = getResources().getColor(R.color.colorBlack);
    private int WHITE = getResources().getColor(R.color.colorWhite);
    private int GRAY = getResources().getColor(R.color.colorGray);
    private int ORANGE = getResources().getColor(R.color.colorOrange);
    private int ORANGE_TRANSPARENT = getResources().getColor(R.color.colorOrangeTransparent);
    private float mTextSize = getResources().getDimension(R.dimen.MMV_dayNumTextSize);
    private BitmapShader mBitmapShader;
    private Paint mBitmapPaint;

    public DayView(Context context, int day) {
        super(context);
        init(day);
    }

    public static void setMultiSelection(boolean multiSelection) {
        mMultiSelection = multiSelection;
    }

    private static void setSelectedDayView(DayView dayView) {
    }

    public static void setOnMomentSelectedListener(@Nullable OnMomentSelectedListener listener) {
        mMomentSelectedListener = listener;
    }

    @Override
    public void setSelected(boolean selected) {
        //TODO add animation
        if (selected) {
            // avoid circular
            if (!isEnabled() || mTimeStatus == TimeStatus.FUTURE) return;
            if (mSelectedDayView != null && !mMultiSelection) {
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
        mBackgroundPaint.setColor(ORANGE_TRANSPARENT);

        this.day = String.valueOf(day);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextRect = new Rect();

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setStateListAnimator(AnimatorInflater.loadStateListAnimator(getContext(), R.anim.btn_elevation));
        }
        super.setOnClickListener(this);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTextPaint.getTextBounds(day, 0, day.length(), mTextRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //        super.onDraw(canvas);
        final float ox = canvas.getWidth() / 2;
        final float oy = canvas.getHeight() / 2;
        final float r = Math.min(ox, oy);

        updatePaint(getBitmapFromDrawable(getDrawable()));
        canvas.drawCircle(ox, oy, r, mBitmapPaint);

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

    //TODO  test performance
    //TODO change selected effect
    @Override public void setImageBitmap(@NonNull Bitmap bitmap) {
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

    @Override public void setOnClickListener(OnClickListener l) {
        throw new UnsupportedOperationException("You cannot call this");
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
}
