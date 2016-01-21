package co.yishun.onemoment.app.ui.home;

import android.widget.FrameLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.CommonWebFragment;
import co.yishun.onemoment.app.ui.common.CommonWebFragment_;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;

/**
 * Created by yyz on 7/21/15.
 */
@EFragment(R.layout.fragment_me)
public class MeFragment extends ToolbarFragment {
    CommonWebFragment meWebFragment;
    @ViewById FrameLayout containerFrameLayout;

    @AfterViews void setUpViews() {
        meWebFragment = CommonWebFragment_.builder().build();
        getFragmentManager().beginTransaction().replace(R.id.containerFrameLayout, meWebFragment).commit();
    }


    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_me_title;
    }
    @Override
    public void setPageInfo() {
        mPageName = "MeFragment";
    }

}
