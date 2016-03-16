package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.qiniu.android.storage.UploadManager;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.CountDownLatch;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.auth.AuthHelper;
import co.yishun.onemoment.app.account.auth.LoginListener;
import co.yishun.onemoment.app.account.auth.OAuthToken;
import co.yishun.onemoment.app.account.auth.QQHelper;
import co.yishun.onemoment.app.account.auth.UserInfo;
import co.yishun.onemoment.app.account.auth.WeiboHelper;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.common.BaseFragment;
import co.yishun.onemoment.app.ui.common.PickCropActivity;
import co.yishun.onemoment.app.ui.view.LocationChooseDialog;

/**
 * Created by Jinge on 2015/11/12.
 */
@EActivity(R.layout.activity_user_info)
public class UserInfoActivity extends PickCropActivity implements AccountManager.OnUserInfoChangeListener {
    private static final String TAG = "UserInfoActivity";
    @ViewById
    Toolbar toolbar;
    @ViewById
    RelativeLayout avatarLayout;
    @ViewById
    ImageView avatarImage;
    @FragmentById
    ItemFragment nicknameFragment;
    @FragmentById
    ItemFragment weiboFragment;
    @FragmentById
    ItemFragment genderFragment;
    @FragmentById
    ItemFragment locationFragment;

    private Uri croppedProfileUri;
    private boolean avatarUploadOk = false;
    private AuthHelper mAuthHelper;

    @Override
    public void setPageInfo() {
        mPageName = "UserInfoActivity";
    }

    @AfterViews
    void setupToolbar() {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(getString(R.string.activity_user_info_title));
        LogUtil.i("setupToolbar", "set home as up true");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountManager.addOnUserInfoChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountManager.removeOnUserInfoChangedListener(this);
    }

    @AfterViews
    void setupViews() {
        avatarLayout.setOnClickListener(this::pickAvatar);

        nicknameFragment.setTitle(getString(R.string.activity_user_info_username));
        weiboFragment.setTitle(getString(R.string.activity_user_info_weibo_id));
        genderFragment.setTitle(getString(R.string.activity_user_info_gender));
        locationFragment.setTitle(getString(R.string.activity_user_info_location));
        nicknameFragment.setOnClickListener(this::usernameClicked);
        weiboFragment.setOnClickListener(this::weiboClicked);
        genderFragment.setOnClickListener(this::genderClicked);
        locationFragment.setOnClickListener(this::locationClicked);

        invalidateUserInfo(AccountManager.getUserInfo(this));
    }

    void pickAvatar(View view) {
        Crop.pickImage(this);
    }

    void usernameClicked(View view) {
        new MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title(getString(R.string.activity_user_info_username))
                .input(getString(R.string.activity_user_info_username), AccountManager.getUserInfo(this).nickname, false, (MaterialDialog dialog, CharSequence input) -> {
                    if (TextUtils.equals(input, AccountManager.getUserInfo(this).nickname))
                        return;
                    updateUserInfo(AccountManager.getUserInfo(this)._id, input.toString(), null, null, null);
                })
                .build().show();
    }

    void weiboClicked(View view) {
        User user = AccountManager.getUserInfo(this);
        if (TextUtils.isEmpty(user.weiboUid)) {
            mAuthHelper = new WeiboHelper(this);
            mAuthHelper.login(new LoginListener() {
                @Override
                public void onSuccess(OAuthToken token) {
                    LogUtil.d(TAG, "login success");
                    bindWeibo(token);
                }

                @Override
                public void onFail() {
                    LogUtil.d(TAG, "login fail");
                }

                @Override
                public void onCancel() {
                    LogUtil.d(TAG, "login cancel");
                }
            });
        } else {
            if (TextUtils.isEmpty(user.phone) && TextUtils.isEmpty(user.qqId)
                    && TextUtils.isEmpty(user.weiboUid) && TextUtils.isEmpty(user.weixinUid)) {
                showSnackMsg(R.string.activity_user_info_weibo_id_unbind_forbid);
                return;
            }
            new MaterialDialog.Builder(this)
                    .theme(Theme.LIGHT)
                    .content(String.format(getString(R.string.activity_user_info_weibo_id_unbind_msg), AccountManager.getUserInfo(this).weiboNickname))
                    .positiveText(R.string.view_location_spinner_positive_btn)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            unbindWeibo(AccountManager.getUserInfo(UserInfoActivity.this).weiboUid);
                        }
                    })
                    .build().show();
        }
    }

    void genderClicked(View view) {
        Account.Gender mSelectGender = AccountManager.getUserInfo(this).gender;
        new MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title(R.string.view_gender_spinner_title)
                .items(R.array.view_gender_spinner_items)
                .itemsCallbackSingleChoice(mSelectGender.toInt() % 2, (dialog, view1, which, text) -> {
                    Account.Gender gender = Account.Gender.format(which);
                    if (gender == AccountManager.getUserInfo(this).gender)
                        return true;
                    updateUserInfo(AccountManager.getUserInfo(this)._id, null, gender, null, null);
                    return true; // allow selection
                })
                .positiveText(R.string.view_gender_spinner_positive_btn)
                .show();
    }

    void locationClicked(View view) {
        new LocationChooseDialog.Builder(this)
                .build()
                .setLocationSelectedListener((String location, Pair<String, String> provinceAndDistrict) -> {
                    if (TextUtils.equals(location, AccountManager.getUserInfo(this).location))
                        return;
                    updateUserInfo(AccountManager.getUserInfo(this)._id, null, null, null, location);
                })
                .show();
    }


    @Click(R.id.signOutBtn)
    void signOut() {
        new MaterialDialog.Builder(this).theme(Theme.LIGHT).title(R.string.activity_user_info_sign_out_dialog_title).content(R.string.activity_user_info_sign_out_dialog_content).positiveText(R.string.activity_user_info_sign_out_dialog_cancel).negativeText(R.string.activity_user_info_sign_out_dialog_ok).
                callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        AccountManager.deleteAccount(UserInfoActivity.this);
                        dialog.dismiss();
                        UserInfoActivity.this.finish();
                        SplashActivity_.intent(UserInfoActivity.this).action(Intent.ACTION_MAIN)
                                .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK).start();
                    }
                }).
                show();
    }

    @UiThread
    void invalidateUserInfo(User user) {
        if (user == null) {
            return;
        }
        LogUtil.i(TAG, "user info: " + user);
        Picasso.with(this).load(user.avatarUrl).into(avatarImage);

        nicknameFragment.setContent(user.nickname);
        String weiboID;
        if (TextUtils.isEmpty(user.weiboUid)) {
            weiboID = getString(R.string.activity_user_info_weibo_id_unbound);
        } else {
            weiboID = user.weiboNickname;
        }
        weiboFragment.setContent(weiboID);
        String gender;
        Account.Gender orin = user.gender;
        if (orin != null)
            switch (user.gender) {
                case FEMALE:
                    gender = "♀";
                    break;
                case MALE:
                    gender = "♂";
                    break;
                default:
                    gender = getString(R.string.activity_user_info_gender_unknown);
                    break;
            }
        else gender = getString(R.string.activity_user_info_gender_unknown);
        genderFragment.setContent(gender);
        locationFragment.setContent(user.location);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthHelper instanceof QQHelper)
            ((QQHelper) mAuthHelper).handleIntent(requestCode, resultCode, data);

        if (mAuthHelper instanceof WeiboHelper) {
            ((WeiboHelper) mAuthHelper).handleIntent(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPictureSelectedFailed(Exception e) {

    }

    @Override
    public void onPictureCropped(Uri uri) {
        croppedProfileUri = uri;
        updateAvatar(AccountManager.getUserInfo(this)._id);
        Picasso.with(this).load(uri).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(avatarImage);
    }

    @Background
    void updateAvatar(@NonNull String userId) {
        if (croppedProfileUri == null) return;
        String uriString = croppedProfileUri.toString();
        String path = uriString.substring(uriString.indexOf(":") + 1);
        String qiNiuKey = Constants.PROFILE_PREFIX + userId + Constants.URL_HYPHEN + Util.unixTimeStamp() + Constants.PROFILE_SUFFIX;

        UploadManager uploadManager = new UploadManager();
        UploadToken token = OneMomentV3.createAdapter().create(Misc.class).getUploadToken(qiNiuKey);
        if (token.code <= 0) {
            LogUtil.e(TAG, "get upload token error: " + token.msg);
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        uploadManager.put(path, qiNiuKey, token.token,
                (s, responseInfo, jsonObject) -> {
                    LogUtil.i(TAG, responseInfo.toString());
                    if (responseInfo.isOK()) {
                        avatarUploadOk = true;
                        LogUtil.d(TAG, "loaded " + responseInfo.path);
                        LogUtil.i(TAG, "profile upload ok");
                    } else {
                        avatarUploadOk = false;
                        LogUtil.e(TAG, "profile upload error: " + responseInfo.error);
                    }
                    latch.countDown();
                }, null
        );
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!avatarUploadOk) {
            return;
        }
        updateUserInfo(userId, null, null, qiNiuKey, null);
    }

    @Background
    void updateUserInfo(String userId, String nickname, Account.Gender gender, String qiNiuKey, String location) {
        Account account = OneMomentV3.createAdapter().create(Account.class);
        User user = account.updateInfo(userId, nickname, gender, qiNiuKey, location);
        if (user.code <= 0) {
            LogUtil.i(TAG, "update info failed: " + user.msg);
            switch (user.errorCode) {
                case Constants.ErrorCode.NICKNAME_EXISTS:
                    showSnackMsg(R.string.fragment_integrate_info_error_nickname_exist);
                    break;
                case Constants.ErrorCode.NICKNAME_FORMAT_ERROR:
                    showSnackMsg(R.string.fragment_integrate_info_error_nickname_invalid);
                    break;
                default:
                    int error = user.errorCode;
                    showSnackMsg(getString(R.string.unknown_error) + ":" + error);
                    break;
            }
            return;
        }
        AccountManager.updateOrCreateUserInfo(this, user);
    }

    @Background
    void bindWeibo(OAuthToken token) {
        UserInfo userInfo = new WeiboHelper(this).getUserInfo(token);

        Account account = OneMomentV3.createAdapter().create(Account.class);
        User user = account.bindWeibo(AccountManager.getUserInfo(this)._id, userInfo.id, userInfo.name);
        if (user.code <= 0) {
            LogUtil.i(TAG, "bind weibo failed: " + user.msg);
            if (user.errorCode == Constants.ErrorCode.ACCOUNT_EXISTS) {
                showSnackMsg(getString(R.string.activity_user_info_weibo_id_bind_forbid));
            }
        } else {
            AccountManager.updateOrCreateUserInfo(this, user);
        }
    }

    @Background
    void unbindWeibo(String weiboUid) {
        Account account = OneMomentV3.createAdapter().create(Account.class);
        User user = account.unbindWeibo(AccountManager.getUserInfo(this)._id, weiboUid);
        if (user.code <= 0) {
            LogUtil.i(TAG, "unbind weibo failed: " + user.msg);
        } else {
            AccountManager.updateOrCreateUserInfo(this, user);
        }
    }

    @Override
    public void onUserInfoChange(User info) {
        invalidateUserInfo(info);
    }

    public static class ItemFragment extends BaseFragment {
        ViewGroup rootView;
        TextView itemTitle;
        TextView itemContent;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            rootView = (ViewGroup) inflater.inflate(R.layout.fragment_user_info_item, container, false);
            itemTitle = (TextView) rootView.findViewById(R.id.itemTitle);
            itemContent = (TextView) rootView.findViewById(R.id.itemContent);
            return rootView;
        }

        void setTitle(String title) {
            itemTitle.setText(title);
        }

        void setContent(String content) {
            itemContent.setText(content);
        }

        void setOnClickListener(View.OnClickListener listener) {
            rootView.setOnClickListener(listener);
        }

        @Override
        public void setPageInfo() {
            mIsPage = false;
        }
    }

}
