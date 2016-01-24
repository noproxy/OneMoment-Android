package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.R;
import co.yishun.library.tag.VideoTag;

/**
 * Created by Jinge on 2015/11/4.
 */
public class TagContainer extends FrameLayout {
    public final static String VIDEO_TAG_VIEW_TAG = "video_tag";
    protected ViewDragHelper mDragHelper;
    protected ViewDragHelper.Callback mCallback;
    protected int mSize;
    protected List<VideoTag> videoTags;
    protected List<View> tagViews;
    protected boolean mEditable;
    protected boolean mSquare;

    public TagContainer(Context context) {
        super(context);
        init(context, null);
    }

    public TagContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TagContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mEditable = false;
        mSquare = true;

        if (attrs != null) {
            TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TagContainer, 0, 0);
            mEditable = ta.getBoolean(R.styleable.TagContainer_tc_editable, mEditable);
            mSquare = ta.getBoolean(R.styleable.TagContainer_tc_square, mSquare);
        }

        mCallback = new ContainerCallback();
        mDragHelper = ViewDragHelper.create(this, mCallback);

        videoTags = new ArrayList<>(3);
        tagViews = new ArrayList<>(3);
    }

    protected View setTextView(VideoTag tag) {
        LinearLayout tagLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_tag, null);
        TextView textView = (TextView) tagLayout.findViewById(R.id.tagTextView);
        tagLayout.setTag(VIDEO_TAG_VIEW_TAG);
        textView.setText(tag.getText());
        int left = (int) (tag.getX() * getSize() / 100);
        int top = (int) (tag.getY() * getSize() / 100);
        textView.setSingleLine(true);
        tagLayout.setX(left);
        tagLayout.setY(top);

        return tagLayout;
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
    }

    public void addTag(VideoTag tag) {
        videoTags.add(tag);
        View textView = setTextView(tag);

        addView(textView);
        tagViews.add(textView);
        textView.measure(0, 0);       //must call measure!
        Log.d("[ETC]", textView.getMeasuredHeight() + " " + textView.getMeasuredWidth());

//        if (textView.getX() + textView.getMeasuredWidth() > mSize)
//            textView.setX(mSize - textView.getMeasuredWidth());
//        else if (textView.getX() < 0)
//            textView.setX(0);
//
//        if (textView.getY() < 0)
//            textView.setY(0);
//        else if (textView.getY() < mSize && textView.getY() > mSize - textView.getMeasuredHeight())
//            textView.setY(mSize - textView.getMeasuredHeight());
    }


    public int getSize() {
        return mSize;
    }

    public void setVideoTags(List<VideoTag> videoTags) {
        this.videoTags = videoTags;
        showTags();
    }

    public void showTags() {
        clearTags();
        if (videoTags == null) return;

        for (int i = 0; i < videoTags.size(); i++) {
            VideoTag videoTag = videoTags.get(i);
            View tagView = setTextView(videoTag);
            tagViews.add(tagView);
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

    public List<VideoTag> getVideoTags() {
        return videoTags;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mEditable) {
            mDragHelper.processTouchEvent(ev);
            return true;
        }else return super.onTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);

        int size = 0;
        if (widthMode == View.MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        }
        mSize = size;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected class ContainerCallback extends ViewDragHelper.Callback {

        @Override public boolean tryCaptureView(View child, int pointerId) {
            return mEditable && child.getTag() != null && child.getTag().equals(VIDEO_TAG_VIEW_TAG);
        }

        @Override public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int leftBound = getPaddingLeft();
            final int rightBound = getWidth() - child.getWidth();
            return Math.min(Math.max(left, leftBound), rightBound);
        }

        @Override public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - child.getHeight();
            final int newTop = Math.min(Math.max(top, topBound), bottomBound);
            return newTop;
        }
    }

}
