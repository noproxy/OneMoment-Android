package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class TestFragment extends BaseFragment {

    public TestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }
}