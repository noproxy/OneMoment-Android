package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/25/15.
 */
public class VisiblePasswordView extends EditText {
    boolean touched = false;
    private Drawable mDrawableVisible;
    private Drawable mDrawableInvisible;
    private boolean visible = false;
    private int left;
    private int right;
    private int top;
    private int bottom;
    private int mInputTypeVisible;
    private int mInputTypeInvisible;

    public VisiblePasswordView(Context context) {
        super(context);
        init(null, 0);
    }

    public VisiblePasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public VisiblePasswordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP) public VisiblePasswordView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    @SuppressWarnings("deprecation")
    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VisiblePasswordView, defStyleAttr, 0);
        if (a.hasValue(R.styleable.VisiblePasswordView_visibleDrawable)) {
            mDrawableVisible = a.getDrawable(R.styleable.VisiblePasswordView_visibleDrawable);
        } else {
            if (Build.VERSION.SDK_INT > 21)
                mDrawableVisible = getContext().getResources().getDrawable(R.drawable.pic_login_eye_open, null);
            else
                mDrawableVisible = getContext().getResources().getDrawable(R.drawable.pic_login_eye_open);
        }
        if (a.hasValue(R.styleable.VisiblePasswordView_inVisibleDrawable)) {
            mDrawableInvisible = a.getDrawable(R.styleable.VisiblePasswordView_inVisibleDrawable);
        } else {
            if (Build.VERSION.SDK_INT > 21)
                mDrawableInvisible = getContext().getResources().getDrawable(R.drawable.ic_alarm, null);
            else
                mDrawableInvisible = getContext().getResources().getDrawable(R.drawable.ic_alarm);
        }


        mInputTypeVisible = a.getInt(R.styleable.VisiblePasswordView_inputTypeVisible, EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        mInputTypeInvisible = a.getInt(R.styleable.VisiblePasswordView_inputTypeInvisible, EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        this.setInputType(mInputTypeInvisible);
        a.recycle();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.i("onMeasure",
                "left: " + getLeft() + " right: " + getRight() + " top: " + getTop() + " bottom: " + getBottom()
                        + " width: " + getWidth() + " height: " + getHeight() +
                        " measuredWidth: " + getMeasuredWidth() + " measuredHeight: " + getMeasuredHeight()

        );
        final int drawableWidth = Math.max(mDrawableInvisible.getIntrinsicWidth(), mDrawableVisible.getIntrinsicWidth());
        final int drawableHeight = Math.max(mDrawableInvisible.getIntrinsicHeight(), mDrawableVisible.getIntrinsicHeight());

        left = getMeasuredWidth() - getPaddingRight() - drawableWidth;
        right = getMeasuredWidth() - getPaddingRight();
        top = (getMeasuredHeight() - drawableHeight) / 2;
        bottom = getMeasuredHeight() - ((getMeasuredHeight() - drawableHeight) / 2);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.i("onDraw",
                "left: " + getLeft() + " right: " + getRight() + " top: " + getTop() + " bottom: " + getBottom()
                        + " width: " + getWidth() + " height: " + getHeight() +
                        " measuredWidth: " + getMeasuredWidth() + " measuredHeight: " + getMeasuredHeight()

        );


        Log.i("onDraw", " left: " + left +
                        " right: " + right +
                        " top: " + top +
                        " bottom: " + bottom
        );

        final Drawable toDraw;
        if (visible)
            toDraw = mDrawableVisible;
        else
            toDraw = mDrawableInvisible;
        toDraw.setBounds(left, top, right, bottom);
        toDraw.draw(canvas);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        float touchedX = event.getX();
        float touchedY = event.getY();
        if (!isInVisibleIcon(touchedX, touchedY))
            return super.onTouchEvent(event);

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touched = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                touched = false;
                break;
            case MotionEvent.ACTION_UP:
                if (touched) {
                    // clicked
                    onSwitchVisible();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                touched = false;
                break;
            case MotionEvent.ACTION_OUTSIDE:
                touched = false;
                break;
            default:
                break;
        }
        return true;
    }

    private void onSwitchVisible() {
        if (visible) {
            setInputType(mInputTypeInvisible);
        } else {
            setInputType(mInputTypeVisible);
        }
        visible = !visible;
    }

    private boolean isInVisibleIcon(float x, float y) {
        Log.i("onTouch", "x: " + x + " y: " + y);
        Log.i("1", "" + (getLeft() + left));
        Log.i("2", "" + (getLeft() + right));
        Log.i("3", "" + (getTop() + top));
        Log.i("4", "" + (getTop() + bottom));
        Log.i("result", String.valueOf(x > (getLeft() + left) && x < (getLeft() + right)
                && y > (getTop() + top) && y < (getTop() + bottom)));

        return x > left && x < +right
                && y > +top && y < bottom;
    }

}
