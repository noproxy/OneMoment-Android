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
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/22.
 */
public class VideoTypeView extends RelativeLayout {
    private TextView mWorldTextView;
    private TextView mDayTextView;
    private TextView mDiaryTextView;

    private TextBgDrawable mWorldDrawable;
    private TextBgDrawable mDayDrawable;
    private TextBgDrawable mDiaryDrawable;

    private int colorOrange;
    private int colorGray;

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

        mWorldTextView = (TextView) findViewById(R.id.worldTextView);
        mDayTextView = (TextView) findViewById(R.id.dayTextView);
        mDiaryTextView = (TextView) findViewById(R.id.diaryTextView);

        colorOrange = getResources().getColor(R.color.colorAccent);
        colorGray = getResources().getColor(R.color.colorGray);

        mWorldDrawable = new TextBgDrawable(colorOrange);
        mDayDrawable = new TextBgDrawable(colorOrange);
        mDiaryDrawable = new TextBgDrawable(colorOrange);

        mWorldTextView.setBackground(mWorldDrawable);
        mDayTextView.setBackground(mDayDrawable);
        mDiaryTextView.setBackground(mDiaryDrawable);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogUtil.d("size R", getWidth() + "  " + getHeight());
        mWorldTextView.setHeight(getHeight() / 2);
        mDayTextView.setHeight(getHeight() / 2);
        mDiaryTextView.setHeight(getHeight() / 2);
        mWorldDrawable.setSize(mWorldTextView.getWidth(), mWorldTextView.getHeight());
        mDayDrawable.setSize(mDayTextView.getWidth(), mDayTextView.getHeight());
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
            mPaint.setStrokeWidth(getResources().getDimension(R.dimen.md_divider_height));
            mRect = new RectF(0, 0, getWidth(), getHeight());
            LogUtil.d("size ", getWidth() + "  " + getHeight());
            mRadius = getHeight() / 2;
        }

        public void setColor(int color) {
            mPaint.setColor(color);
        }

        public void setSize(int width, int height) {
            mRect.set(0, 0, width, height);
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
