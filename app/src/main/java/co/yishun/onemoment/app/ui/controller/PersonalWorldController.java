package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.BannerHeaderProvider;
import co.yishun.onemoment.app.ui.adapter.HeaderRecyclerAdapter;
import co.yishun.onemoment.app.ui.adapter.PersonalWorldAdapter;
import co.yishun.onemoment.app.ui.adapter.WorldBannerHeaderProvider;

/**
 * Created by Jinge on 2016/1/20.
 */
@EBean
public class PersonalWorldController implements SwipeRefreshLayout.OnRefreshListener, OnMoreListener {
    private static final String TAG = "PersonalWorldController";
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private PersonalWorldAdapter mAdapter;
    private HeaderCompatibleSuperRecyclerView mRecyclerView;
    private BannerHeaderProvider mBannerHeaderProvider;

    public PersonalWorldController() {

    }

    public void setUp(Context context, HeaderCompatibleSuperRecyclerView recyclerView, PersonalWorldAdapter.OnItemClickListener<WorldTag> listener) {
        mRecyclerView = recyclerView;
        mAdapter = new PersonalWorldAdapter(context, listener);
        RecyclerView.Adapter trueAdapter = mAdapter;

        GridLayoutManager manager = new GridLayoutManager(context, 2);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setRefreshListener(this);
        mRecyclerView.setOnMoreListener(this);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int position) {
                return position == 0 ? manager.getSpanCount() : 1;
            }
        });

        mBannerHeaderProvider = new WorldBannerHeaderProvider(context);
        trueAdapter = new HeaderRecyclerAdapter(trueAdapter, mBannerHeaderProvider);
        loadBanners();
        mRecyclerView.setAdapter(trueAdapter);
        loadTags();
    }

    public PersonalWorldAdapter getAdapter() {
        return mAdapter;
    }

    public SuperRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Background void loadBanners() {
        ListWithError<Banner> banners = mWorld.getBanners(null);
        if (banners.isSuccess()) {
            onLoadBanners(banners);
        } else {
            LogUtil.i(TAG, "load banner error");
        }
    }

    @UiThread void onLoadBanners(List<Banner> banners) {
        mBannerHeaderProvider.setupBanners(banners);
    }

    @Background void loadTags() {
        synchronizedLoadTags();
    }

    synchronized void synchronizedLoadTags() {
        ListWithError<WorldTag> list = mWorld.getWorldTagList(5, "", "recommend");
        if (list.isSuccess()) {
            onLoadTags(list);
        } else {
            onLoadError();
        }
    }

    @UiThread void onLoadError() {
        Snackbar.make(mRecyclerView, R.string.text_load_error, Snackbar.LENGTH_LONG).show();
        mRecyclerView.loadEnd();
        mRecyclerView.getSwipeToRefresh().setRefreshing(false);
    }


    @UiThread void onLoadTags(List<WorldTag> list) {
        mAdapter.addAll(list);
        mRecyclerView.loadEnd();
        mRecyclerView.getSwipeToRefresh().setRefreshing(false);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        mAdapter.clear();
        loadTags();
    }

    @Override
    public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
        LogUtil.i(TAG, "start load more, int numberOfItems, int numberBeforeMore, int currentItemPos: " + numberOfItems + ", " + numberBeforeMore + ", " + currentItemPos);
        loadTags();
    }
}
