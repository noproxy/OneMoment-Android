package co.yishun.onemoment.app.ui.other;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;

import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
public abstract class RecyclerController<Offset, V extends RecyclerView, I, VH extends RecyclerView.ViewHolder> {
    protected final Context mContext;
    private AbstractRecyclerViewAdapter<I, VH> mAdapter;
    private V mRecyclerView;
    private Offset mOffset;

    protected RecyclerController(Context context) {
        this.mContext = context;
    }

    public Offset getOffset() {
        return mOffset;
    }

    public void setOffset(Offset offset) {
        this.mOffset = offset;
    }

    @CallSuper
    public void setUp(AbstractRecyclerViewAdapter<I, VH> adapter, V recyclerView, Offset offset) {
        this.mAdapter = adapter;
        this.mRecyclerView = recyclerView;
        this.mOffset = offset;
    }

    public AbstractRecyclerViewAdapter<I, VH> getAdapter() {
        return mAdapter;
    }

    public V getRecyclerView() {
        return mRecyclerView;
    }

}
