package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/22.
 */
public class VideoTypeView extends RelativeLayout {
    private TextView mWorldTextView;
    private TextView mTodayTextView;
    private TextView mDiaryTextView;
    private View mWorldClearView;

    private TextBgDrawable mWorldDrawable;
    private TextBgDrawable mTodayDrawable;
    private TextBgDrawable mDiaryDrawable;
    private CloseDrawable mWorldClearDrawable;

    private int colorOrange;
    private int colorGray;
    private int colorGrayDark;
    private int colorWhite;
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
        mTodayTextView = (TextView) findViewById(R.id.todayTextView);
        mDiaryTextView = (TextView) findViewById(R.id.diaryTextView);
        mWorldClearView = findViewById(R.id.worldClearView);
        mWorldClearView.setVisibility(INVISIBLE);

        colorOrange = getResources().getColor(R.color.colorAccent);
        colorGray = getResources().getColor(R.color.colorVideoTypeGray);
        colorGrayDark = getResources().getColor(R.color.colorVideoTypeGrayDark);
        colorWhite = getResources().getColor(R.color.colorWhite);

        mWorldDrawable = new TextBgDrawable(colorGrayDark, colorGray, true);
        mTodayDrawable = new TextBgDrawable(colorGrayDark, colorGray);
        mDiaryDrawable = new TextBgDrawable(colorGrayDark, colorGray);
        mWorldClearDrawable = new CloseDrawable(colorWhite);

        mWorldTextView.setBackground(mWorldDrawable);
        mTodayTextView.setBackground(mTodayDrawable);
        mDiaryTextView.setBackground(mDiaryDrawable);
        mWorldClearView.setBackground(mWorldClearDrawable);

        setWorldCheck(false, null);
        setTodayCheck(false);
        setDiaryCheck(false);
    }

    public void setWorldCheck(boolean check, String worldName) {
        if (check) {
            mWorldTextView.setText(worldName);
            mWorldTextView.setTextColor(colorOrange);
            mWorldDrawable.setColor(colorOrange, colorOrange);
            mWorldClearView.setVisibility(VISIBLE);
        } else {
            mWorldTextView.setText(getResources().getString(R.string.video_type_add_to_world));
            mWorldTextView.setTextColor(colorGrayDark);
            mWorldDrawable.setColor(colorGrayDark, colorGray);
            mWorldClearView.setVisibility(INVISIBLE);
        }
    }

    public void setTodayCheck(boolean check) {
        if (check) {
            mTodayTextView.setTextColor(colorOrange);
            mTodayDrawable.setColor(colorOrange, colorOrange);
        } else {
            mTodayTextView.setTextColor(colorGrayDark);
            mTodayDrawable.setColor(colorGrayDark, colorGray);
        }
    }

    public void setDiaryCheck(boolean check) {
        if (check) {
            mDiaryTextView.setTextColor(colorOrange);
            mDiaryDrawable.setColor(colorOrange, colorOrange);
        } else {
            mDiaryTextView.setTextColor(colorGrayDark);
            mDiaryDrawable.setColor(colorGrayDark, colorGray);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogUtil.d("size R", getWidth() + "  " + getHeight());

        int textHeight;
        int margin;

        if (getHeight() >= 92 * oneDp) {
            textHeight = 34 * oneDp;
            margin = 10 * oneDp;
        } else {
            textHeight = 28 * oneDp;
            margin = (getHeight() - 2 * textHeight) / 3;
        }

        mWorldTextView.setHeight(textHeight);
        mTodayTextView.setHeight(textHeight);
        mDiaryTextView.setHeight(textHeight);
        ((LayoutParams) mWorldTextView.getLayoutParams()).bottomMargin = margin;

        ((LayoutParams) mWorldClearView.getLayoutParams()).width = textHeight;

        mWorldDrawable.setSize(mWorldTextView.getWidth(), mWorldTextView.getHeight());
        mTodayDrawable.setSize(mTodayTextView.getWidth(), mTodayTextView.getHeight());
        mDiaryDrawable.setSize(mDiaryTextView.getWidth(), mDiaryTextView.getHeight());
        mWorldClearDrawable.setSize(mWorldClearView.getWidth(), mWorldClearView.getHeight());
    }

    private class TextBgDrawable extends Drawable {
        private Paint mStrokePaint;
        private Paint mSolidPaint;
        private RectF mRect;
        private float mRadius;
        private float mStrokeWidth;
        private int mHeight;
        private boolean mWithArrow;
        private Path mPath;

        public TextBgDrawable(int strokeColor, int solidColor) {
            this(strokeColor, solidColor, false);
        }

        public TextBgDrawable(int strokeColor, int solidColor, boolean withArrow) {
            mStrokeWidth = oneDp * 1.3f;
            mWithArrow = withArrow;
            if (withArrow) mPath = new Path();

            mStrokePaint = new Paint();
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setAntiAlias(true);
            mStrokePaint.setColor(strokeColor);
            mStrokePaint.setStrokeWidth(mStrokeWidth);

            mSolidPaint = new Paint();
            mSolidPaint.setStyle(Paint.Style.FILL);
            mSolidPaint.setAntiAlias(true);
            mSolidPaint.setColor(solidColor);

            mRect = new RectF(0, 0, getWidth(), getHeight());
            mRect.inset(mStrokeWidth / 2, mStrokeWidth / 2);
            LogUtil.d("size ", getWidth() + "  " + getHeight());
        }

        public void setColor(int strokeColor, int solidColor) {
            mStrokePaint.setColor(strokeColor);
            mSolidPaint.setColor(solidColor);
        }

        public void setSize(int width, int height) {
            mHeight = height;
            mRect.set(0, 0, width, height);
            mRect.inset(mStrokeWidth / 2, mStrokeWidth / 2);
            mRadius = height / 2;

            if (mWithArrow) {
                float dSize = 0.1667f;
                mPath.reset();
                mPath.moveTo(width - mRadius - dSize * height, (0.5f - dSize) * height);
                mPath.lineTo(width - mRadius, height * 0.5f);
                mPath.lineTo(width - mRadius - dSize * height, (0.5f + dSize) * height);
            }
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, mRadius, mRadius, mStrokePaint);
            canvas.drawCircle(mHeight / 2, mHeight / 2, mRadius / 2, mSolidPaint);
            canvas.drawCircle(mHeight / 2, mHeight / 2, mRadius / 2, mStrokePaint);
            if (mWithArrow)
                canvas.drawPath(mPath, mStrokePaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mStrokePaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mStrokePaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    class CloseDrawable extends Drawable {
        private Paint mPaint;
        private float mWidth;
        private float mHeight;
        private float mStrokeWidth;

        CloseDrawable(int color) {
            mStrokeWidth = oneDp * 1.3f;
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(color);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(mStrokeWidth);
        }

        void setSize(float width, float height) {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public void draw(Canvas canvas) {
            float dSize = 0.5f - 1.414f / 8;
            canvas.drawLine(dSize * mWidth, dSize * mHeight,
                    (1 - dSize) * mWidth, (1 - dSize) * mHeight, mPaint);
            canvas.drawLine((1 - dSize) * mWidth, dSize * mHeight,
                    dSize * mWidth, (1 - dSize) * mHeight, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

}
