package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.VideoVotedUpActivity_;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;

/**
 * Created by yyz on 7/20/15.
 */
@EFragment
public class DiscoveryFragment extends ToolbarFragment implements View.OnClickListener {

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        return rootView;
    }

    @Click({R.id.btnAlarmEveryday, R.id.btnVideoLike})
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnVideoLike:
                VideoVotedUpActivity_.intent(this).start();
                break;
        }
    }


    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_explore_title;
    }

    @Override
    public void setPageInfo() {
        mPageName = "DiscoveryFragment";
    }
}
