package co.yishun.onemoment.app.ui.account;

import android.app.Activity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.PhoneAccountActivity;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created by yyz on 8/3/15.
 */

@EFragment(R.layout.fragment_phone_login)
public class PhoneLoginFragment extends BaseFragment {

    private PhoneAccountActivity mActivity;

    @AfterViews void setViews() {

    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (PhoneAccountActivity) activity;
        mActivity.setTitle(R.string.phone_login_title);
    }
}
