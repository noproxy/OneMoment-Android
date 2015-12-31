package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public abstract class RecyclerController<Offset, V extends ViewGroup, I, VH extends RecyclerView.ViewHolder> {
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
    protected void setUp(AbstractRecyclerViewAdapter<I, VH> adapter, V recyclerView, Offset offset) {
        this.mAdapter = adapter;
        this.mRecyclerView = recyclerView;
        this.mOffset = offset;
        load();
    }

    public AbstractRecyclerViewAdapter<I, VH> getAdapter() {
        return mAdapter;
    }

    public V getRecyclerView() {
        return mRecyclerView;
    }


    @Background void load() {
        onLoadEnd(synchronizedLoad());
    }

    /*
    to ensure onLoad() execute synchronized
     */
    private synchronized List<I> synchronizedLoad() {
        return onLoad();
    }

    /**
     * load data from network, this will be call in the background.
     *
     * @return list of I, null if error occurs in loading.
     */
    protected abstract List<I> onLoad();

    @UiThread
    protected abstract void onLoadError();

    @UiThread void onLoadEnd(List<I> list) {
        if (list != null) {
            getAdapter().addAll(list);
            if (mRecyclerView instanceof SuperRecyclerView) {
                ((SuperRecyclerView) mRecyclerView).getSwipeToRefresh().setRefreshing(false);
            }
            if (mRecyclerView instanceof HeaderCompatibleSuperRecyclerView) {
                ((HeaderCompatibleSuperRecyclerView) mRecyclerView).loadEnd();
            }
        } else {
            onLoadError();
        }
    }
}
