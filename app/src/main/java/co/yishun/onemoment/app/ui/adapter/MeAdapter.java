package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;

import co.yishun.onemoment.app.api.model.WorldTag;

/**
 * Created by Carlos on 2015/8/17.
 */
public class MeAdapter extends SearchAdapter {

    public MeAdapter(Context context, OnItemClickListener<WorldTag> listener) {
        super(context, listener);
    }

}
