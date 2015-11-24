package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class TestFragment extends ToolbarFragment {


    public TestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        return rootView;
    }

    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_fab_menu_world;
    }

    @Override
    public void setPageInfo() {

    }
}
