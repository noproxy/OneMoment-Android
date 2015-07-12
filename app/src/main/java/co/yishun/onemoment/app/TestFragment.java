package co.yishun.onemoment.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.ui.EspressoTestActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class TestFragment extends Fragment {

    public TestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    public void onBtnClick(View view) {
        startActivity(new Intent(getActivity(), EspressoTestActivity.class));
    }
}
