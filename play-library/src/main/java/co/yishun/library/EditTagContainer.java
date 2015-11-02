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
 * Created on 2015/10/29.
 */
public class EditTagContainer extends FrameLayout {
    public final static String VIDEO_TAG_VIEW_TAG = "video_tag";
    private int mSize;
    private List<VideoTag> videoTags;
    private List<View> tagViews;
    private OnAddTagListener addTagListener;

    private View.OnTouchListener listener = new View.OnTouchListener() {
        private float touchX;
        private float touchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
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
                        tagViews.remove(v);
                        removeView(v);
                    }
                    return true;
            }
            return false;
        }
    };

    public EditTagContainer(Context context) {
        super(context);
        init();
    }

    public EditTagContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTagContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        tagViews = new ArrayList<>();
        videoTags = new ArrayList<>();
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getY() > mSize)
                        return true;
                    onAddTag(event.getX() / mSize, event.getY() / mSize);
                    return true;
                }
                return false;
            }
        });
    }

    public void addTag(VideoTag tag) {
        videoTags.add(tag);
        TextView textView = new TextView(getContext());
        textView.setOnTouchListener(listener);
        textView.setTag(VIDEO_TAG_VIEW_TAG);

        tagViews.add(textView);
        textView.setText(tag.getText());
        // position
        int left = (int) (tag.getX() * getSize());
        int top = (int) (tag.getY() * getSize());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(left, top, 0, 0);
        textView.setLayoutParams(params);
        addView(textView);
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

    public void setOnAddTagListener(OnAddTagListener listener) {
        this.addTagListener = listener;
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
        mSize = size;

        Log.i("[VTC]", "tag container size " + mSize);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void onAddTag(float x, float y) {
        if (addTagListener != null) {
            addTagListener.onAddTag(x, y);
        }
    }

    public interface OnAddTagListener {
        void onAddTag(float x, float y);
    }

}
