package co.yishun.onemoment.app.ui.account;

import android.app.Activity;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity.setFABBackgroundColor(getResources().getColor(R.color.colorAccent));
        //TODO set image
    }

    private void next() {
        //TODO open verifyFragment

    }

}
