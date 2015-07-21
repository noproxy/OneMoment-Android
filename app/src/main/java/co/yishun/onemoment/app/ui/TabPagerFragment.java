package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/21/15.
 */
public abstract class TabPagerFragment extends BaseFragment {

    abstract @LayoutRes int getContentViewId(Bundle savedInstanceState);

    @NonNull abstract PagerAdapter getPagerAdapter(LayoutInflater inflater, Bundle savedInstanceState);

    @NonNull @CallSuper @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getContentViewId(savedInstanceState), container, false);
        assert rootView != null;

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        if (toolbar == null || viewPager == null || tabLayout == null)
            throw new AssertionError("You must ensure your layout contain TabLayout, ViewPager and Toolbar with R.id.tabLayout, R.id.viewPager, R.id.toolbar");

        PagerAdapter viewPagerAdapter = getPagerAdapter(inflater, savedInstanceState);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }
}
