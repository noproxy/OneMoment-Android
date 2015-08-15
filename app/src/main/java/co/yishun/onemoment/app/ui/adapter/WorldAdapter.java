package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;

/**
 * Created by Carlos on 2015/8/14.
 */
public class WorldAdapter extends RecyclerView.Adapter<WorldAdapter.SimpleViewHolder> {
    private final OnTagClickListener mListener;
    private final Context mContext;
    private final List<WorldTag> mItems = new ArrayList<>();
    private final String PeopleSuffix;
    private final Drawable[] mTagDrawable;
    private String mDomain;

    public WorldAdapter(OnTagClickListener listener, Context context) {
        this.mListener = listener;
        this.mContext = context;
        Resources resource = context.getResources();
        mTagDrawable = new Drawable[]{
                resource.getDrawable(R.drawable.ic_world_tag_time),
                resource.getDrawable(R.drawable.ic_world_tag_location),
                resource.getDrawable(R.drawable.ic_world_tag_msg)
        };
        PeopleSuffix = " " + context.getString(R.string.fragment_world_suffix_people_count);
    }

    public void setDomain(@NonNull String domain) {
        mDomain = domain;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_world_item, parent, false));
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        WorldTag tag = mItems.get(position);
        Picasso.with(mContext).load(mDomain + tag.thumbnail).into(holder.itemImageView);
        holder.numTextView.setText(String.valueOf(tag.videosCount) + PeopleSuffix);
        holder.tagTextView.setText(tag.name);
        holder.likeTextView.setText(String.valueOf(tag.likeCount));
        holder.tagTextView.setCompoundDrawablesWithIntrinsicBounds(getDrawableByType(tag.type), null, null, null);
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onClick(tag);
        });
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

    public void add(int location, WorldTag object) {
        mItems.add(location, object);
        notifyItemInserted(location);
    }

    public boolean add(WorldTag object) {
        boolean re = mItems.add(object);
        notifyItemInserted(mItems.size() - 1);
        return re;
    }

    public boolean addAll(int location, Collection<? extends WorldTag> collection) {
        boolean re = mItems.addAll(location, collection);
        notifyItemRangeInserted(location, collection.size());
        return re;
    }

    public boolean addAll(Collection<? extends WorldTag> collection) {
        boolean re = mItems.addAll(collection);
        notifyItemRangeInserted(mItems.size() - collection.size(), collection.size());
        return re;
    }

    public void clear() {
        if (mItems.size() == 0) return;
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnTagClickListener {
        void onClick(WorldTag tag);
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
}
