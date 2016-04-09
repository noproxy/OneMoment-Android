package co.yishun.onemoment.app.ui.account;

import android.app.Activity;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import co.yishun.onemoment.app.ui.AccountActivity;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created by Carlos on 2015/8/10.
 */
public abstract class AccountFragment extends BaseFragment implements AccountFABHandler {
    AccountActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AccountActivity) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.setFABBackgroundColor(getResources().getColor(getFABBackgroundColorRes()));
        mActivity.setFABImageResource(getFABImageResource());
        mActivity.setCurrentFragment(this);
    }

    @ColorRes
    abstract int getFABBackgroundColorRes();

    @DrawableRes
    abstract int getFABImageResource();
}
