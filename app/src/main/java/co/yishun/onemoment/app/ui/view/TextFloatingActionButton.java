package co.yishun.onemoment.app.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.TypedValue;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 8/1/15.
 */
public class TextFloatingActionButton extends FloatingActionButton {
    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_TEXT_SIZE = 15;
    private CharSequence mText = "";
    private Paint mPaint;
    private int mIntrinsicWidth;
    private int mIntrinsicHeight;
    private int mTextColor;
    private float mTextSize;

    public TextFloatingActionButton(Context context) {
        super(context);
        init(null, 0);
    }

    public TextFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TextFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextFloatingActionButton, defStyleAttr, 0);
        CharSequence text = a.getText(R.styleable.TextFloatingActionButton_android_text);
        mTextColor = a.getColor(R.styleable.TextFloatingActionButton_android_textColor, DEFAULT_COLOR);
        mTextSize = a.getDimension(R.styleable.TextFloatingActionButton_android_textSize, DEFAULT_TEXT_SIZE);

        a.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setTextAlign(Paint.Align.CENTER);

        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, getResources().getDisplayMetrics());
        mPaint.setTextSize(textSize);

        setText(text);
    }

    private void invalidateText() {
        mIntrinsicWidth = (int) (mPaint.measureText(mText, 0, mText.length()) + .5);
        mIntrinsicHeight = mPaint.getFontMetricsInt(null);
    }

    public void setTextColor(@ColorRes int colorRes) {
        mTextColor = colorRes;
        mPaint.setColor(getResources().getColor(mTextColor));
        invalidate();
    }

    public CharSequence getText() {
        return mText;
    }

    public void setText(CharSequence s) {
        if (s == null) {
            s = "";
        }
        if (!mText.equals(s)) {
            mText = s;
            invalidateText();
            invalidate();
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mText, 0, mText.length(), getMeasuredWidth() / 2, getMeasuredHeight() / 2, mPaint);
        //TODO why this fuck text always not center!
    }
}
