package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;

import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/21/15.
 */
@EFragment(R.layout.fragment_me)
public class MeFragment extends TabPagerFragment {
    @Override protected int getTitle() {
        return R.string.me_title;
    }


    @Override int getContentViewId(Bundle savedInstanceState) {
        return R.layout.fragment_me;
    }

    @NonNull @Override PagerAdapter getPagerAdapter(LayoutInflater inflater, Bundle savedInstanceState) {
        return null;
    }
}
