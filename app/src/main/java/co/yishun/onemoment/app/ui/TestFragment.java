package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class TestFragment extends BaseFragment {

    Toolbar toolbar;

    public TestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        return rootView;
    }

    protected void setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);
        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_world_menu);
        Log.i("setupToolbar", "set home as up true");
    }

    @Override public void onResume() {
        super.onResume();
        setupToolbar((AppCompatActivity) getActivity(), toolbar);
    }
}
