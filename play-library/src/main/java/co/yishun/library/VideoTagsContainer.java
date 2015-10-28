package co.yishun.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/4/15.
 */
public class VideoTagsContainer extends FrameLayout {
    public final static String VIDEO_TAG_VIEW_TAG = "video_tag";
    private int mSize;
    private List<VideoTag> videoTags;
    private List<View> tagViews;
    private boolean mShowTags = true;
    private boolean mEditable = true;

    private View.OnTouchListener listener = new View.OnTouchListener() {
        private float touchX;
        private float touchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!mEditable) {
                        return false;
                    }
                    touchX = event.getX();
                    touchY = event.getY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX() - touchX;
                    float moveY = event.getY() - touchY;
                    v.setX(v.getX() + moveX);
                    v.setY(v.getY() + moveY);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (v.getY() > getSize()) {
                        videoTags.remove(tagViews.indexOf(v));
                        removeView(v);
                    }
                    return true;
            }
            return false;
        }
    };


    public VideoTagsContainer(Context context) {
        super(context);
        init();
    }

    public VideoTagsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoTagsContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        tagViews = new ArrayList<View>();
    }

    public void addTag(VideoTag tag) {
        if (mEditable) {
            videoTags.add(tag);
            showTags();
        }
    }

    public void showTags() {
        clearTags();

        if (!mShowTags) return;
        if (videoTags == null) return;

        for (int i = 0; i < videoTags.size(); i++) {
            VideoTag videoTag = videoTags.get(i);

            TextView textView;
            if(i < tagViews.size()) {
                textView = (TextView) tagViews.get(i);
            } else {
                textView = new TextView(getContext());
                textView.setOnTouchListener(listener);
                textView.setTag(VIDEO_TAG_VIEW_TAG);
                tagViews.add(textView);
            }
            textView.setText(videoTag.getText());
            // position
            int left = (int) (videoTag.getX() * getSize());
            int top = (int) (videoTag.getY() * getSize());

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(left, top, 0, 0);
            textView.setLayoutParams(params);
            addView(textView);
        }
    }

    private void clearTags() {
        for (int i = 0; i < getChildCount(); ) {
            View v = getChildAt(i);
            Object tag = v.getTag();
            if (tag != null && tag.equals(VIDEO_TAG_VIEW_TAG)) {
                removeViewAt(i);
            } else {
                i++;
            }
        }
    }

    public int getSize() {
        return mSize;
    }

    public List<VideoTag> getVideoTags() {
        return videoTags;
    }

    public void setVideoTags(List<VideoTag> videoTags) {
        this.videoTags = videoTags;
    }

    public boolean isShowTags() {
        return mShowTags;
    }

    public void setShowTags(boolean mShowTags) {
        this.mShowTags = mShowTags;
    }

    public boolean isEditable() {
        return mEditable;
    }

    public void setEditable(boolean mEditable) {
        this.mEditable = mEditable;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size;
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            size = heightSize;
        } else {
            size = widthSize < heightSize ? widthSize : heightSize;
        }

        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        mSize = size;

        if(mShowTags) {
            showTags();
        }
        Log.i("[VTC]", "tag container size " + mSize);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }

}
