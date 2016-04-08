package co.yishun.onemoment.app.ui.account;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.MainActivity_;

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
        return R.drawable.ic_login_login;
    }

    @Override
    public void onFABClick(View view) {
        if (checkPhoneNum() && checkPassword()) login();
    }

    @EditorAction(R.id.passwordEditText)
    void onSubmit(TextView view, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            onFABClick(view);
    }

    @Background
    void login() {
        mActivity.showProgress(R.string.fragment_phone_login_login_progress);
        User user = mActivity.getAccountService().signInByPhone(getPhoneNum(), getPassword());
        mActivity.hideProgress();
        if (user.code > 0) {
            mActivity.showSnackMsg(R.string.fragment_phone_login_success);
            AccountManager.saveAccount(mActivity, user);
            exitWithStartMain();
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

    @UiThread(delay = Constants.INT_EXIT_DELAY_MILLIS)
    void exitWithStartMain() {
        MainActivity_.intent(mActivity).flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).start();
        mActivity.finish();
    }


    @Override
    public void setPageInfo() {
        mPageName = "PhoneLoginFragment";
    }
}
