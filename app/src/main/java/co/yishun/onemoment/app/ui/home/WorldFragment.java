package co.yishun.onemoment.app.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.ApiUtil;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.BannerHeaderProvider;
import co.yishun.onemoment.app.ui.adapter.HeaderRecyclerAdapter;
import co.yishun.onemoment.app.ui.adapter.WorldAdapter;
import co.yishun.onemoment.app.ui.common.TabPagerFragment;

/**
 * Created by yyz on 7/13/15.
 */
@EFragment
public class WorldFragment extends TabPagerFragment implements WorldAdapter.OnTagClickListener {

    private World mWorld = OneMomentV3.createAdapter().create(World.class);
//    private PagerController[] mControllers = new PagerController[2];

    public WorldFragment() {
    }

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
//        if (mControllers[position] != null) {
//            View view = mControllers[position].getRecyclerView();
//            ((ViewGroup) view.getParent()).removeView(view);
//            container.addView(view);
//            return view;
//        }
        SuperRecyclerView recyclerView = (SuperRecyclerView) inflater.inflate(R.layout.page_world, container, false);
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
    public void onClick(WorldTag tag) {

    }

    @EBean
    public static class PagerController implements SwipeRefreshLayout.OnRefreshListener, OnMoreListener {
        private WorldAdapter mAdapter;
        private SuperRecyclerView mRecyclerView;
        private boolean isRecommend;
        private WorldAdapter.OnTagClickListener mOnTagClickListener;
        private World mWorld;
        private BannerHeaderProvider mBannerHeaderProvider;
        private String ranking = "";

        public PagerController() {
        }

        public void setUp(Context context, SuperRecyclerView mRecyclerView, boolean recommend, World world, WorldAdapter.OnTagClickListener listener) {
            this.mRecyclerView = mRecyclerView;
            isRecommend = recommend;
            mOnTagClickListener = listener;
            mWorld = world;
            WorldAdapter adapter;


            LinearLayoutManager manager = new LinearLayoutManager(context);
            manager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(manager);
            mRecyclerView.setRefreshListener(this);
            mRecyclerView.setOnMoreListener(this);

            if (recommend) {
                adapter = new WorldAdapter(mOnTagClickListener, context);
                mBannerHeaderProvider = new BannerHeaderProvider(context);
                mRecyclerView.setAdapter(new HeaderRecyclerAdapter(adapter, mBannerHeaderProvider));
                loadBanners();
            } else {
                adapter = new WorldAdapter(mOnTagClickListener, context);
                mRecyclerView.setAdapter(adapter);
            }


            this.mAdapter = adapter;


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
            String domain = ApiUtil.getVideoResourceDomain();
            if (domain == null) {
                //TODO loading error
                return;
            }
            mAdapter.setDomain(domain);
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
        }

        @Override
        public void onRefresh() {
            mAdapter.clear();
            ranking = "";
            loadTags();
        }

        @Override
        public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
            loadTags();
        }
    }
}