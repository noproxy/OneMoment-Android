package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Carlos on 2015/8/25.
 */
public class SquareFrameLayout extends FrameLayout {
    public SquareFrameLayout(Context context) {
        super(context);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
//        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
//        setMeasuredDimension(width, width);
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

//    @Override
//    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
//        super.onSizeChanged(w, w, oldw, oldh);
//    }
}
