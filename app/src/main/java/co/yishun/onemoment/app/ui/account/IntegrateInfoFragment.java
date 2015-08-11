package co.yishun.onemoment.app.ui.account;

import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.model.User;

/**
 * Created by Carlos on 2015/8/11.
 */
@EFragment(R.layout.fragment_integrate_info)
public class IntegrateInfoFragment extends AccountFragment {

    @FragmentArg String phoneNum;
    @FragmentArg String password;
    private String nickName;

    @AfterTextChange(R.id.nickNameEditText)
    void onNickNameChanged(Editable text, TextView nicknameText) {
        nickName = text.toString();
    }

    @Override
    int getFABBackgroundColorRes() {
        return R.color.colorSecondary;
    }

    @Override
    int getFABImageResource() {
        return R.drawable.ic_login_done;
    }

    @Override
    public void onFABClick(View view) {
        if (checkInfo()) signUp();
    }

    private boolean checkInfo() {
        return !TextUtils.isEmpty(nickName);
    }

    @Background
    void signUp() {
        mActivity.showProgress(R.string.fragment_integrate_info_sign_up_progress);
        User user = mActivity.getAccountService().signUpByPhone(phoneNum, password, nickName, Account.Gender.MALE, null, "");

        if (user.code > 0) {
            //TODO save account
            mActivity.showSnackMsg(R.string.fragment_integrate_info_sign_up_success);
            exit();
        } else switch (user.errorCode) {
            default:
                mActivity.showSnackMsg(R.string.unknown_error);
                break;
        }
        mActivity.hideProgress();
    }

    @UiThread(delay = 300)
    void exit() {
        mActivity.finish();
    }
}
