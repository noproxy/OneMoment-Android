package co.yishun.onemoment.app.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Created by yyz on 7/14/15.
 */
public abstract class BaseFragment extends Fragment {
    Toolbar toolbar;

    protected void setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);
        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        Log.i("setupToolbar", "set home as up true");
    }

    @Override public void onResume() {
        super.onResume();
        setupToolbar((AppCompatActivity) getActivity(), toolbar);
    }
}
