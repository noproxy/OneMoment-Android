package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.modelv4.ListErrorProvider;
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
    private synchronized ListErrorProvider<I> synchronizedLoad() {
        return onLoad();
    }

    /**
     * load data from network, this will be call in the background.
     *
     * @return list of I, null if error occurs in loading.
     */
    protected abstract ListErrorProvider<I> onLoad();

    /**
     * Respond to error occurs in loading. You should make a reflection to user now. But you don't need to care about the load progress view.
     * <p>
     * By default, this method make a snackbar to tell user error in loading.
     */
    @UiThread
    protected void onLoadError() {
        Snackbar.make(getRecyclerView(), R.string.text_load_error, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Add loaded result to adapter, call {@link #onLoadError()} if error occur and remove refresh and loading view.
     * <p>
     * Don't forget to call {@link #onLoadError()} and to remove refresh and loading view if you override.
     */
    @UiThread void onLoadEnd(ListErrorProvider<I> list) {
        if (list == null || !list.isSuccess()) {
            onLoadError();
        } else {
            getAdapter().addAll(list);
        }

        if (mRecyclerView instanceof SuperRecyclerView) {
            ((SuperRecyclerView) mRecyclerView).getSwipeToRefresh().setRefreshing(false);
        }
        if (mRecyclerView instanceof HeaderCompatibleSuperRecyclerView) {
            ((HeaderCompatibleSuperRecyclerView) mRecyclerView).loadEnd();
        }
    }
}
