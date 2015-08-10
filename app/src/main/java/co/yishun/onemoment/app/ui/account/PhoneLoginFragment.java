package co.yishun.onemoment.app.ui.account;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.PhoneSignUpFragment_;

/**
 * Created by yyz on 8/3/15.
 */

@EFragment(R.layout.fragment_phone_login)
public class PhoneLoginFragment extends AccountFragment {

    private String mPhoneNum;
    private String mPassword;


    @AfterTextChange(R.id.phoneEditText)
    void onPhoneChange(Editable text, TextView phone) {
        mPhoneNum = text.toString();
    }

    @AfterTextChange(R.id.passwordEditText)
    void onPasswordChange(Editable text, TextView phone) {
        mPassword = text.toString();
    }

    @AfterViews
    void setViews() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity.setFABBackgroundColor(getResources().getColor(R.color.colorSecondary));
        //TODO set image
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
            User user = mActivity.getAccountService().signInByPhone(mPhoneNum, mPassword);
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

    @Background
    void startSignUp() {

    }

    @UiThread(delay = 300)
    void exit() {
        mActivity.finish();
    }

    private boolean checkPhoneNum() {
        if (TextUtils.isEmpty(mPhoneNum)) {
            mActivity.showSnackMsg(R.string.fragment_phone_login_phone_empty);
            return false;
        }
        if (!TextUtils.isDigitsOnly(mPhoneNum) || mPhoneNum.trim().length() != 11) {
            mActivity.showSnackMsg(R.string.fragment_phone_login_phone_incorrect);
            return false;
        }
        return true;
    }

    private boolean checkPassword() {
        if (TextUtils.isEmpty(mPassword)) {
            mActivity.showSnackMsg(R.string.fragment_phone_login_password_empty);
            return false;
        }
        if (mPassword.length() <= 5 || mPassword.length() >= 30) {
            //TODO update password require
            mActivity.showSnackMsg(R.string.fragment_phone_login_password_incorrect);
            return false;
        }
        return true;
    }

    @Click
    void signUpByPhoneClicked(View view) {
        mActivity.openFragment(PhoneSignUpFragment_.builder().build());
    }

}
