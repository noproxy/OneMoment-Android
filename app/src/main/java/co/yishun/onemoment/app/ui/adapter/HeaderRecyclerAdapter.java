package co.yishun.onemoment.app.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Carlos on 2015/8/14.
 */
public class HeaderRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final static int TYPE_HEADER = -1;
    private final RecyclerView.Adapter mAdapter;
    private final HeaderProvider mHeaderProvider;

    public HeaderRecyclerAdapter(@NonNull RecyclerView.Adapter adapter, @NonNull HeaderProvider provider) {
        mAdapter = adapter;
        mHeaderProvider = provider;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new ViewHolder(mHeaderProvider.getHeaderView(parent)) {
            };
        } else return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position > 0)
            mAdapter.onBindViewHolder(holder, position - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : mAdapter.getItemViewType(position - 1);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount() + 1;
    }

    public interface HeaderProvider {
        View getHeaderView(ViewGroup viewGroup);
    }
}
