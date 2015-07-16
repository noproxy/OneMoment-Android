package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;

import java.util.ArrayList;
import java.util.List;

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

        WorldViewPagerAdapter viewPagerAdapter = new WorldViewPagerAdapter(inflater);
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
    private final int TITLE_RES[] = new int[]{
            R.string.world_page_recommend_title,
            R.string.world_page_latest_title
    };
    private final LayoutInflater inflater;
    private final Context context;

    public WorldViewPagerAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
        this.context = inflater.getContext();
    }

    public static BaseSliderView generateSimpleSliderView(Context context, @DrawableRes int
            imageRes) {
        return new BaseSliderView(context) {
            @Override public View getView() {
                ImageView imageView = (ImageView) View.inflate(context, R.layout.layout_slider_image, null);
                imageView.setImageResource(imageRes);
                return imageView;
            }
        };
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
        boolean isRecommend = position == 0;
        View rootView = inflater.inflate(R.layout.page_world, container,
                false);


        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        final List<String> content = new ArrayList<>();
        for (int i = 0; i < 30; i++)
            content.add("text" + i);
        RecyclerView.Adapter adapter = new WorldAdapter(context, content, isRecommend);

        recyclerView.setAdapter(adapter);
        container.addView(rootView);
        return rootView;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override public CharSequence getPageTitle(int position) {
        return context.getString(TITLE_RES[position]);
    }

    @Override public int getCount() {
        return 2;
    }

    @Override public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private BaseSliderView generateSimpleSliderView(String url) {
        return new BaseSliderView(context) {
            @Override public View getView() {
                ImageView imageView = new ImageView(context);
                //TODO load image from url
                return imageView;
            }
        };
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class WorldAdapter extends RecyclerView.Adapter<SimpleViewHolder> {
        private final int TYPE_HEADER = -1;
        private final int TYPE_ITEM = -2;
        private final Context context;
        private final List<String> items;
        private final boolean hasHeader;

        public WorldAdapter(Context context, List<String> items, boolean hasHeader) {
            super();
            this.context = context;
            this.items = items;
            this.hasHeader = hasHeader;
        }

        @Override public int getItemViewType(int position) {
            return position == 0 ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SimpleViewHolder(LayoutInflater.from(context).inflate(
                    hasHeader ?
                            (viewType == TYPE_HEADER ? R.layout.layout_world_header_slider : android.R.layout.simple_list_item_1)
                            : android.R.layout.simple_list_item_1,
                    parent, false));
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            if (hasHeader) {
                if (getItemViewType(position) == TYPE_HEADER) {
                    SliderLayout worldSlider = (SliderLayout) holder.itemView.findViewById
                            (R.id.worldSlider);
                    worldSlider.addSlider(generateSimpleSliderView(context, R.drawable.ic_diary));
                    worldSlider.addSlider(generateSimpleSliderView(context, R.drawable.ic_explore));
                    worldSlider.addSlider(generateSimpleSliderView(context, R.drawable.ic_me));
                } else {
                    ((TextView) holder.itemView).setText(items.get(position - 1));
                }
            } else {
                ((TextView) holder.itemView).setText(items.get(position));
            }


        }

        @Override public int getItemCount() {
            return items.size() + (hasHeader ? 1 : 0);
        }
    }
}