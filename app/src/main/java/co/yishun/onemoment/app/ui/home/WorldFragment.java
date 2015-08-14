package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.Background;
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

    BannerHeaderProvider mBannerHeaderProvider;
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private WorldAdapter mAdapters[] = new WorldAdapter[2];
    private View[] cacheViews = new View[2];

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
        if (cacheViews[position] != null) return cacheViews[position];
        View rootView = inflater.inflate(R.layout.page_world, container, false);
        cacheViews[position] = rootView;

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(inflater.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        if (position == 0) {
            mAdapters[0] = new WorldAdapter(this, inflater.getContext());
            mBannerHeaderProvider = new BannerHeaderProvider(inflater.getContext());
            recyclerView.setAdapter(new HeaderRecyclerAdapter(mAdapters[0], mBannerHeaderProvider));
            loadBanners();
        } else {
            mAdapters[1] = new WorldAdapter(this, inflater.getContext());
            recyclerView.setAdapter(mAdapters[1]);
        }
        loadTags(position);
        container.addView(rootView);
        return rootView;
    }

    @Background
    void loadTags(int position) {
        String domain = ApiUtil.getVideoResourceDomain();
        if (domain == null) {
            //TODO loading error
            return;
        }
        mAdapters[position].setDomain(domain);
        List<WorldTag> list = mWorld.getWorldTagList(5, null, position == 0 ? World.TAG_SORT_TYPE_RECOMMEND : World.TAG_SORT_TYPE_TIME);
        if (list.size() == 0) {
            //TODO loading error
            return;
        }
        onLoadTags(list, mAdapters[position]);
    }

    @UiThread
    void onLoadTags(List<WorldTag> list, WorldAdapter adapter) {
        adapter.addAll(list);
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

    @Override
    protected int getContentViewId(Bundle savedInstanceState) {
        return R.layout.fragment_world;
    }

    @Override
    public void onClick(WorldTag tag) {

    }
}