package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/22.
 */
public class VideoTypeView extends RelativeLayout {
    private TextView mWorldTextView;
    private TextView mLifeTextView;
    private TextView mDiaryTextView;

    private TextBgDrawable mWorldDrawable;
    private TextBgDrawable mLifeDrawable;
    private TextBgDrawable mDiaryDrawable;

    private int colorOrange;
    private int colorGray;
    private int oneDp;

    public VideoTypeView(Context context) {
        super(context);
        init();
    }

    public VideoTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoTypeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_video_type, this);
        oneDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        mWorldTextView = (TextView) findViewById(R.id.worldTextView);
        mLifeTextView = (TextView) findViewById(R.id.lifeTextView);
        mDiaryTextView = (TextView) findViewById(R.id.diaryTextView);

        colorOrange = getResources().getColor(R.color.colorAccent);
        colorGray = getResources().getColor(R.color.colorGray);

        mWorldDrawable = new TextBgDrawable(colorGray);
        mLifeDrawable = new TextBgDrawable(colorGray);
        mDiaryDrawable = new TextBgDrawable(colorGray);

        mWorldTextView.setBackground(mWorldDrawable);
        mLifeTextView.setBackground(mLifeDrawable);
        mDiaryTextView.setBackground(mDiaryDrawable);

        setWorldCheck(false, null);
        setLifeCheck(false);
        setDiaryCheck(false);
    }

    public void setWorldCheck(boolean check, String worldName) {
        if (check) {
            mWorldTextView.setText(worldName);
            mWorldTextView.setTextColor(colorOrange);
            mWorldDrawable.setColor(colorOrange);
        } else {
            mWorldTextView.setText(getResources().getString(R.string.video_type_add_to_world));
            mWorldTextView.setTextColor(colorGray);
            mWorldDrawable.setColor(colorGray);
        }
    }

    public void setLifeCheck(boolean check) {
        if (check) {
            mLifeTextView.setTextColor(colorOrange);
            mLifeDrawable.setColor(colorOrange);
        } else {
            mLifeTextView.setTextColor(colorGray);
            mLifeDrawable.setColor(colorGray);
        }
    }

    public void setDiaryCheck(boolean check) {
        if (check) {
            mDiaryTextView.setTextColor(colorOrange);
            mDiaryDrawable.setColor(colorOrange);
        } else {
            mDiaryTextView.setTextColor(colorGray);
            mDiaryDrawable.setColor(colorGray);
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogUtil.d("size R", getWidth() + "  " + getHeight());

        int textHeight = 0;
        int margin = 0;

        if (getHeight() >= 92 * oneDp) {
            textHeight = 34 * oneDp;
            margin = 10 * oneDp;
        } else {
            textHeight = 28 * oneDp;
            margin = (getHeight() - textHeight) / 3;
        }

        mWorldTextView.setHeight(textHeight);
        mLifeTextView.setHeight(textHeight);
        mDiaryTextView.setHeight(textHeight);
        ((LayoutParams) mWorldTextView.getLayoutParams()).bottomMargin = margin;

        mWorldDrawable.setSize(mWorldTextView.getWidth(), mWorldTextView.getHeight());
        mLifeDrawable.setSize(mLifeTextView.getWidth(), mLifeTextView.getHeight());
        mDiaryDrawable.setSize(mDiaryTextView.getWidth(), mDiaryTextView.getHeight());
    }

    private class TextBgDrawable extends Drawable {
        private Paint mPaint;
        private RectF mRect;
        private float mRadius;

        public TextBgDrawable(int color) {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);

            mPaint.setAntiAlias(true);
            mPaint.setColor(color);
            mPaint.setStrokeWidth(oneDp);
            mRect = new RectF(0, 0, getWidth(), getHeight());
            mRect.inset(oneDp / 2, oneDp / 2);
            LogUtil.d("size ", getWidth() + "  " + getHeight());
        }

        public void setColor(int color) {
            mPaint.setColor(color);
        }

        public void setSize(int width, int height) {
            mRect.set(0, 0, width, height);
            mRect.inset(oneDp / 2, oneDp / 2);
            mRadius = height / 2;
        }

        @Override public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, mRadius, mRadius, mPaint);
        }

        @Override public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override public void setColorFilter(ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

}
