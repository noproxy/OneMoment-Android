package co.yishun.onemoment.app.ui.home;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.modelv4.World;
import co.yishun.onemoment.app.ui.WorldVideosActivity_;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;
import co.yishun.onemoment.app.ui.controller.DiscoveryController_;

/**
 * Created by yyz on 7/20/15.
 */
@EFragment(R.layout.fragment_discovery)
public class DiscoveryFragment extends ToolbarFragment implements AbstractRecyclerViewAdapter.OnItemClickListener<World> {

    private static final String TAG = "DiscoveryFragment";
    @ViewById HeaderCompatibleSuperRecyclerView recyclerView;

    @AfterViews void setupViews() {
        DiscoveryController_.getInstance_(getContext()).setUp(getContext(), recyclerView, this);
    }

    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_explore_title;
    }

    @Override public void onClick(View view, World item) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        LogUtil.d(TAG, rect.toString());
        WorldVideosActivity_.intent(this).world(item).forWorld(false)
                .imageRect(rect).imageCorner(12).flags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .start();
    }

    @Override
    public void setPageInfo() {
        mPageName = "DiscoveryFragment";
    }
}
