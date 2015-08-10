package co.yishun.onemoment.app.ui.account;

import android.text.Editable;
import android.text.TextUtils;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Created by Carlos on 2015/8/11.
 */
@EFragment
abstract class PhonePasswordFragment extends AccountFragment {
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

    public boolean checkPhoneNum() {
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

    public boolean checkPassword() {
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

    public String getPhoneNum() {
        return mPhoneNum;
    }

    public String getPassword() {
        return mPassword;
    }
}
