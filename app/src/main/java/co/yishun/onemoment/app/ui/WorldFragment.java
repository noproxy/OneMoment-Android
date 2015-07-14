package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/13/15.
 */
public final class WorldFragment extends BaseFragment {

    Toolbar toolbar;

    public WorldFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_world, container, false);
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);
        AppBarLayout appBar = (AppBarLayout) rootView.findViewById(R.id.appBar);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        WorldViewPagerAdapter viewPagerAdapter = new WorldViewPagerAdapter(inflater.getContext());
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        return rootView;
    }

    protected void setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);

        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.world_title);
        Log.i("setupToolbar", "set home as up true");
    }

    @Override public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        setupToolbar(activity, toolbar);
        activity.syncToggle();
    }
}


class WorldViewPagerAdapter extends PagerAdapter {
    private final Context context;

    public WorldViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
        boolean isRecommend = position == 0;
        View rootView = LayoutInflater.from(context).inflate(R.layout.page_world, container,
                false);
        View worldSlider = rootView.findViewById(R.id.worldSlider);


        if (isRecommend) {
            worldSlider.setVisibility(View.VISIBLE);
        } else {
            worldSlider.setVisibility(View.INVISIBLE);
        }
        return rootView;
    }

    @Override public int getCount() {
        return 2;
    }

    @Override public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}