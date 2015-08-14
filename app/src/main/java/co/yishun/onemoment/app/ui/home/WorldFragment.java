package co.yishun.onemoment.app.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.ApiUtil;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.MainActivity;
import co.yishun.onemoment.app.ui.common.TabPagerFragment;

/**
 * Created by yyz on 7/13/15.
 */
@EFragment
public class WorldFragment extends TabPagerFragment {

    private World mWorld = OneMomentV3.createAdapter().create(World.class);

    public WorldFragment() {
    }

    @Override
    protected int getTitleDrawableRes() {
        return R.drawable.pic_world_title;
    }

    @Override
    protected int getTabTitleArrayResources() {
        return R.array.world_page_title;
    }

    @NonNull
    @Override
    protected View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        boolean isRecommend = position == 0;
        View rootView = inflater.inflate(R.layout.page_world, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(inflater.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        loadAdapter(recyclerView, inflater, isRecommend);

        container.addView(rootView);
        return rootView;
    }

    @Background
    void loadAdapter(RecyclerView recyclerView, LayoutInflater inflater, boolean isRecommend) {
        String domain = ApiUtil.getVideoResourceDomain();
        if (domain == null) {
            //TODO loading error
            return;
        }
        List<WorldTag> list = mWorld.getWorldTagList(5, null, isRecommend ? World.TAG_SORT_TYPE_RECOMMEND : World.TAG_SORT_TYPE_TIME);
        if (list.size() == 0) {
            //TODO loading error
            return;
        }
        setAdapter(recyclerView, inflater, isRecommend, domain, list);
    }

    @UiThread
    void setAdapter(RecyclerView recyclerView, LayoutInflater inflater, boolean isRecommend, String domain, List<WorldTag> list) {
        RecyclerView.Adapter adapter = new WorldAdapter(inflater.getContext(), domain, list, isRecommend);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected int getContentViewId(Bundle savedInstanceState) {
        return R.layout.fragment_world;
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImageView;
        final TextView numTextView;
        final TextView tagTextView;
        final TextView likeTextView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
            numTextView = (TextView) itemView.findViewById(R.id.numTextView);
            tagTextView = (TextView) itemView.findViewById(R.id.tagTextView);
            likeTextView = (TextView) itemView.findViewById(R.id.likeTextView);
        }

    }

    public static class WorldAdapter extends RecyclerView.Adapter<SimpleViewHolder> implements View.OnClickListener {
        private final static int TYPE_HEADER = -1;
        private final static int TYPE_ITEM = -2;
        private final Context context;
        private final List<WorldTag> items;
        private final boolean hasHeader;
        private final String domain;
        private final Drawable[] mTagDrawable;
        private final String PeopleSuffix;

        public WorldAdapter(Context context, String domain, List<WorldTag> items, boolean hasHeader) {
            super();
            this.context = context.getApplicationContext();
            this.items = items;
            this.hasHeader = hasHeader;
            this.domain = domain;
            Resources resource = context.getResources();
            mTagDrawable = new Drawable[]{
                    resource.getDrawable(R.drawable.ic_world_tag_time),
                    resource.getDrawable(R.drawable.ic_world_tag_location),
                    resource.getDrawable(R.drawable.ic_world_tag_msg)
            };
            PeopleSuffix = " " + context.getString(R.string.fragment_world_suffix_people_count);
        }

        private Drawable getDrawableByType(String type) {
            switch (type) {
                case "time":
                    return mTagDrawable[0];
                case "location":
                    return mTagDrawable[1];
                default:
                    return mTagDrawable[2];
            }
        }

        private BaseSliderView generateSimpleSliderView(Context context, @DrawableRes int imageRes) {
            return new BaseSliderView(context.getApplicationContext()) {
                @Override
                public View getView() {
                    ImageView imageView = (ImageView) View.inflate(context, R.layout.layout_slider_image, null);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Picasso.with(context).load(imageRes).into(imageView);
                    imageView.setOnClickListener(v -> Snackbar.make(MainActivity.withView(v), "slider clicked!", Snackbar.LENGTH_SHORT).show());
                    return imageView;
                }
            };
        }

        private BaseSliderView generateSimpleSliderView(String url) {
            return new BaseSliderView(context) {
                @Override
                public View getView() {
                    ImageView imageView = new ImageView(context);
                    //TODO load image from url
                    return imageView;
                }
            };
        }

        @Override
        public int getItemViewType(int position) {
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
                WorldTag tag = items.get((int) getItemId(position));
                Picasso.with(context).load(domain + tag.thumbnail).into(holder.itemImageView);
                holder.numTextView.setText(String.valueOf(tag.videosCount) + PeopleSuffix);
                holder.tagTextView.setText(tag.name);
                holder.likeTextView.setText(String.valueOf(tag.likeCount));
                holder.tagTextView.setCompoundDrawablesWithIntrinsicBounds(getDrawableByType(tag.type), null, null, null);
            }
        }

        @Override
        public void onViewRecycled(SimpleViewHolder holder) {
            super.onViewRecycled(holder);
            Picasso.with(context).cancelRequest(holder.itemImageView);
        }

        @Override
        public long getItemId(int position) {
            if (hasHeader) return position - 1;
            else return position;
        }

        @Override
        public int getItemCount() {
            return items.size() + (hasHeader ? 1 : 0);
        }

        @Override
        public void onClick(View v) {
            Snackbar.make(MainActivity.withView(v), "test", Snackbar.LENGTH_SHORT).show();
        }
    }
}