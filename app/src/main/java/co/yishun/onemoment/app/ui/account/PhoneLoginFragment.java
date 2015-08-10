package co.yishun.onemoment.app.ui.account;

import android.view.View;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;

/**
 * Created by yyz on 8/3/15.
 */

@EFragment(R.layout.fragment_phone_login)
public class PhoneLoginFragment extends PhonePasswordFragment {

    @Override
    int getFABBackgroundColorRes() {
        return R.color.colorSecondary;
    }

    @Override
    int getFABImageResource() {
        //TODO set image
        return R.drawable.ic_fab;
    }

    @Override
    public void onFABClick(View view) {
        login();
    }

    @Background
    void login() {
        if (checkPhoneNum() && checkPassword()) {
            //TODO show progress bar
            mActivity.showProgress(R.string.fragment_phone_login_login_progress);
            User user = mActivity.getAccountService().signInByPhone(getPhoneNum(), getPassword());
            mActivity.hideProgress();
            if (user.code > 0) {
                mActivity.showSnackMsg(R.string.fragment_phone_login_success);
                //TODO add account and save info
                exit();
            } else switch (user.errorCode) {
                case Constants.ErrorCode.PHONE_FORMAT_ERROR:
                    mActivity.showSnackMsg(R.string.fragment_phone_login_phone_incorrect);
                    break;
                case Constants.ErrorCode.PASSWORD_NOT_CORRECT:
                    mActivity.showSnackMsg(R.string.fragment_phone_login_error_password);
                    break;
                case Constants.ErrorCode.ACCOUNT_DOESNT_EXIST:
                    mActivity.showSnackMsg(R.string.fragment_phone_login_error_account_not_exist);
                    break;
                default:
                    mActivity.showSnackMsg(R.string.unknown_error);
                    break;
            }
        }
    }

    @UiThread(delay = 300)
    void exit() {
        mActivity.finish();
    }


    @Click
    void signUpByPhoneClicked(View view) {
        mActivity.openFragment(PhoneSignUpFragment_.builder().build());
    }

}
