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
        if (checkVerifyCode()) verify();
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
            mActivity.runOnUiThread(countDownResentView::countDown);
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
    void verify() {
        mActivity.showProgress(R.string.fragment_phone_verify_verify_progress);
        ApiModel result = mActivity.getAccountService().verifyPhone(phoneNum, mVerificationCode);
        mActivity.hideProgress();
        if (result.code > 0) {
            mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_success);
            next();
        } else switch (result.errorCode) {
            case Constants.ErrorCode.PHONE_VERIFY_CODE_WRONG:
                mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_error_verify_fail);
                break;
            case Constants.ErrorCode.ACCOUNT_EXISTS:
                mActivity.showSnackMsg(R.string.fragment_phone_verify_sign_up_error_account_exit);
                break;
            case Constants.ErrorCode.PHONE_VERIFY_CODE_EXPIRES:
                mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_error_expire);
            default:
                mActivity.showSnackMsg(R.string.fragment_phone_verify_verify_error_network);
                break;
        }
    }

    @UiThread(delay = 300)
    void next() {
        mActivity.openFragment(IntegrateInfoFragment_.builder().phoneNum(phoneNum).password(password).build());
    }

    @Override
    public void setPageInfo() {

    }
}
