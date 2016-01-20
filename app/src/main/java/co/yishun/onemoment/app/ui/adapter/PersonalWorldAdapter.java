package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;

/**
 * Created by Jinge on 2016/1/20.
 */
public class PersonalWorldAdapter extends AbstractRecyclerViewAdapter<WorldTag, PersonalWorldAdapter.PersonalWorldViewHolder> {

    public PersonalWorldAdapter(Context context, OnItemClickListener<WorldTag> listener) {
        super(context, listener);
    }

    @Override public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(PersonalWorldViewHolder holder, WorldTag item, int position) {

    }

    @Override
    public PersonalWorldViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PersonalWorldViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_item_personal_world, parent, false));
    }

    public static class PersonalWorldViewHolder extends RecyclerView.ViewHolder{

        public PersonalWorldViewHolder(View itemView) {
            super(itemView);
        }
    }
}
