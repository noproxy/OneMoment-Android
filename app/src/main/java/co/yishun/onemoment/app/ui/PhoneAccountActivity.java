package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.OneMomentV3;
import co.yishun.onemoment.app.ui.account.PhoneLoginFragment;
import retrofit.RestAdapter;

/**
 * Created by yyz on 8/1/15.
 */

@EActivity(R.layout.activity_phone)
public class PhoneAccountActivity extends AppCompatActivity {

    protected FragmentManager fragmentManager;
    @ViewById CoordinatorLayout coordinatorLayout;
    private Account mAccount;
    private RestAdapter mAdapter;

    public Account getAccountService() {
        return mAccount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = OneMomentV3.createAdapter();
        mAccount = mAdapter.create(Account.class);

        fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().replace(R.id.fragment_container, new PhoneLoginFragment()).commit();
    }

    @UiThread
    public void showSnackMsg(String msg) {
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void showSnackMsg(@StringRes int msgRes) {
        showSnackMsg(getString(msgRes));
    }

    public void showProgress() {
        showProgress(R.string.progress_loading_msg);
    }

    @UiThread
    public void showProgress(String msg) {
        //TODO show progress
    }

    public void showProgress(@StringRes int msgRes) {
        showProgress(getString(msgRes));
    }

    public void hideProgress() {

    }
}
