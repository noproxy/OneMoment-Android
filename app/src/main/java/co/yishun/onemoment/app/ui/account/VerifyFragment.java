package co.yishun.onemoment.app.ui.account;

import android.app.Activity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.view.CountDownResentView;

/**
 * Created by Carlos on 2015/8/11.
 */
@EFragment(R.layout.fragment_phone_verify)
public class VerifyFragment extends AccountFragment {
    public static final String EXTRA_TYPE_SIGN_UP = "signup";
    public static final String EXTRA_TYPE_FIND_PASSWORD = "reset_pw";
    @ViewById CountDownResentView countDownResentView;
    @FragmentArg String type = EXTRA_TYPE_SIGN_UP;
    @FragmentArg String phoneNum;
    @FragmentArg String password;
    boolean isSending = false;
    private String mVerificationCode;

    @Override
    public void onFABClick(View view) {
        if (checkVerifyCode()) signUp();
    }

    @AfterTextChange(R.id.verificationCodeEditText)
    void onPhoneChange(Editable text, TextView textView) {
        mVerificationCode = text.toString();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        sendSms();
    }

    @AfterViews
    void setViews() {
        countDownResentView.setOnClickListenerWhenEnd(view -> {
            if (isSending)
                sendSms();
            mActivity.showSnackMsg(R.string.fragment_phone_verify_sms_sending);//TODO change to sticky
        });
    }

    @Background
    void sendSms() {
        isSending = true;
        ApiModel model = mActivity.getAccountService().sendVerifySms(phoneNum, type);
        if (model.code > 0) {
            countDownResentView.countDown();
            mActivity.showSnackMsg(R.string.fragment_phone_verify_sms_success);
        } else switch (model.errorCode) {
            case Constants.ErrorCode.ACCOUNT_EXISTS:
                mActivity.showSnackMsg(R.string.fragment_phone_verify_sms_fail_account_exist);
                break;
            default:
                mActivity.showSnackMsg(R.string.fragment_phone_verify_sms_fail);
                break;
        }
        isSending = false;
    }

    @Override
    int getFABBackgroundColorRes() {
        return type.equals(EXTRA_TYPE_SIGN_UP) ? R.color.colorAccent : R.color.colorSecondary;
    }

    @Override
    int getFABImageResource() {
        return type.equals(EXTRA_TYPE_SIGN_UP) ? R.drawable.ic_login_next : R.drawable.ic_login_done;
    }

    private boolean checkVerifyCode() {
        return !TextUtils.isEmpty(mVerificationCode) && mVerificationCode.length() < 8 && mVerificationCode.length() > 2;
    }

    @Background
    void signUp() {
        mActivity.showProgress(R.string.fragment_phone_verify_sign_up_progress);
        User user = mActivity.getAccountService().signUpByPhone(phoneNum, password);
        mActivity.hideProgress();
        if (user.code > 0) {
            mActivity.showSnackMsg(R.string.fragment_phone_verify_sign_up_success);
            //TODO save account
            exit();
        } else handleErrorCode(user.errorCode);
    }

    @UiThread(delay = 300)
    void exit() {
        mActivity.finish();
    }

    private void handleErrorCode(int errorCode) {
        switch (errorCode) {
            case Constants.ErrorCode.PHONE_VERIFY_CODE_WRONG:
                mActivity.showSnackMsg(R.string.fragment_phone_verify_sign_up_error_verify_fail);
                break;
            case Constants.ErrorCode.ACCOUNT_EXISTS:
                mActivity.showSnackMsg(R.string.fragment_phone_verify_sign_up_error_account_exit);
                break;
            default:
                mActivity.showSnackMsg(R.string.unknown_error);
                break;
            //TODO more error
        }
    }
}
