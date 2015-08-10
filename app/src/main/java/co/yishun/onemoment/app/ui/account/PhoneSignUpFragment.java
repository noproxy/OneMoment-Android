package co.yishun.onemoment.app.ui.account;

import android.view.View;

import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Created by Carlos on 2015/8/11.
 */
@EFragment(R.layout.fragment_phone_sign_up)
public class PhoneSignUpFragment extends PhonePasswordFragment {
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
        //TODO set image
        return R.drawable.ic_fab;
    }

    private void next() {
        //TODO open verifyFragment

    }

}
