package co.yishun.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created on 2015/10/29.
 */
public class PlaySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "PlaySurfaceView";
    private SurfaceHolder mHolder;
    private OMVideoPlayer mVideoPlayer;
    private boolean surfeceCreate = false;
    private boolean holderSet = false;

    public PlaySurfaceView(Context context) {
        super(context);
        init();
    }

    public PlaySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Log.d(TAG, "init");
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void setPlayer(OMVideoPlayer player, OMVideoPlayer.PlayListener listener) {
        Log.d(TAG, "set player");
        mVideoPlayer = player;
        mVideoPlayer.setListener(listener);
        if (!holderSet && surfeceCreate) {
            mVideoPlayer.setDisplay(mHolder);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surface create");
        surfeceCreate = true;
        if (mVideoPlayer != null) {
            mVideoPlayer.setDisplay(holder);
            holderSet = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surface change");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surface destroy");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
