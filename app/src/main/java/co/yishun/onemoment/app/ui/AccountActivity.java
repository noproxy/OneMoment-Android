package co.yishun.onemoment.app.ui;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.auth.AccessTokenKeeper;
import co.yishun.onemoment.app.account.auth.UserInfo;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.ui.account.AccountFragment;
import co.yishun.onemoment.app.ui.account.GetPasswordFragment_;
import co.yishun.onemoment.app.ui.account.IntegrateInfoFragment_;
import co.yishun.onemoment.app.ui.account.PhoneLoginFragment;
import co.yishun.onemoment.app.ui.account.PhoneLoginFragment_;
import co.yishun.onemoment.app.ui.account.PhoneSignUpFragment_;
import co.yishun.onemoment.app.ui.common.PickCropActivity;
import retrofit.RestAdapter;

/**
 * Created by yyz on 8/1/15.
 */

@EActivity(R.layout.activity_account)
public class AccountActivity extends PickCropActivity {
    private static final String TAG = "AccountActivity";
    protected FragmentManager fragmentManager;
    @ViewById CoordinatorLayout coordinatorLayout;
    @ViewById(R.id.fab)
    FloatingActionButton floatingActionButton;
    @Extra UserInfo userInfo;
    @Extra AccessTokenKeeper.KeeperType type;
    private Account mAccount;
    private RestAdapter mAdapter;
    private AccountFragment mCurrentFragment;

    public Account getAccountService() {
        return mAccount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG, "AccountActivity onCreate");
        mAdapter = OneMomentV3.createAdapter();
        mAccount = mAdapter.create(Account.class);
        fragmentManager = getSupportFragmentManager();
    }

    @AfterViews
    void setViews() {
// this cause the first add to back stack       setCurrentFragment(new PhoneLoginFragment_());
        if (userInfo == null) {
            mCurrentFragment = new PhoneLoginFragment_();
        } else {
            mCurrentFragment = IntegrateInfoFragment_.builder().userInfo(userInfo).type(type).build();
        }
        fragmentManager.beginTransaction().replace(R.id.fragment_container, mCurrentFragment).commit();
    }

    @Click(R.id.fab)
    void onFABClicked(View view) {
        LogUtil.i(TAG, "fab clicked, currentFragment: " + mCurrentFragment);
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
        findViewById(R.id.signUpByPhone).setVisibility(mCurrentFragment instanceof PhoneLoginFragment ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.getPassword).setVisibility(mCurrentFragment instanceof PhoneLoginFragment ? View.VISIBLE : View.INVISIBLE);
    }

    @Click
    void signUpByPhoneClicked(View view) {
        openFragment(PhoneSignUpFragment_.builder().build());
    }

    @Click void getPasswordClicked(View view) {
        openFragment(GetPasswordFragment_.builder().build());
    }

    @NonNull @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return super.getSnackbarAnchorWithView(coordinatorLayout);
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }

    @Override
    public void onPictureSelectedFailed(Exception e) {
        if (mCurrentFragment instanceof PictureCroppedHandler) {
            ((PictureCroppedHandler) mCurrentFragment).onPictureSelectedFailed(e);
        }
    }

    @Override
    public void onPictureCropped(Uri uri) {
        if (mCurrentFragment instanceof PictureCroppedHandler) {
            ((PictureCroppedHandler) mCurrentFragment).onPictureCropped(uri);
        }
    }

    public interface PictureCroppedHandler {
        void onPictureSelectedFailed(Exception e);

        void onPictureCropped(Uri uri);
    }

    //TODO bug: sign up -> verify -> back to sign up, not touch phone num -> verify => not phone num
}
