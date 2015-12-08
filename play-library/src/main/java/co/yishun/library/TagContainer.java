package co.yishun.library;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by Jinge on 2015/11/4.
 */
public class TagContainer extends RelativeLayout{
    public final static String VIDEO_TAG_VIEW_TAG = "video_tag";
    protected int mSize;
    protected List<VideoTag> videoTags;
    protected List<View> tagViews;
    protected boolean mShowTags = true;

    public TagContainer(Context context) {
        super(context);
        init();
    }

    public TagContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
    }

    protected TextView setTextView(VideoTag tag) {
        TextView textView = new TextView(getContext());
        textView.setTag(VIDEO_TAG_VIEW_TAG);
        textView.setText(tag.getText());
        int left = (int) (tag.getX() * getSize() / 100);
        int top = (int) (tag.getY() * getSize() / 100);
        textView.setSingleLine(true);
        textView.setX(left);
        textView.setY(top);
        textView.setBackgroundResource(R.drawable.bg_tag);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
//        textView.setTextSize(12);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        return textView;
    }

    public int getSize() {
        return mSize;
    }

    public List<VideoTag> getVideoTags() {
        return videoTags;
    }

    public boolean isShowTags() {
        return mShowTags;
    }

    public void setShowTags(boolean mShowTags) {
        this.mShowTags = mShowTags;
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

}
