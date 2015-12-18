package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;

import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.SearchActivity_;
import co.yishun.onemoment.app.ui.TagActivity;
import co.yishun.onemoment.app.ui.TagActivity_;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
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

    @NonNull
    @Override
    protected View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        HeaderCompatibleSuperRecyclerView recyclerView = (HeaderCompatibleSuperRecyclerView) inflater.inflate(R.layout.page_world, container, false);
        WorldPagerController controller = WorldPagerController_.getInstance_(inflater.getContext());
        controller.setUp(inflater.getContext(), recyclerView, position == 0, mWorld, this);
        container.addView(recyclerView);
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
}