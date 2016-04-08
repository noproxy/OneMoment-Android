package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by Carlos on 2015/8/16.
 */
public abstract class AbstractRecyclerViewAdapter<I, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    final Context mContext;
    final List<I> mItems = new ArrayList<>();
    private final OnItemClickListener<I> mListener;

    public AbstractRecyclerViewAdapter(Context context, OnItemClickListener<I> listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public void add(int location, I object) {
        mItems.add(location, object);
        notifyItemInserted(location);
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        onBindViewHolder(holder, mItems.get(position), position);
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onClick(holder.itemView, mItems.get(position));
        });
    }

    public abstract void onBindViewHolder(VH holder, I item, int position);

    public boolean add(I object) {
        boolean re = mItems.add(object);
        notifyItemInserted(mItems.size() - 1);
        return re;
    }

    public boolean addAll(int location, Collection<? extends I> collection) {
        boolean re = mItems.addAll(location, collection);
        notifyItemRangeInserted(location, collection.size());
        return re;
    }

    public boolean addAll(Collection<? extends I> collection) {
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

    public interface OnItemClickListener<I> {
        void onClick(View view, I item);
    }
}


