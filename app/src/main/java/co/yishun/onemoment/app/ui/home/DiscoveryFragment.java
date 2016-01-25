package co.yishun.onemoment.app.ui.home;

import android.view.View;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.modelv4.TodayWorld;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;
import co.yishun.onemoment.app.ui.controller.DiscoveryController_;

/**
 * Created by yyz on 7/20/15.
 */
@EFragment(R.layout.fragment_discovery)
public class DiscoveryFragment extends ToolbarFragment implements AbstractRecyclerViewAdapter.OnItemClickListener<TodayWorld> {

    private static final String TAG = "DiscoveryFragment";
    @ViewById HeaderCompatibleSuperRecyclerView recyclerView;

    @AfterViews void setupViews() {
        DiscoveryController_.getInstance_(getContext()).setUp(getContext(), recyclerView, this);
    }

    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_explore_title;
    }

    @Override public void onClick(View view, TodayWorld item) {
        LogUtil.d(TAG, "item click : " + item.name);
//        int[] location = new int[2];
//        view.getLocationOnScreen(location);
//        TagActivity_.intent(this).tag(item).top(location[1]).from(TagActivity.FROM_WORLD_FRAGMENT).isPrivate(false).start();
    }

    @Override
    public void setPageInfo() {
        mPageName = "DiscoveryFragment";
    }
}
