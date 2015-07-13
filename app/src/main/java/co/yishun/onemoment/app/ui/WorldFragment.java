package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/13/15.
 */
public final class WorldFragment extends BaseFragment {

    public WorldFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_world, container, false);
    }
}
