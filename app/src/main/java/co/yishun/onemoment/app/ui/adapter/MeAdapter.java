package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;

/**
 * Created by Carlos on 2015/8/17.
 */
public class MeAdapter extends SearchAdapter {

    public MeAdapter(Context context, OnItemClickListener<WorldTag> listener) {
        super(context, listener);
    }

}
