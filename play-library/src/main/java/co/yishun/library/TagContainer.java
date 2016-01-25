package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.util.ArrayMap;
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
import java.util.Map;
import java.util.Set;

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
    protected boolean mEditable;
    protected boolean mSquare;
    Map<View, Rect> viewRectMap;

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
        viewRectMap = new ArrayMap<>(3);
    }

    protected View createTagView(VideoTag tag) {
        LinearLayout tagView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.layout_tag, this, false);
        tagView.setTag(VIDEO_TAG_VIEW_TAG);

        TextView tagText = (TextView) tagView.findViewById(R.id.tagTextView);
        tagText.setText(tag.getText());
        tagText.setSingleLine(true);

        if (mEditable) {
            tagView.findViewById(R.id.tagClearImage).setOnClickListener(v -> {
                viewRectMap.remove(tagView);
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
        videoTags.add(tag);
        showTags();
    }


    public int getSize() {
        return mSize;
    }

    public void showTags() {
        clearTagsMap();

        if (videoTags == null) return;

        for (int i = 0; i < videoTags.size(); i++) {
            VideoTag tag = videoTags.get(i);

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
            viewRectMap.put(tagView, new Rect(params.leftMargin, params.topMargin,
                    params.leftMargin + tagView.getMeasuredWidth(), params.topMargin + tagView.getMeasuredHeight()));
        }
    }

    private void clearTagsMap() {
        Log.d(TAG, "clear tags : " + videoTags.size());
        Set<View> viewSet = viewRectMap.keySet();

        for (View v : viewSet) {
            removeView(v);
        }viewRectMap.clear();
    }

    public List<VideoTag> getVideoTags() {
        return videoTags;
    }

    public void setVideoTags(List<VideoTag> videoTags) {
        this.videoTags = videoTags;
        showTags();
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (View v : viewRectMap.keySet()) {
            LayoutParams params = (LayoutParams) v.getLayoutParams();
            Rect rect = viewRectMap.get(v);
            params.topMargin = rect.top;
            params.leftMargin = rect.left;
        }
    }

    private int checkHorizontal(View view, int left, int dx) {
        return 0;
    }

    private int checkVertival(View view) {
        return 0;
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
            Rect rect = viewRectMap.get(child);
            rect.offsetTo(newLeft, rect.top);
            return newLeft;
        }

        @Override public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - child.getHeight();
            final int newTop = Math.min(Math.max(top, topBound), bottomBound);
            Rect rect = viewRectMap.get(child);
            rect.offsetTo(rect.left, newTop);
            return newTop;
        }
    }

}
