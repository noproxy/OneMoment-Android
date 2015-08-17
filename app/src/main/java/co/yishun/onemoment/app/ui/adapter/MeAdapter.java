package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;

/**
 * Created by Carlos on 2015/8/17.
 */
public class MeAdapter extends WorldAdapter {

    public MeAdapter(Context context, OnItemClickListener<WorldTag> listener) {
        super(context, listener);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_me, parent, false));
    }
}
