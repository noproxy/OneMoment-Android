package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.modelv4.TodayWorld;

/**
 * Created by Jinge on 2016/1/20.
 */
public class DiscoveryAdapter extends AbstractRecyclerViewAdapter<TodayWorld, DiscoveryAdapter.SimpleViewHolder> {
    private final String PeopleSuffix;

    public DiscoveryAdapter(Context context, OnItemClickListener<TodayWorld> listener) {
        super(context, listener);
        PeopleSuffix = context.getString(R.string.fragment_world_suffix_people_count);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_world_item, parent, false));
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, TodayWorld item, int position) {
        if (TextUtils.isEmpty(item.thumbnail))
            holder.itemImageView.setImageResource(R.drawable.pic_slider_loading);
        else
            Picasso.with(mContext).load(item.thumbnail).placeholder(R.drawable.pic_slider_loading).into(holder.itemImageView);
        holder.numTextView.setText(String.format(PeopleSuffix, item.videoNum));
        holder.tagTextView.setText(item.name);
        holder.tagTextView.setCompoundDrawablesWithIntrinsicBounds(
                mContext.getResources().getDrawable(R.drawable.ic_world_tag_time), null, null, null);
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImageView;
        final TextView numTextView;
        final TextView tagTextView;
        final TextView likeTextView;

        public SimpleViewHolder(View itemView) {
            super(itemView);
//            ((CardView)itemView).setPreventCornerOverlap(false);
            itemImageView = (ImageView) itemView.findViewById(R.id.itemImageView);
            numTextView = (TextView) itemView.findViewById(R.id.numTextView);
            tagTextView = (TextView) itemView.findViewById(R.id.tagTextView);
            likeTextView = (TextView) itemView.findViewById(R.id.likeTextView);
        }

    }
}
