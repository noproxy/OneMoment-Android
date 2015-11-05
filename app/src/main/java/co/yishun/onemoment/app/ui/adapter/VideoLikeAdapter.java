package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.squareup.picasso.Picasso;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.Video;

/**
 * Created by Carlos on 2015/8/16.
 */
public class VideoLikeAdapter extends AbstractRecyclerViewAdapter<Video, VideoLikeAdapter.SimpleViewHolder> {
    private final SuperRecyclerView recyclerView;

    public VideoLikeAdapter(Context context, OnItemClickListener<Video> listener, SuperRecyclerView recyclerView) {
        super(context, listener);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, Video item, int position) {
        //TODO there is a video
        Picasso.with(mContext).load(R.drawable.pic_slider_loading).fit().into(holder.itemImageView);
    }


    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_video_like, parent, false));
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImageView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
        }
    }
}
