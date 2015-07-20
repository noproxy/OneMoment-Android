package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/20/15.
 */
@EFragment
public class DiscoveryFragment extends BaseFragment implements View.OnClickListener {

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        return rootView;
    }

    @Click({R.id.btnAlarmEveryday, R.id.btnVideoLike})
    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnVideoLike:
                Intent intent = new Intent(v.getContext(), VideoLikeActivity_.class);
                startActivity(intent);
                break;
            case R.id.btnAlarmEveryday:
                //TODO handle click
                break;
        }
    }


    @Override protected int getTitle() {
        return R.string.discovery_title;
    }
}
