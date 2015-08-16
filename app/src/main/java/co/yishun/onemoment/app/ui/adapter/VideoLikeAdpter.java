package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.Video;

/**
 * Created by Carlos on 2015/8/16.
 */
public class VideoLikeAdpter extends AbstractRecyclerViewAdapter<Video, VideoLikeAdpter.SimpleViewHolder> {

    public VideoLikeAdpter(Context context, OnItemClickListener<Video> listener) {
        super(context, listener);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Video video = mItems.get(position);
        Picasso.with(mContext).load(video.domain.domain + video.fileName).into(holder.itemImageView);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_video_like_item, parent, false));
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImageView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
        }
    }
}
