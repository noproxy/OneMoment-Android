package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by Jinge on 2015/11/4.
 */
public class TagContainer extends FrameLayout {
    public final static String VIDEO_TAG_VIEW_TAG = "video_tag";
    private static final String TAG = "TagContainer";
    protected ViewDragHelper mDragHelper;
    protected ViewDragHelper.Callback mCallback;
    protected int mSize;
    protected List<VideoTag> videoTags;

    protected List<RectF> tagViewRects;
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
//        tagViews = new ArrayList<>(3);
        tagViewRects = new ArrayList<>(3);
    }

    protected View createTagView(VideoTag tag) {
        LinearLayout tagView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_tag, this, false);
        tagView.setTag(VIDEO_TAG_VIEW_TAG);


        TextView tagText = (TextView) tagView.findViewById(R.id.tagTextView);
        tagText.setText(tag.getText());
        tagText.setSingleLine(true);

        tagView.findViewById(R.id.tagClearImage).setOnClickListener(v -> {
//            removeView(tagView);
            tagView.setVisibility(INVISIBLE);
        });
        return tagView;
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
    }

    public void addTag(VideoTag tag) {
        videoTags.add(tag);
        View tagView = createTagView(tag);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int left = (int) (tag.getX() * getSize() / 100);
        int top = (int) (tag.getY() * getSize() / 100);
        params.leftMargin = left;
        params.topMargin = top;

        tagView.measure(0, 0);       //must call measure!
        Log.d(TAG, tagView.getMeasuredHeight() + " " + tagView.getMeasuredWidth());

        if (params.leftMargin + tagView.getMeasuredWidth() > mSize)
            params.leftMargin = mSize - tagView.getMeasuredWidth();
        else if (params.leftMargin < 0)
            params.leftMargin = 0;

        if (params.topMargin < 0)
            params.topMargin = 0;
        else if (params.topMargin > mSize - tagView.getMeasuredHeight())
            params.topMargin = mSize - tagView.getMeasuredHeight();

        tagView.setLayoutParams(params);

        addView(tagView);
        tagViewRects.add(new RectF(params.leftMargin, params.topMargin,
                params.leftMargin + tagView.getMeasuredWidth(), params.topMargin + tagView.getMeasuredHeight()));

    }


    public int getSize() {
        return mSize;
    }

    public void showTags() {
        clearTags();
        if (videoTags == null) return;

        for (int i = 0; i < videoTags.size(); i++) {
            VideoTag videoTag = videoTags.get(i);
            addTag(videoTag);
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

    public void setVideoTags(List<VideoTag> videoTags) {
        this.videoTags = videoTags;
        showTags();
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
        } else return super.onTouchEvent(ev);
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
            int leftBound = getPaddingLeft();
            int rightBound = getWidth() - child.getWidth();
            int newLeft = Math.min(Math.max(left, leftBound), rightBound);
            return newLeft;
        }

        @Override public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - child.getHeight();
            final int newTop = Math.min(Math.max(top, topBound), bottomBound);
            return newTop;
        }
    }

}
