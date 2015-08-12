package co.yishun.onemoment.app.ui;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.OneMomentV3;
import co.yishun.onemoment.app.ui.account.AccountFragment;
import co.yishun.onemoment.app.ui.account.PhoneLoginFragment_;
import co.yishun.onemoment.app.ui.common.PickCropActivity;
import retrofit.RestAdapter;

/**
 * Created by yyz on 8/1/15.
 */

@EActivity(R.layout.activity_phone)
public class PhoneAccountActivity extends PickCropActivity {
    private static final String TAG = "PhoneAccountActivity";
    protected FragmentManager fragmentManager;
    @ViewById CoordinatorLayout coordinatorLayout;
    @ViewById(R.id.fab)
    FloatingActionButton floatingActionButton;
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

    @Click(R.id.fab)
    void onFABClicked(View view) {
        Log.i(TAG, "fab clicked, currentFragment: " + mCurrentFragment);
        if (mCurrentFragment != null) {
            mCurrentFragment.onFABClick(view);
        }
    }

    public void openFragment(AccountFragment fragment) {
        fragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, fragment).commit();
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

    public void setCurrentFragment(AccountFragment fragment) {
        mCurrentFragment = fragment;
    }

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return coordinatorLayout;
    }

    @Override
    public void onPictureSelectedFailed(Exception e) {
        showSnackMsg(R.string.activity_phone_account_fail_select_pic);
    }

    @Override
    public void onPictureCropped(Uri uri) {
        if (mCurrentFragment instanceof PictureCroppedHandler) {
            ((PictureCroppedHandler) mCurrentFragment).onPictureCropped(uri);
        }
    }

    public interface PictureCroppedHandler {
        void onPictureCropped(Uri uri);
    }

    //TODO bug: sign up -> verify -> back to sign up, not touch phone num -> verify => not phone num
}
