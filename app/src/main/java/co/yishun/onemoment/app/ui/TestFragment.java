package co.yishun.onemoment.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class TestFragment extends Fragment {
    AppCompatActivity mActivity;


    public TestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar(view);
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    protected void setupToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mActivity.setSupportActionBar(toolbar);

        final ActionBar ab = mActivity.getSupportActionBar();
        assert ab != null;
        ab.setTitle(R.string.title_activity_espresso_test);
        ab.setHomeAsUpIndicator(android.R.drawable.ic_btn_speak_now);
        ab.setDisplayHomeAsUpEnabled(true);
    }
}
