package co.yishun.onemoment.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

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

    @Override public void onResume() {
        super.onResume();
        Log.i("fragment lifecycle", "resume called!");//        setupToolbar();
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i("fragment lifecycle", "attach called!");
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("fragment lifecycle", "create called!");
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("fragment lifecycle", "on act created called!");
        setupToolbar();
    }

    @Override public void onStart() {
        super.onStart();
        Log.i("fragment lifecycle", "start called!");
    }

    @Override public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.i("fragment lifecycle", "view state restored called!");
    }

    @Override public void onPause() {
        super.onPause();
        Log.i("fragment lifecycle", "pause called!");
    }

    @Override public void onStop() {
        super.onStop();
        Log.i("fragment lifecycle", "resume called!");
    }

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i("fragment lifecycle", "on hidden change called!");
        if (!hidden) {
            setupToolbar();
        }

    }
}
