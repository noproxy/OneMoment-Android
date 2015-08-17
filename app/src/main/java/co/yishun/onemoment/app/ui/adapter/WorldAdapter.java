package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;

/**
 * Created by Carlos on 2015/8/14.
 */
public class WorldAdapter extends AbstractRecyclerViewAdapter<WorldTag, WorldAdapter.SimpleViewHolder> {
    private final String PeopleSuffix;
    private final Drawable[] mTagDrawable;

    public WorldAdapter(Context context, OnItemClickListener<WorldTag> listener) {
        super(context, listener);
        Resources resource = context.getResources();
        mTagDrawable = new Drawable[]{
                resource.getDrawable(R.drawable.ic_world_tag_time),
                resource.getDrawable(R.drawable.ic_world_tag_location),
                resource.getDrawable(R.drawable.ic_world_tag_msg)
        };
        PeopleSuffix = " " + context.getString(R.string.fragment_world_suffix_people_count);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_world_item, parent, false));
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

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, WorldTag item, int position) {
        Picasso.with(mContext).load(item.domain + item.thumbnail).into(holder.itemImageView);
        holder.numTextView.setText(String.valueOf(item.videosCount) + PeopleSuffix);
        holder.tagTextView.setText(item.name);
        holder.likeTextView.setText(String.valueOf(item.likeCount));
        holder.tagTextView.setCompoundDrawablesWithIntrinsicBounds(getDrawableByType(item.type), null, null, null);
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
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
