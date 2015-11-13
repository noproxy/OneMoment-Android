package co.yishun.onemoment.app.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qiniu.android.storage.UploadManager;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.CountDownLatch;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.common.BaseFragment;
import co.yishun.onemoment.app.ui.common.PickCropActivity;

/**
 * Created by Jinge on 2015/11/12.
 */
@EActivity(R.layout.activity_user_info)
public class UserInfoActivity extends PickCropActivity {
    private static final String TAG = "UserInfoActivity";
    @ViewById
    Toolbar toolbar;
    @ViewById
    RelativeLayout avatarLayout;
    @ViewById
    ImageView avatarImage;
    @FragmentById
    ItemFragment usernameFragment;
    @FragmentById
    ItemFragment weiboFragment;
    @FragmentById
    ItemFragment genderFragment;
    @FragmentById
    ItemFragment locationFragment;

    private Uri croppedProfileUri;
    private boolean avatarUploadOk = false;
    private Account mAccount;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
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
        ab.setTitle(getResources().getString(R.string.activity_user_info_title));
        Log.i("setupToolbar", "set home as up true");
    }

    @AfterViews
    void setupViews() {
        avatarLayout.setOnClickListener(this::pickAvatar);
        Picasso.with(this).load(AccountHelper.getUserInfo(this).avatarUrl).into(avatarImage);

        usernameFragment.setTitle(getResources().getString(R.string.activity_user_info_username));
        usernameFragment.setContent(AccountHelper.getUserInfo(this).nickname);

        weiboFragment.setTitle(getResources().getString(R.string.activity_user_info_weibo_id));
        weiboFragment.setContent(AccountHelper.getUserInfo(this).weiboNickname);

        genderFragment.setTitle(getResources().getString(R.string.activity_user_info_gender));
        genderFragment.setContent(AccountHelper.getUserInfo(this).gender.toString());

        locationFragment.setTitle(getResources().getString(R.string.activity_user_info_location));
        locationFragment.setContent(AccountHelper.getUserInfo(this).location);
    }

    void pickAvatar(View view) {
        Crop.pickImage(this);
    }

    @Override
    public void onPictureSelectedFailed(Exception e) {

    }

    @Override
    public void onPictureCropped(Uri uri) {
        croppedProfileUri = uri;
        updateAvatar(AccountHelper.getUserInfo(this)._id);
        Picasso.with(this).load(uri).memoryPolicy(MemoryPolicy.NO_STORE).memoryPolicy(MemoryPolicy.NO_CACHE).into(avatarImage);
    }

    /**
     * return whether error occurs when update avatar.
     *
     * @return true if no need update or update success
     */
    @Background
    void updateAvatar(@NonNull String userId) {
        if (croppedProfileUri == null) return ;
        String uriString = croppedProfileUri.toString();
        String path = uriString.substring(uriString.indexOf(":") + 1);
        String qiNiuKey = Constants.PROFILE_PREFIX + userId + Constants.URL_HYPHEN + Util.unixTimeStamp() + Constants.PROFILE_SUFFIX;

        UploadManager uploadManager = new UploadManager();
        UploadToken token = OneMomentV3.createAdapter().create(Misc.class).getUploadToken(qiNiuKey);
        if (token.code <= 0) {
            Log.e(TAG, "get upload token error: " + token.msg);
            return ;
        }
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
            return;
        }

        Account account = OneMomentV3.createAdapter().create(Account.class);
        User user = account.updateInfo(userId, null, null, qiNiuKey, null);
        if (user.code <= 0) {
            Log.i(TAG, "update info failed: " + user.msg);
            return ;
        }
        AccountHelper.updateOrCreateUserInfo(this, user);
        return ;
    }

    public static class ItemFragment extends BaseFragment {
        TextView itemTitle;
        TextView itemContent;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            ViewGroup viewRoot = (ViewGroup) inflater.inflate(R.layout.fragment_user_info_item, container, false);
            itemTitle = (TextView) viewRoot.findViewById(R.id.itemTitle);
            itemContent = (TextView) viewRoot.findViewById(R.id.itemContent);
            return viewRoot;
        }

        void setTitle(String title) {
            itemTitle.setText(title);
        }

        void setContent(String content) {
            itemContent.setText(content);
        }
    }

}