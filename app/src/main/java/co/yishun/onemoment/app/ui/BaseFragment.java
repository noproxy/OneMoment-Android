package co.yishun.onemoment.app.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/14/15.
 */
public abstract class BaseFragment extends Fragment {
    protected void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar((Toolbar) activity.findViewById(R.id.toolbar));
        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override public void onStart() {
        super.onStart();
        setupToolbar();
    }
}
