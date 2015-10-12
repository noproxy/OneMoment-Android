package co.yishun.onemoment.app.ui.view;

/**
 * Created by Carlos on 2015/10/12.
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

import co.yishun.onemoment.app.R;

/**
 * An ImageButton
 * <p>
 * Created by Carlos on 2015/4/13.
 */
public class TintableImageButton extends ImageButton {
    private int mTintUpdateDelay;
    private ColorStateList mColorStateList;


    public TintableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);


        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TintableImageButton);
        mColorStateList = typedArray.getColorStateList(R.styleable.TintableImageButton_tintColorStateList);
        mTintUpdateDelay = typedArray.getInt(R.styleable.TintableImageButton_tintUpdateDelay, 300);
        typedArray.recycle();


        String focusable = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "focusable");
        if (focusable != null)
            setFocusable(Boolean.parseBoolean(focusable));
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mColorStateList == null) return;
        if (mTintUpdateDelay <= 0) updateTint();
        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateTint();
                }
            }, mTintUpdateDelay);
        }
    }


    private void updateTint() {
        if (mColorStateList == null)
            return;

        int tint = mColorStateList.getColorForState(getDrawableState(), 0x00000000);
        setColorFilter(tint, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        updateTint();
        return result;
    }
}