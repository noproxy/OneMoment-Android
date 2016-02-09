package co.yishun.onemoment.app.ui.adapter;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import co.yishun.library.datacenter.DataCenterAdapter;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;

/**
 * Created by carlos on 2/9/16.
 */
public class DataCenterWorldAdapter extends DataCenterAdapter<WorldTag, DataCenterWorldAdapter.SimpleViewHolder> {
    private final AbstractRecyclerViewAdapter.OnItemClickListener<WorldTag> mListener;
    private final String PeopleSuffix;
    private final Drawable[] mTagDrawable;


    public DataCenterWorldAdapter(FragmentActivity activity, AbstractRecyclerViewAdapter.OnItemClickListener<WorldTag> listener) {
        super(activity);
        mListener = listener;
        Resources resource = getContext().getResources();
        mTagDrawable = new Drawable[]{
                resource.getDrawable(R.drawable.ic_world_tag_time),
                resource.getDrawable(R.drawable.ic_world_tag_location),
                resource.getDrawable(R.drawable.ic_world_tag_msg)
        };
        PeopleSuffix = resource.getString(R.string.fragment_world_suffix_people_count);
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
    public void onBindViewHolder(SimpleViewHolder holder, int position, WorldTag data) {
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onClick(holder.itemView, data);
        });
        Picasso.with(getContext()).load(data.domain + data.thumbnail).placeholder(R.drawable.pic_slider_loading).into(holder.itemImageView);
        holder.numTextView.setText(String.format(PeopleSuffix, data.videosCount));
        holder.tagTextView.setText(data.name);
        holder.likeTextView.setText(String.valueOf(data.likeCount));
        holder.tagTextView.setCompoundDrawablesWithIntrinsicBounds(getDrawableByType(data.type), null, null, null);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.layout_world_item, parent, false));
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
