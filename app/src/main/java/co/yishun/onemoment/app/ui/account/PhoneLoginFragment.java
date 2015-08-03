package co.yishun.onemoment.app.ui.account;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created by yyz on 8/3/15.
 */
public class PhoneLoginFragment extends BaseFragment {

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_phone_login, container, false);


        return rootView;
    }

}
