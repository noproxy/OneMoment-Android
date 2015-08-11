package co.yishun.onemoment.app.ui;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.OneMomentV3;
import co.yishun.onemoment.app.ui.account.AccountFragment;
import co.yishun.onemoment.app.ui.account.PhoneLoginFragment_;
import retrofit.RestAdapter;

/**
 * Created by yyz on 8/1/15.
 */

@EActivity(R.layout.activity_phone)
public class PhoneAccountActivity extends AppCompatActivity {
    private static final String TAG = "PhoneAccountActivity";
    protected FragmentManager fragmentManager;
    @ViewById CoordinatorLayout coordinatorLayout;
    @ViewById(R.id.fab)
    FloatingActionButton floatingActionButton;
    private MaterialDialog mProgressDialog;
    private Account mAccount;
    private RestAdapter mAdapter;
    private AccountFragment mCurrentFragment;

    public Account getAccountService() {
        return mAccount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = OneMomentV3.createAdapter();
        mAccount = mAdapter.create(Account.class);
        fragmentManager = getSupportFragmentManager();
    }

    @AfterViews
    void setViews() {
// this cause the first add to back stack       setCurrentFragment(new PhoneLoginFragment_());
        mCurrentFragment = new PhoneLoginFragment_();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, mCurrentFragment).commit();
    }

    private void setCurrentFragment(AccountFragment fragment) {
        mCurrentFragment = fragment;
        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, fragment).commit();
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

    @Click(R.id.fab)
    void onFABClicked(View view) {
        Log.i(TAG, "fab clicked, currentFragment: " + mCurrentFragment);
        if (mCurrentFragment != null) {
            mCurrentFragment.onFABClick(view);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @UiThread
    public void showProgress(String msg) {
        //TODO show progress
        if (mProgressDialog == null)
            mProgressDialog = new MaterialDialog.Builder(this).theme(Theme.LIGHT).content(msg).progress(true, 0).build();
        mProgressDialog.show();
    }

    public void showProgress(@StringRes int msgRes) {
        showProgress(getString(msgRes));
    }

    @UiThread
    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    public void openFragment(AccountFragment fragment) {
        setCurrentFragment(fragment);
    }

    public void setFABImageDrawable(Drawable drawable) {
        floatingActionButton.setImageDrawable(drawable);
    }

    public void setFABImageResource(@DrawableRes int res) {
        floatingActionButton.setImageResource(res);
    }

    public void setFABBackgroundResource(@DrawableRes int drawableRes) {
        floatingActionButton.setBackgroundResource(drawableRes);
    }

    public void setFABBackgroundColor(int color) {
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}
