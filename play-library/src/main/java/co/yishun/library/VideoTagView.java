package co.yishun.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import co.yishun.library.tag.VideoTag;

/**
 * TODO: document your custom view class.
 */
public class VideoTagView extends View {
    private VideoTag mVideoTag;
    private Paint mTextPaint;

    public VideoTagView(Context context) {
        super(context);
        init(null, 0);
    }

    public VideoTagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public VideoTagView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void setVideoTag(VideoTag tag) {
        mVideoTag = tag;
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i("[VTV]", "start draw");
        canvas.drawText(mVideoTag.getText(), 0.0f, 0.0f, mTextPaint);
    }
}
