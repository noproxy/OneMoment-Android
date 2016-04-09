package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;

import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public abstract class RefreshableRecyclerController<Offset, V extends SuperRecyclerView, I, VH extends RecyclerView.ViewHolder> extends RecyclerController<Offset, V, I, VH> implements SwipeRefreshLayout.OnRefreshListener {
    protected RefreshableRecyclerController(Context context) {
        super(context);
    }

    @Override
    protected void setUp(AbstractRecyclerViewAdapter<I, VH> adapter, V recyclerView, Offset offset) {
        recyclerView.setRefreshListener(this);
        super.setUp(adapter, recyclerView, offset);
    }

    @Override
    public void onRefresh() {
        getAdapter().clear();
        resetOffset();
        load();
    }

    protected abstract void resetOffset();
}
