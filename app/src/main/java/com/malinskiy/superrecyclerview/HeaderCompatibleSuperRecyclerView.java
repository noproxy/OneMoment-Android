package com.malinskiy.superrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Carlos on 2015/8/16.
 */
public class HeaderCompatibleSuperRecyclerView extends SuperRecyclerView {
    public HeaderCompatibleSuperRecyclerView(Context context) {
        super(context);
    }

    public HeaderCompatibleSuperRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderCompatibleSuperRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * To make super recycler view work properly after loading more, you must call it after you
     * update adapter data set if you use HeaderRecyclerAdapter.
     */
    public void loadEnd() {
        mProgress.setVisibility(View.GONE);
        mMoreProgress.setVisibility(View.GONE);
        isLoadingMore = false;
        mPtrLayout.setRefreshing(false);
        if (mRecycler.getAdapter().getItemCount() == 0 && mEmptyId != 0) {
            mEmpty.setVisibility(View.VISIBLE);
        } else if (mEmptyId != 0) {
            mEmpty.setVisibility(View.GONE);
        }
    }
}
