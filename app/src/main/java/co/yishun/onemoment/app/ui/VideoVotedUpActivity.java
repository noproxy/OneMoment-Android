package co.yishun.onemoment.app.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.ppamorim.dragger.DraggerActivity;
import com.github.ppamorim.dragger.DraggerView;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.SdkVersionHelper;
import org.lucasr.twowayview.ItemClickSupport;
import org.lucasr.twowayview.TwoWayLayoutManager;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
import org.lucasr.twowayview.widget.TwoWayView;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/20/15.
 */
@EActivity(R.layout.activity_video_voted_up)
public class VideoVotedUpActivity extends DraggerActivity implements ItemClickSupport.OnItemClickListener, ItemClickSupport.OnItemLongClickListener {
    @ViewById Toolbar toolbar;
    @ViewById TwoWayView twoWayView;
    @ViewById DraggerView draggerView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
    }

    @AfterViews void setupToolbar() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.discovery_video_voted_up);
        Log.i("setupToolbar", "set home as up true");
    }

    @Override protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @SuppressLint("NewApi") @AfterViews void setupTwoWayView() {
        twoWayView.setHasFixedSize(true);
        twoWayView.setLongClickable(true);// long click to edit

        final ItemClickSupport itemClick = ItemClickSupport.addTo(twoWayView);
        itemClick.setOnItemClickListener(this::onItemClick);
        itemClick.setOnItemLongClickListener(this::onItemLongClick);

        final Drawable divider;
        if (SdkVersionHelper.getSdkInt() > 21) {
            divider = getResources().getDrawable(R.drawable.divider, null);
        } else {
            //noinspection deprecation
            divider = getResources().getDrawable(R.drawable.divider);
        }
        twoWayView.addItemDecoration(new DividerItemDecoration(divider));

        twoWayView.setAdapter(new VideoLikeAdapter(this, twoWayView));
    }

    @Override public void onItemClick(RecyclerView recyclerView, View view, int i, long l) {

    }

    @Override public boolean onItemLongClick(RecyclerView recyclerView, View view, int i, long l) {
        return false;
    }

    private static class VideoLikeAdapter extends RecyclerView.Adapter<VideoVH> {
        private final Context context;
        private final LayoutInflater inflater;
        private final int[] res = WorldViewPagerAdapter.res;
        private final TwoWayView twoWayView;

        public VideoLikeAdapter(Context context, TwoWayView twoWayView) {
            this.context = context.getApplicationContext();
            this.inflater = LayoutInflater.from(this.context);
            this.twoWayView = twoWayView;
        }

        @Override public VideoVH onCreateViewHolder(ViewGroup parent, int viewType) {
            VideoVH vh = new VideoVH(inflater.inflate(R.layout.layout_video_like_item, parent, false));
            return vh;
        }

        @Override public void onBindViewHolder(VideoVH holder, int position) {
            final View itemView = holder.itemView;
            Picasso.with(context).load(res[position]).into(holder.itemImageView);
            boolean isVertical = (twoWayView.getOrientation() == TwoWayLayoutManager.Orientation.VERTICAL);

            final SpannableGridLayoutManager.LayoutParams lp = (SpannableGridLayoutManager.LayoutParams) itemView.getLayoutParams();

            final int span1 = (position == 0 || position == 3 ? 2 : 1);
            final int span2 = (position == 0 ? 2 : (position == 3 ? 3 : 1));

            final int colSpan = (isVertical ? span2 : span1);
            final int rowSpan = (isVertical ? span1 : span2);


            if (lp.rowSpan != rowSpan || lp.colSpan != colSpan) {
                lp.rowSpan = rowSpan;
                lp.colSpan = colSpan;
                itemView.setLayoutParams(lp);
            }

        }

        @Override public int getItemCount() {
            return res.length;
        }
    }

    private static class VideoVH extends RecyclerView.ViewHolder {
        final ImageView itemImageView;

        public VideoVH(View itemView) {
            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
        }
    }
}

