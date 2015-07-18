package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.design.widget.Snackbar;
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

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.squareup.picasso.Picasso;

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
//        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinatorLayout);
//        AppBarLayout appBar = (AppBarLayout) rootView.findViewById(R.id.appBar);
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
    public static final int res[] = new int[]{
            R.drawable.pic_world_item_test_0,
            R.drawable.pic_world_item_test_1,
            R.drawable.pic_world_item_test_2,
            R.drawable.pic_world_item_test_3,
            R.drawable.pic_world_item_test_4,
            R.drawable.pic_world_item_test_5,
            R.drawable.pic_world_item_test_6,
            R.drawable.pic_world_item_test_7,
            R.drawable.pic_world_item_test_8,
            R.drawable.pic_world_item_test_9,
            R.drawable.pic_world_item_test_10,
            R.drawable.pic_world_item_test_11,
            R.drawable.pic_world_item_test_12,
            R.drawable.pic_world_item_test_13,
            R.drawable.pic_world_item_test_14,
            R.drawable.pic_world_item_test_15,
            R.drawable.pic_world_item_test_16,
            R.drawable.pic_world_item_test_17,
            R.drawable.pic_world_item_test_18,
            R.drawable.pic_world_item_test_19,
            R.drawable.pic_world_item_test_20,
            R.drawable.pic_world_item_test_21,
            R.drawable.pic_world_item_test_22,
            R.drawable.pic_world_item_test_23,
            R.drawable.pic_world_item_test_24,
            R.drawable.pic_world_item_test_25,
            R.drawable.pic_world_item_test_26,
            R.drawable.pic_world_item_test_27,
            R.drawable.pic_world_item_test_28,
            R.drawable.pic_world_item_test_29,
            R.drawable.pic_world_item_test_30,
            R.drawable.pic_world_item_test_31,
            R.drawable.pic_world_item_test_32
    };
    private final int TITLE_RES[] = new int[]{
            R.string.world_page_recommend_title,
            R.string.world_page_latest_title
    };
    private final LayoutInflater inflater;
    private final Context context;


    public WorldViewPagerAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
        this.context = inflater.getContext().getApplicationContext();
    }

    public static BaseSliderView generateSimpleSliderView(Context context, @DrawableRes int
            imageRes) {
        return new BaseSliderView(context.getApplicationContext()) {
            @Override public View getView() {
                ImageView imageView = (ImageView) View.inflate(context, R.layout.layout_slider_image, null);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                imageView.setImageResource(imageRes);
                Picasso.with(context).load(imageRes).into(imageView);
                imageView.setOnClickListener(v -> Snackbar.make(MainActivity.withView(v), "slider " +
                        "clicked!", Snackbar.LENGTH_SHORT).show());
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

        RecyclerView.Adapter adapter = new WorldAdapter(context, res, isRecommend);

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

        public ImageView itemImageView() {
            return (ImageView) itemView.findViewById(R.id.itemImageView);
        }

    }

    static class WorldAdapter extends RecyclerView.Adapter<SimpleViewHolder> implements
            View
                    .OnClickListener {
        private final static int TYPE_HEADER = -1;
        private final static int TYPE_ITEM = -2;
        private final Context context;
        private final int items[];
        private final boolean hasHeader;

        public WorldAdapter(Context context, int items[], boolean hasHeader) {
            super();
            this.context = context.getApplicationContext();
            this.items = items;
            this.hasHeader = hasHeader;
        }

        @Override public int getItemViewType(int position) {
            return position == 0 ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final SimpleViewHolder holder;
            if (hasHeader && viewType == TYPE_HEADER) {
                Log.i("recycler view", "create header");
                holder = new SimpleViewHolder(LayoutInflater.from(context).inflate(R.layout
                        .layout_world_header_slider, parent, false));
                SliderLayout worldSlider = (SliderLayout) holder.itemView.findViewById
                        (R.id.worldSlider);
                worldSlider.addSlider(generateSimpleSliderView(context, R.drawable.pic_slider_loading));
                worldSlider.addSlider(generateSimpleSliderView(context, R.drawable.pic_slider_header_test_0));
                worldSlider.addSlider(generateSimpleSliderView(context, R.drawable.pic_slider_header_test_1));
            } else {
                holder = new SimpleViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_world_item, parent, false));
                holder.itemView.setOnClickListener(this);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            if (!hasHeader || getItemViewType(position) != TYPE_HEADER) {
                Picasso.with(context).load(res[((int) getItemId(position))]).centerCrop().into
                        (holder.itemImageView());
            }
        }

        @Override public void onViewRecycled(SimpleViewHolder holder) {
            super.onViewRecycled(holder);
            Picasso.with(context).cancelRequest(holder.itemImageView());
        }

        @Override public long getItemId(int position) {
            if (hasHeader) return position - 1;
            else return position;
        }

        @Override public int getItemCount() {
            return items.length + (hasHeader ? 1 : 0);
        }

        @Override public void onClick(View v) {
            Snackbar.make(MainActivity.withView(v), "test", Snackbar.LENGTH_SHORT).show();
        }
    }
}