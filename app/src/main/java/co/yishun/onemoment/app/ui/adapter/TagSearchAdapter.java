package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2015/11/2.
 */
public class TagSearchAdapter extends AbstractRecyclerViewAdapter<String, TagSearchAdapter.TagSearchViewHolder> {

    public TagSearchAdapter(Context context, OnItemClickListener<String> listener) {
        super(context, listener);
    }

    public void replaceItem(int position, String item) {
        if (mItems.get(position).equals(item)) {
            return;
        }
        mItems.remove(position);
        mItems.add(position, item);
        notifyItemChanged(position);
    }

    @Override
    public void onBindViewHolder(TagSearchViewHolder holder, String item, int position) {
        holder.itemTextView.setText(item);
    }

    @Override
    public TagSearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TagSearchViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_tag_search, parent, false));
    }

    public class TagSearchViewHolder extends RecyclerView.ViewHolder {
        final TextView itemTextView;

        public TagSearchViewHolder(View itemView) {
            super(itemView);
            itemTextView = (TextView) itemView.findViewById(R.id.itemTextView);
        }
    }
}
