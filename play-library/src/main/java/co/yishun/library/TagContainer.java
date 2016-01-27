package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
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
    protected List<View> tagViews;
    protected List<Rect> tagRects;
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
        tagRects = new ArrayList<>(3);
    }

    protected View createTagView(VideoTag tag) {
        LinearLayout tagView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_tag, this, false);
        tagView.setTag(VIDEO_TAG_VIEW_TAG);

        TextView tagText = (TextView) tagView.findViewById(R.id.tagTextView);
        tagText.setText(tag.getText());
        tagText.setSingleLine(true);

        if (mEditable) {
            tagView.findViewById(R.id.tagClearImage).setOnClickListener(v -> {
                int index = tagViews.indexOf(tagView);
                if (index >= 0) {
                    tagViews.remove(index);
                    videoTags.remove(index);
                    tagRects.remove(index);
                }
                removeView(tagView);
            });
        } else {
            tagView.findViewById(R.id.tagClearImage).setVisibility(GONE);
            LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) tagText.getLayoutParams();
            textParams.rightMargin = textParams.leftMargin;
        }
        return tagView;
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
    }

    public void addTag(VideoTag tag) {
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

        Rect rect = new Rect(params.leftMargin, params.topMargin,
                params.leftMargin + tagView.getMeasuredWidth(), params.topMargin + tagView.getMeasuredHeight());
        videoTags.add(tag);
        tagViews.add(tagView);
        tagRects.add(rect);
        checkVertical(tagRects.size() - 1);
        params.leftMargin = rect.left;
        params.topMargin = rect.top;

        tagView.setLayoutParams(params);
        addView(tagView);
    }

    public int getSize() {
        return mSize;
    }

    private void clearTags() {
        for (View v : tagViews) {
            removeView(v);
        }
        videoTags.clear();
        tagViews.clear();
        tagRects.clear();
    }

    public List<VideoTag> getVideoTags() {
        for (int i = 0; i < videoTags.size(); i++) {
            videoTags.get(i).setX(tagRects.get(i).left * 100f / mSize);
            videoTags.get(i).setY(tagRects.get(i).top * 100f / mSize);
        }
        return videoTags;
    }

    public void setVideoTags(List<VideoTag> videoTags) {
        clearTags();
        for (VideoTag tag : videoTags) {
            addTag(tag);
        }
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (int i = 0; i < tagViews.size(); i++) {
            View v = tagViews.get(i);
            LayoutParams params = (LayoutParams) v.getLayoutParams();
            Rect rect = tagRects.get(i);
            params.topMargin = rect.top;
            params.leftMargin = rect.left;
        }
    }

    private void checkHorizontal(int index) {
        Rect origin = tagRects.get(index);
        for (int i = 0; i < tagViews.size(); i++) {
            Rect other = tagRects.get(i);
            if (index != i && Rect.intersects(origin, other)) {
                if (origin.right > other.right && origin.left < other.right) {
                    origin.offsetTo(other.right, origin.top);
                }
                if (origin.left < other.left && origin.right > other.left) {
                    origin.offsetTo(other.left - origin.width(), origin.top);
                }
            }
        }
    }

    private void checkVertical(int index) {
        Rect origin = tagRects.get(index);
        for (int i = 0; i < tagViews.size(); i++) {
            Rect other = tagRects.get(i);
            if (index != i && Rect.intersects(origin, other)) {
                if (origin.bottom >= other.bottom && origin.top < other.bottom) {
                    origin.offsetTo(origin.left, other.bottom);
                }
                if (origin.top < other.top && origin.bottom > other.top) {
                    origin.offsetTo(origin.left, other.top - origin.height());
                }
            }
        }
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

            int index = tagViews.indexOf(child);
            Rect rect = tagRects.get(index);
            rect.offsetTo(newLeft, rect.top);
            checkHorizontal(index);
            return rect.left;
        }

        @Override public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - child.getHeight();
            final int newTop = Math.min(Math.max(top, topBound), bottomBound);

            int index = tagViews.indexOf(child);
            Rect rect = tagRects.get(index);
            rect.offsetTo(rect.left, newTop);
            checkVertical(index);
            return rect.top;
        }
    }

}
