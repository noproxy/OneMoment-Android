package co.yishun.onemoment.app.ui.account;

import android.app.Activity;

import co.yishun.onemoment.app.ui.PhoneAccountActivity;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created by Carlos on 2015/8/10.
 */
public abstract class AccountFragment extends BaseFragment implements AccountFABHandler {
    PhoneAccountActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (PhoneAccountActivity) activity;
    }
}
