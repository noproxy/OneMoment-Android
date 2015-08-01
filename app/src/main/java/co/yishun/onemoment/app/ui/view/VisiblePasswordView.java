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
    private float mTouchLeft;
    private float mTouchRight;
    private float mTouchTop;
    private float mTouchBottom;
    private int mInputTypeVisible;
    private int mInputTypeInvisible;
    private float mMinTouchHeight;
    private float mMinTouchWidth;

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
        mMinTouchHeight = getResources().getDimension(R.dimen.default_drawable_touch_height);
        mMinTouchWidth = getResources().getDimension(R.dimen.default_drawable_touch_width);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.VisiblePasswordView, defStyleAttr, 0);
        if (a.hasValue(R.styleable.VisiblePasswordView_visibleDrawable)) {
            mDrawableVisible = a.getDrawable(R.styleable.VisiblePasswordView_visibleDrawable);
        } else {
            if (Build.VERSION.SDK_INT > 21)
                mDrawableVisible = getContext().getResources().getDrawable(R.drawable.ic_login_eye_open, null);
            else
                mDrawableVisible = getContext().getResources().getDrawable(R.drawable.ic_login_eye_open);
        }
        if (a.hasValue(R.styleable.VisiblePasswordView_inVisibleDrawable)) {
            mDrawableInvisible = a.getDrawable(R.styleable.VisiblePasswordView_inVisibleDrawable);
        } else {
            if (Build.VERSION.SDK_INT > 21)
                mDrawableInvisible = getContext().getResources().getDrawable(R.drawable.ic_login_eye_off, null);
            else
                mDrawableInvisible = getContext().getResources().getDrawable(R.drawable.ic_login_eye_off);
        }

        mMinTouchHeight = a.getDimension(R.styleable.VisiblePasswordView_minTouchHeight, mMinTouchHeight);
        mMinTouchWidth = a.getDimension(R.styleable.VisiblePasswordView_minTouchWidth, mMinTouchWidth);


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


        mTouchRight = right = getMeasuredWidth() - getPaddingRight();
        mTouchLeft = left = right - drawableWidth;
        mTouchTop = top = (getMeasuredHeight() - drawableHeight) / 2;
        mTouchBottom = bottom = getMeasuredHeight() - ((getMeasuredHeight() - drawableHeight) / 2);

        float widthPlus = mMinTouchWidth - (left - right);
        float heightPlus = mMinTouchHeight - (bottom - top);

        if (widthPlus > 0) {
            mTouchRight += widthPlus / 2;
            mTouchLeft -= widthPlus / 2;
        }
        if (heightPlus > 0) {
            mTouchTop -= heightPlus / 2;
            mTouchBottom += heightPlus / 2;
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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
        boolean result = x > mTouchLeft && x < +mTouchRight && y > +mTouchTop && y < mTouchBottom;
        Log.i("onTouch", "In visibility Area " + result + " {" +
                "left=" + left +
                ", right=" + right +
                ", top=" + top +
                ", bottom=" + bottom +
                ", mTouchLeft=" + mTouchLeft +
                ", mTouchRight=" + mTouchRight +
                ", mTouchTop=" + mTouchTop +
                ", mTouchBottom=" + mTouchBottom +
                '}');
        return result;
    }
}
