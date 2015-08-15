package co.yishun.onemoment.app.ui.common;

import android.os.Bundle;
import android.support.annotation.ArrayRes;
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
public abstract class TabPagerFragment extends ToolbarFragment {

    private ViewPager mViewPager;

    protected abstract
    @ArrayRes
    int getTabTitleArrayResources();

    private PagerAdapter getViewPager(LayoutInflater inflater) {
        String titles[] = getResources().getStringArray(getTabTitleArrayResources());
        return new PagerAdapter() {
            @Override
            public int getCount() {
                return titles.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View rootView = onCreatePagerView(inflater, container, position);
                container.addView(rootView);
                return rootView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(((View) object));
            }
        };
    }

    protected abstract
    @NonNull
    View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position);

    protected abstract
    @LayoutRes
    int getContentViewId(Bundle savedInstanceState);

    @NonNull
    @CallSuper
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getContentViewId(savedInstanceState), container, false);
        assert rootView != null;

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        if (toolbar == null || viewPager == null || tabLayout == null)
            throw new AssertionError("You must ensure your layout contain TabLayout, ViewPager and Toolbar with R.id.tabLayout, R.id.viewPager, R.id.toolbar");
        PagerAdapter viewPagerAdapter = getViewPager(inflater);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }
}
