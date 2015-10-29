package co.yishun.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created on 2015/10/29.
 */
public class OnemomentTagView extends RelativeLayout {
    public OnemomentTagView(Context context) {
        super(context);
    }

    public OnemomentTagView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OnemomentTagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OnemomentTagView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
