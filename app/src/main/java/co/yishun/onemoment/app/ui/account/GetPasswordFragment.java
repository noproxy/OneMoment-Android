package co.yishun.onemoment.app.ui.account;

import android.view.View;

import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/18.
 */
@EFragment(R.layout.fragment_get_password)
public class GetPasswordFragment extends PhonePasswordFragment {
    @Override
    public void onFABClick(View view) {
        if (checkPhoneNum() && checkPassword()) next();
    }

    @Override
    int getFABBackgroundColorRes() {
        return R.color.colorAccent;
    }

    @Override
    int getFABImageResource() {
        return R.drawable.ic_login_next;
    }

    private void next() {
        mActivity.openFragment(VerifyFragment_.builder().phoneNum(getPhoneNum()).password(getPassword()).type(VerifyFragment.EXTRA_TYPE_SIGN_UP).build());
    }

    @Override public void setPageInfo() {
        mPageName = "GetPasswordFragment";
    }
}
