package co.yishun.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import co.yishun.library.tag.VideoTag;

/**
 * Created on 2015/10/29.
 */
public class EditTagContainer extends TagContainer {
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
                    if (v.getX() + moveX + v.getWidth() > mSize) {
                        v.setX(mSize - v.getWidth());
                    } else if (v.getX() + moveX < 0) {
                        v.setX(0);
                    } else {
                        v.setX(v.getX() + moveX);
                    }
                    if (v.getY() + moveY < 0) {
                        v.setY(0);
                    } else {
                        v.setY(v.getY() + moveY);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (v.getY() > getSize()) {
                        videoTags.remove(tagViews.indexOf(v));
                        tagViews.remove(v);
                        removeView(v);
                    } else {
                        int index = tagViews.indexOf(v);
                        videoTags.get(index).setX(v.getX() / mSize * 100);
                        videoTags.get(index).setY(v.getY() / mSize * 100);
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
        this.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return event.getY() <= mSize;
                case MotionEvent.ACTION_MOVE:
                    return true;
                case MotionEvent.ACTION_UP:
                    if (event.getY() > mSize)
                        return true;
                    onAddTag(event.getX() / mSize * 100, event.getY() / mSize * 100);
                    return true;
            }
            return false;
        });
    }

    public void addTag(VideoTag tag) {
        videoTags.add(tag);
        TextView textView = setTextView(tag);
        textView.setOnTouchListener(listener);
        addView(textView);
        tagViews.add(textView);
        Log.d("[ETC]", textView.getWidth() + " " + textView.getHeight());
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
