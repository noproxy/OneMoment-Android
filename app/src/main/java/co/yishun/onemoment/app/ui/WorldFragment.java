package co.yishun.onemoment.app.ui;

import android.content.Context;
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

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.squareup.picasso.Picasso;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/13/15.
 */
public final class WorldFragment extends TabPagerFragment {

    private static final int res[] = Test.res;

    public WorldFragment() {
    }

    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_world_title;
    }

    @Override int getTabTitleArrayResources() {
        return R.array.world_page_title;
    }

    @NonNull @Override View onCreatePagerView(LayoutInflater inflater, ViewGroup container, int position) {
        boolean isRecommend = position == 0;
        View rootView = inflater.inflate(R.layout.page_world, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(inflater.getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        RecyclerView.Adapter adapter = new WorldAdapter(inflater.getContext(), res, isRecommend);

        recyclerView.setAdapter(adapter);
        container.addView(rootView);
        return rootView;
    }

    @Override int getContentViewId(Bundle savedInstanceState) {
        return R.layout.fragment_world;
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImageView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
        }

    }

    public static class WorldAdapter extends RecyclerView.Adapter<SimpleViewHolder> implements View.OnClickListener {
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

        private BaseSliderView generateSimpleSliderView(Context context, @DrawableRes int imageRes) {
            return new BaseSliderView(context.getApplicationContext()) {
                @Override public View getView() {
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
                @Override public View getView() {
                    ImageView imageView = new ImageView(context);
                    //TODO load image from url
                    return imageView;
                }
            };
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
                Picasso.with(context).load(res[((int) getItemId(position))]).into(holder.itemImageView);
            }
        }

        @Override public void onViewRecycled(SimpleViewHolder holder) {
            super.onViewRecycled(holder);
            Picasso.with(context).cancelRequest(holder.itemImageView);
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