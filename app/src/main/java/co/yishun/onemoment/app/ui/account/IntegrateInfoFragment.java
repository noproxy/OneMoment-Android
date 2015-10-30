package co.yishun.onemoment.app.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.qiniu.android.storage.UploadManager;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.CountDownLatch;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.account.auth.AccessTokenKeeper;
import co.yishun.onemoment.app.account.auth.UserInfo;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.AccountActivity;
import co.yishun.onemoment.app.ui.MainActivity_;
import co.yishun.onemoment.app.ui.view.GenderSpinner;
import co.yishun.onemoment.app.ui.view.LocationSpinner;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Carlos on 2015/8/11.
 */
@EFragment(R.layout.fragment_integrate_info)
public class IntegrateInfoFragment extends AccountFragment implements AccountActivity.PictureCroppedHandler {

    private static final String TAG = "IntegrateInfoFragment";
    @FragmentArg String phoneNum;
    @FragmentArg String password;
    @FragmentArg UserInfo userInfo;
    @FragmentArg AccessTokenKeeper.KeeperType type;
    @ViewById GenderSpinner genderSpinner;
    @ViewById LocationSpinner locationSpinner;
    @ViewById
    CircleImageView profileImageView;
    @ViewById EditText nickNameEditText;
    private Uri croppedProfileUri;
    private boolean avatarUploadOk = false;

    @AfterTextChange(R.id.nickNameEditText)
    void onNickNameChanged(Editable text, TextView nicknameText) {
        userInfo.name = text.toString().trim();
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
        if (checkInfo()) checkNicknameAndSignUp();
    }

    private boolean checkInfo() {
        Account.Gender gender = genderSpinner.getSelectGender();
        userInfo.gender = gender.toString();
        if (gender == Account.Gender.OTHER) {
            mActivity.showSnackMsg(R.string.fragment_integrate_info_error_gender_empty);
            return false;
        }

        userInfo.location = locationSpinner.getSelectedLocation();
        if (TextUtils.isEmpty(userInfo.location)) {
            mActivity.showSnackMsg(R.string.fragment_integrate_info_error_location_empty);
            return false;
        }
        if (TextUtils.isEmpty(userInfo.name)) {
            mActivity.showSnackMsg(R.string.fragment_integrate_info_error_nickname_empty);
            return false;
        }
        if (userInfo.name.length() >= 90) {
            mActivity.showSnackMsg(R.string.fragment_integrate_info_error_nickname_long);
            return false;
        }
        return true;
    }

    @Background
    void checkNicknameAndSignUp() {
        signUp();
    }

    /**
     * return whether error occurs when update avatar.
     *
     * @return true if no need update or update success
     */
    @SupposeBackground
    boolean updateAvatar(@NonNull String userId) {
        if (croppedProfileUri == null) return true;
        String uriString = croppedProfileUri.toString();
        String path = uriString.substring(uriString.indexOf(":") + 1);
        String qiNiuKey = Constants.PROFILE_PREFIX + userId + Constants.URL_HYPHEN + Util.unixTimeStamp() + Constants.PROFILE_SUFFIX;


        UploadManager uploadManager = new UploadManager();
        UploadToken token = OneMomentV3.createAdapter().create(Misc.class).getUploadToken(qiNiuKey);
        if (token.code <= 0) {
            Log.e(TAG, "get upload token error: " + token.msg);
            return false;
        }
        // don't need
//        String domain = ApiUtil.getVideoResourceDomain();
//        if (domain == null) {
//            Log.e(TAG, "get domain error");
//            return false;
//        }
        CountDownLatch latch = new CountDownLatch(1);
        uploadManager.put(path, qiNiuKey, token.token,
                (s, responseInfo, jsonObject) -> {
                    Log.i(TAG, responseInfo.toString());
                    if (responseInfo.isOK()) {
                        avatarUploadOk = true;
                        Log.i(TAG, "profile upload ok");
                    } else {
                        avatarUploadOk = false;
                        Log.e(TAG, "profile upload error: " + responseInfo.error);
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
            return false;
        }


        User user = mActivity.getAccountService().updateInfo(userId, null, null, qiNiuKey, null);
        if (user.code <= 0) {
            Log.i(TAG, "update info failed: " + user.msg);
            return false;
        }
        AccountHelper.updateOrCreateUserInfo(mActivity, user);
        return true;
    }

    @SupposeBackground
    void signUp() {
        mActivity.showProgress(R.string.fragment_integrate_info_sign_up_progress);
        User user;
        switch (type) {
            case WeChat:
                user = mActivity.getAccountService().signUpByWeChat(userInfo.id, userInfo.name, Account.Gender.format(userInfo.gender), userInfo.avatar_large, userInfo.location, userInfo.name);
                break;
            case Weibo:
                user = mActivity.getAccountService().signUpByWeibo(userInfo.id, userInfo.name, Account.Gender.format(userInfo.gender), userInfo.avatar_large, userInfo.location, userInfo.name);
                break;
            case QQ:
                user = mActivity.getAccountService().signUpByQQ(userInfo.id, userInfo.name, Account.Gender.format(userInfo.gender), userInfo.avatar_large, userInfo.location, userInfo.name);
                break;
            default:
                user = mActivity.getAccountService().signUpByPhone(phoneNum, password, userInfo.name, Account.Gender.format(userInfo.gender), null, userInfo.location);
                break;
        }
        if (user.code > 0) {
            AccountHelper.saveAccount(mActivity, user);
            checkAvatarAndExit(user._id);
        } else switch (user.errorCode) {
            case Constants.ErrorCode.NICKNAME_EXISTS:
                mActivity.showSnackMsg(R.string.fragment_integrate_info_error_nickname_exist);
                break;
            case Constants.ErrorCode.NICKNAME_FORMAT_ERROR:
                mActivity.showSnackMsg(R.string.fragment_integrate_info_error_nickname_invalid);
                break;
            default:
                mActivity.showSnackMsg(R.string.unknown_error);
                break;
        }
        mActivity.hideProgress();
    }

    @SupposeBackground
    void checkAvatarAndExit(@NonNull String userId) {
        boolean success = updateAvatar(userId);
        mActivity.showSnackMsg(success ? R.string.fragment_integrate_info_sign_up_success : R.string.fragment_integrate_info_sign_up_success_but_avatar_upload_failed);
        exitWithStartMain();
    }

    @Click
    void profileImageViewClicked(View view) {
        Crop.pickImage(mActivity);
    }

    @UiThread(delay = Constants.INT_EXIT_DELAY_MILLIS)
    void exitWithStartMain() {
        MainActivity_.intent(mActivity).flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).start();
        //TODO bug not stop the WXEntryAct
    }

    @Override
    public void onPictureSelectedFailed(Exception e) {
        mActivity.showSnackMsg(R.string.activity_phone_account_fail_select_pic);
        Log.e(TAG, "select picture fail", e);
    }

    @Override
    public void onPictureCropped(Uri uri) {
        croppedProfileUri = uri;
        Picasso.with(mActivity).load(croppedProfileUri).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(profileImageView);
    }

    @AfterViews
    void setViews() {
        if (userInfo == null) {
            userInfo = new UserInfo();
        }
        nickNameEditText.setText(userInfo.name);
        genderSpinner.setSelectedGender(Account.Gender.format(userInfo.gender));
        if (TextUtils.isEmpty(userInfo.location))
            locationSpinner.setSelectedLocation(userInfo.location);
        // not set uri, because three party image don't need upload
        // if uri not null( user set avatar replace image from three-party), it will update after sign up
        Picasso.with(mActivity).load(userInfo.avatar_large).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(profileImageView);
    }
}
