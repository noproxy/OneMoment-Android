package co.yishun.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/4/15.
 */
public class PlayTagContainer extends TagContainer {
    private boolean mShowTags = true;

    public PlayTagContainer(Context context) {
        super(context);
        init();
    }

    public PlayTagContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayTagContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        tagViews = new ArrayList<View>();
    }

    public void showTags() {
        clearTags();

        if (!mShowTags) return;
        if (videoTags == null) return;

        for (int i = 0; i < videoTags.size(); i++) {
            VideoTag videoTag = videoTags.get(i);
            TextView textView = setTextView(videoTag);
            if (getChildCount() >= 2)
                addView(textView, getChildCount() - 2);
            else addView(textView);
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

    public void setVideoTags(List<VideoTag> videoTags) {
        this.videoTags = videoTags;
        showTags();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int size = 0;
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        }
        mSize = size;
        if (mShowTags) {
            showTags();
        }
        Log.i("[VTC]", "tag container size " + mSize);
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
