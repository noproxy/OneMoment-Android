package co.yishun.onemoment.app.ui.view.shoot;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * An GLSurface whose height equals width. You must use match_parent instead of wrap_content.
 * Created by Carlos on 2015/10/13.
 */
public class SquareGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "SquareGlSurfaceView";

    public SquareGLSurfaceView(Context context) {
        super(context);
    }

    public SquareGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(
                getMeasuredWidth(),
                getMeasuredHeight()
        );

        Log.i(TAG, "size: " + size);
        setMeasuredDimension(size, size);
    }
}