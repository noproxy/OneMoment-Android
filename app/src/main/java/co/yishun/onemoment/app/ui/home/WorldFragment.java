package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;

import org.androidannotations.annotations.EFragment;

import java.util.List;

import co.yishun.library.datacenter.DataCenter;
import co.yishun.library.datacenter.LoadIndexProvider;
import co.yishun.library.datacenter.SuperRecyclerViewLoadMore;
import co.yishun.library.datacenter.SuperRecyclerViewRefreshable;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.SearchActivity_;
import co.yishun.onemoment.app.ui.TagActivity;
import co.yishun.onemoment.app.ui.TagActivity_;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.BannerHeaderProvider;
import co.yishun.onemoment.app.ui.adapter.DataCenterWorldAdapter;
import co.yishun.onemoment.app.ui.common.TabPagerFragment;
import co.yishun.onemoment.app.ui.controller.WorldPagerController;
import co.yishun.onemoment.app.ui.controller.WorldPagerController_;

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

    @Override
    public void onPause() {
        super.onPause();
        BannerHeaderProvider.stopSliderAutoCycle();
    }

    @Override
    public void onResume() {
        super.onResume();
        BannerHeaderProvider.startSliderAutoCycle();
    }

    @NonNull
    @Override
    protected View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        HeaderCompatibleSuperRecyclerView recyclerView = (HeaderCompatibleSuperRecyclerView) inflater.inflate(R.layout.page_world, container, false);
        if (position == 0) {
            WorldPagerController controller = WorldPagerController_.getInstance_(inflater.getContext());
            controller.setUp(inflater.getContext(), recyclerView, position == 0, mWorld, this);
//        container.addView(recyclerView); why work properly before I comment this line?
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

            DataCenterWorldAdapter adapter = new DataCenterWorldAdapter(getActivity(), this);
            adapter.setLoader(new WorldTagLoader(false));
            adapter.setLoadIndexProvider(new LoadIndexProvider<RankingIndex, WorldTag>() {
                @Override
                public RankingIndex initInstance() {
                    return new RankingIndex(null);
                }

                @Override
                public RankingIndex resetIndex() {
                    return new RankingIndex("");
                }
            });
            adapter.setLoadMore(new SuperRecyclerViewLoadMore(recyclerView));
            adapter.setRefreshable(new SuperRecyclerViewRefreshable(recyclerView));

            adapter.setOnEndListener(new DataCenter.OnEndListener<RankingIndex, WorldTag>() {
                @Override
                public void onFail(RankingIndex index) {
                    if (index.getRanking() == null) {
                        Toast.makeText(getContext(), "Refresh failed", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Load failed", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onSuccess(RankingIndex index) {

                }
            });
            recyclerView.setAdapter(adapter);

            adapter.loadNext();
        }
        return recyclerView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_world, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_world_action_search:
                SearchActivity_.intent(this).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getContentViewId(Bundle savedInstanceState) {
        return R.layout.fragment_world;
    }

    @Override
    public void onClick(View view, WorldTag item) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        TagActivity_.intent(this).tag(item).top(location[1]).from(TagActivity.FROM_WORLD_FRAGMENT).isPrivate(false).start();
    }


    @Override
    public void setPageInfo() {
        mPageName = "WorldFragment";
    }

    public static class WorldTagLoader implements DataCenter.DataLoader<RankingIndex, WorldTag> {
        private static final World mCacheOnlyWorld = OneMomentV3.getCacheOnlyRetrofit().create(World.class);
        private static final World mNoCacheWorld = OneMomentV3.getNoCacheRetrofit().create(World.class);
        private final boolean isRecommend;

        public WorldTagLoader(boolean recommend) {
            isRecommend = recommend;
        }

        @Override
        public List<WorldTag> loadOptional(RankingIndex index) {
            return load(mCacheOnlyWorld, index.getRanking());
        }

        @Override
        public List<WorldTag> loadNecessary(RankingIndex index) {
            return null;
        }

        private List<WorldTag> load(World world, String ranking) {
            ListWithError<WorldTag> list = world.getWorldTagList(5, ranking, isRecommend ? "recommend" : "time");
            if (list.isSuccess()) {
                return list;
            } else
                return null;
        }
    }

    public static class RankingIndex implements LoadIndexProvider.LoadIndex<WorldTag> {
        private String ranking;

        public RankingIndex(String ranking) {
            this.ranking = ranking;
        }

        public String getRanking() {
            return ranking;
        }

        @Override
        public void increment(List<WorldTag> result) {
            ranking = getMinRanking(result);
        }

        private String getMinRanking(List<WorldTag> result) {
            int rankInt = Integer.MAX_VALUE;
            for (WorldTag worldTag : result) {
                int r = Integer.valueOf(worldTag.ranking);
                if (r < rankInt) {
                    rankInt = r;
                }
            }
            if (rankInt != Integer.MAX_VALUE)
                return String.valueOf(rankInt);
            else
                return null;
        }

        @Override
        public void reset() {
            ranking = null;
        }

        @Override
        public boolean equals(LoadIndexProvider.LoadIndex<WorldTag> another) {
            return another instanceof RankingIndex && (ranking != null && ranking.equals(((RankingIndex) another).ranking) || (ranking == null && ((RankingIndex) another).ranking == null));
        }

        @Override
        public LoadIndexProvider.LoadIndex<WorldTag> getCopy() {
            try {
                //noinspection unchecked
                return (LoadIndexProvider.LoadIndex<WorldTag>) clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}