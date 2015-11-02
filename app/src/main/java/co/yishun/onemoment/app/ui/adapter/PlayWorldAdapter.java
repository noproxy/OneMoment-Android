package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.validation.TypeInfoProvider;

import co.yishun.onemoment.app.R;

/**
 * Created on 2015/10/28.
 */
public class PlayWorldAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<String> items;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SPACE = 1;

    public PlayWorldAdapter(Context context) {
        mContext = context;
        items = new ArrayList<>();
    }

    public void onBindViewHolder(PlayWorldViewHolder holder, String item, int position) {
        Picasso.with(mContext).load(item).into(holder.avatar);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == getItemCount() - 1) {
            return TYPE_SPACE;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return items.size() + 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            return new PlayWorldViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_avatar, parent, false));
        }
        return new PlayWorldSpaceViewHolder(new View(mContext));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public static class PlayWorldViewHolder extends RecyclerView.ViewHolder {
        public final ImageView avatar;

        public PlayWorldViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }

    public static class PlayWorldSpaceViewHolder extends RecyclerView.ViewHolder {

        public PlayWorldSpaceViewHolder(View itemView) {
            super(itemView);
        }
    }
}
