package co.yishun.onemoment.app.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.BannerHeaderProvider;
import co.yishun.onemoment.app.ui.adapter.HeaderRecyclerAdapter;
import co.yishun.onemoment.app.ui.adapter.WorldAdapter;
import co.yishun.onemoment.app.ui.common.TabPagerFragment;

/**
 * Created by yyz on 7/13/15.
 */
@EFragment
public class WorldFragment extends TabPagerFragment implements AbstractRecyclerViewAdapter.OnItemClickListener<WorldTag> {

    private World mWorld = OneMomentV3.createAdapter().create(World.class);

    @Override
    protected int getTitleDrawableRes() {
        return R.drawable.pic_world_title;
    }

    @Override
    protected int getTabTitleArrayResources() {
        return R.array.world_page_title;
    }

    @NonNull
    @Override
    protected View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        HeaderCompatibleSuperRecyclerView recyclerView = (HeaderCompatibleSuperRecyclerView) inflater.inflate(R.layout.page_world, container, false);
        PagerController controller = WorldFragment_.PagerController_.getInstance_(inflater.getContext());
        controller.setUp(inflater.getContext(), recyclerView, position == 0, mWorld, this);
        container.addView(recyclerView);
        return recyclerView;
    }


    @Override
    protected int getContentViewId(Bundle savedInstanceState) {
        return R.layout.fragment_world;
    }

    @Override
    public void onClick(View view, WorldTag item) {

    }

    @EBean
    public static class PagerController implements SwipeRefreshLayout.OnRefreshListener, OnMoreListener {
        private static final String TAG = "PagerController";
        private WorldAdapter mAdapter;
        private HeaderCompatibleSuperRecyclerView mRecyclerView;
        private boolean isRecommend;
        private World mWorld;
        private BannerHeaderProvider mBannerHeaderProvider;
        private String ranking = "";

        public PagerController() {
        }

        public void setUp(Context context, HeaderCompatibleSuperRecyclerView mRecyclerView, boolean recommend, World world, WorldAdapter.OnItemClickListener<WorldTag> listener) {
            this.mRecyclerView = mRecyclerView;
            isRecommend = recommend;
            mWorld = world;
            this.mAdapter = new WorldAdapter(context, listener);
            RecyclerView.Adapter trueAdapter = mAdapter;


            LinearLayoutManager manager = new LinearLayoutManager(context);
            manager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(manager);
            mRecyclerView.setRefreshListener(this);
            mRecyclerView.setOnMoreListener(this);

            if (recommend) {
                mBannerHeaderProvider = new BannerHeaderProvider(context);
                trueAdapter = new HeaderRecyclerAdapter(trueAdapter, mBannerHeaderProvider);
                loadBanners();
            }
            mRecyclerView.setAdapter(trueAdapter);

            loadTags();
        }

        public WorldAdapter getAdapter() {
            return mAdapter;
        }

        public SuperRecyclerView getRecyclerView() {
            return mRecyclerView;
        }

        @Background
        void loadBanners() {
            List<Banner> banners = mWorld.getBanners(3);
            if (banners.size() == 0) {
                //TODO loading error
                return;
            }
            onLoadBanners(banners);
        }

        @UiThread
        void onLoadBanners(List<Banner> banners) {
            mBannerHeaderProvider.setupBanners(banners);
        }

        @Background
        void loadTags() {
            synchronizedLoadTags();
        }

        synchronized void synchronizedLoadTags() {
            List<WorldTag> list = mWorld.getWorldTagList(5, ranking, isRecommend ? World.TAG_SORT_TYPE_RECOMMEND : World.TAG_SORT_TYPE_TIME);
            if (list.size() == 0) {
                //TODO loading error
                return;
            }
            ranking = list.get(list.size() - 1).ranking;
            onLoadTags(list);
        }


        @UiThread
        void onLoadTags(List<WorldTag> list) {
            mAdapter.addAll(list);
            mRecyclerView.loadEnd();
            mRecyclerView.getSwipeToRefresh().setRefreshing(false);
        }

        @Override
        public void onRefresh() {
            mAdapter.clear();
            ranking = "";
            loadTags();
        }

        @Override
        public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
            Log.i(TAG, "start load more, int numberOfItems, int numberBeforeMore, int currentItemPos: " + numberOfItems + ", " + numberBeforeMore + ", " + currentItemPos);
            loadTags();
        }
    }
}