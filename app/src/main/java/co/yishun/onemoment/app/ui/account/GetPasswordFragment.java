package co.yishun.onemoment.app.ui.account;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;

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
        mActivity.openFragment(VerifyFragment_.builder().phoneNum(getPhoneNum()).password(getPassword()).type(VerifyFragment.EXTRA_TYPE_FIND_PASSWORD).build());
    }

    @EditorAction(R.id.passwordEditText)
    void onSubmit(TextView view, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            onFABClick(view);
    }

    @Override
    public void setPageInfo() {
        mPageName = "GetPasswordFragment";
    }
}
