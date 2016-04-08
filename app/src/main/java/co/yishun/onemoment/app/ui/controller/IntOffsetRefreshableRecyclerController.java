package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;

import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public abstract class IntOffsetRefreshableRecyclerController<V extends SuperRecyclerView, I, VH extends RecyclerView.ViewHolder> extends RefreshableRecyclerController<Integer, V, I, VH> {
    protected IntOffsetRefreshableRecyclerController(Context context) {
        super(context);
    }

    @Override
    protected void resetOffset() {
        setOffset(0);
    }

    public void setUp(AbstractRecyclerViewAdapter<I, VH> adapter, V recyclerView) {
        super.setUp(adapter, recyclerView, 0);
    }
}
