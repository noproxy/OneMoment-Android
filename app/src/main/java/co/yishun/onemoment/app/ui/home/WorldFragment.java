package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.TagActivity;
import co.yishun.onemoment.app.ui.TagActivity_;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.PersonalWorldAdapter;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;
import co.yishun.onemoment.app.ui.controller.PersonalWorldController_;

/**
 * Created by yyz on 7/13/15.
 */
@EFragment
public class WorldFragment extends ToolbarFragment implements AbstractRecyclerViewAdapter.OnItemClickListener<WorldTag> {

    @ViewById HeaderCompatibleSuperRecyclerView recyclerView;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_world, container, false);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        return rootView;
    }

    @Override
    protected int getTitleDrawableRes() {
        return R.drawable.pic_world_title;
    }

    @AfterViews void setUpViews() {
        PersonalWorldController_.getInstance_(this.getActivity()).setUp(this.getActivity(), recyclerView, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_world, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_world_action_add:
                //TODO add tag
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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