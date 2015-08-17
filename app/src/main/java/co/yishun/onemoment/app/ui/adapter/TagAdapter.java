package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.TagVideo;

/**
 * Created by Carlos on 2015/8/17.
 */
public class TagAdapter extends AbstractRecyclerViewAdapter<TagVideo, TagAdapter.SimpleViewHolder> {


    public TagAdapter(Context context, OnItemClickListener<TagVideo> listener) {
        super(context, listener);
    }

    @Override
    public void onBindViewHolder(TagAdapter.SimpleViewHolder holder, TagVideo item, int position) {
        Picasso.with(mContext).load(R.drawable.pic_slider_loading).fit().into(holder.itemImageView);
    }

    @Override
    public TagAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
