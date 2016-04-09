package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.api.APIV4;
import co.yishun.onemoment.app.api.WorldAPI;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.authentication.OneMomentV4;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.modelv4.ListWithErrorV4;
import co.yishun.onemoment.app.api.modelv4.World;
import co.yishun.onemoment.app.ui.adapter.BannerHeaderProvider;
import co.yishun.onemoment.app.ui.adapter.DiscoveryAdapter;
import co.yishun.onemoment.app.ui.adapter.HeaderRecyclerAdapter;

/**
 * Created by Jinge on 2016/1/20.
 */
@EBean
public class DiscoveryController implements SwipeRefreshLayout.OnRefreshListener, OnMoreListener {
    private static final String TAG = "DiscoveryController";
    AtomicBoolean isLoading = new AtomicBoolean(false);
    private WorldAPI mWorldAPI = OneMomentV3.createAdapter().create(WorldAPI.class);
    private APIV4 mApiV4 = OneMomentV4.createAdapter().create(APIV4.class);
    private DiscoveryAdapter mAdapter;
    private int mRanking;
    private HeaderCompatibleSuperRecyclerView mRecyclerView;
    private BannerHeaderProvider mBannerHeaderProvider;

    public DiscoveryController() {

    }

    public void setUp(Context context, HeaderCompatibleSuperRecyclerView recyclerView, DiscoveryAdapter.OnItemClickListener<World> listener) {
        mRecyclerView = recyclerView;
        mRanking = (int) Util.unixTimeStamp();
        mAdapter = new DiscoveryAdapter(context, listener);
        RecyclerView.Adapter trueAdapter = mAdapter;

        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setRefreshListener(this);

        mBannerHeaderProvider = new BannerHeaderProvider(context);
        trueAdapter = new HeaderRecyclerAdapter(trueAdapter, mBannerHeaderProvider);
        mRecyclerView.setAdapter(trueAdapter);

        loadBanners();
        loadTags();
    }

    public DiscoveryAdapter getAdapter() {
        return mAdapter;
    }

    public SuperRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Background
    void loadBanners() {
        ListWithError<Banner> banners = mWorldAPI.getBanners(null);
        if (banners.isSuccess()) {
            onLoadBanners(banners);
        } else {
            LogUtil.i(TAG, "load banner error");
        }
    }

    @UiThread
    void onLoadBanners(List<Banner> banners) {
        mBannerHeaderProvider.setupBanners(banners);
    }

    @Background
    void loadTags() {
        if (isLoading.get()) {
            LogUtil.i(TAG, "isLoading, cancel");
            return;
        }
        isLoading.set(true);
        synchronizedLoadTags();
    }

    synchronized void synchronizedLoadTags() {
        ListWithErrorV4<World> list = mApiV4.getTodayWorlds(mRanking, 6);
        if (list.isSuccess()) {
            if (list.size() > 0) {
                mRanking = list.get(list.size() - 1).ranking;
                onLoadTags(list);
                onLoadEnd();
            } else {
                onLoadNothing();
            }
        } else {
            onLoadError();
            onLoadEnd();
        }
    }

    @UiThread
    void onLoadNothing() {
        Snackbar.make(mRecyclerView, R.string.text_load_nothing, Snackbar.LENGTH_LONG).show();
        LogUtil.i(TAG, "load to nothing. Disable loading more.");
        isLoading.set(false);
        mRecyclerView.setOnMoreListener(null);
        mRecyclerView.loadEnd();
    }

    @UiThread
    void onLoadError() {
        Snackbar.make(mRecyclerView, R.string.text_load_error, Snackbar.LENGTH_LONG).show();
    }

    @UiThread
    void onLoadTags(List<World> list) {
        LogUtil.i(TAG, "add " + list.size() + " items to adapter.");
        mAdapter.addAll(list);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @UiThread
    void onLoadEnd() {
        LogUtil.i(TAG, "load end");
        isLoading.set(false);
        mRecyclerView.setOnMoreListener(this);
        mRecyclerView.loadEnd();
    }

    @Override
    public void onRefresh() {
        mAdapter.clear();
        mRanking = (int) Util.unixTimeStamp();
        loadTags();
    }

    @Override
    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
        LogUtil.i(TAG, "start load more, int numberOfItems, int numberBeforeMore, int currentItemPos: " + numberOfItems + ", " + numberBeforeMore + ", " + currentItemPos);
        mRecyclerView.setOnMoreListener(null);
        loadTags();
    }
}
