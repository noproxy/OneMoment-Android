package co.yishun.onemoment.app.wxapi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.ProgressSnackBar;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.account.auth.AccessTokenKeeper;
import co.yishun.onemoment.app.account.auth.AuthHelper;
import co.yishun.onemoment.app.account.auth.LoginListener;
import co.yishun.onemoment.app.account.auth.OAuthToken;
import co.yishun.onemoment.app.account.auth.QQHelper;
import co.yishun.onemoment.app.account.auth.UserInfo;
import co.yishun.onemoment.app.account.auth.WeChatHelper;
import co.yishun.onemoment.app.account.auth.WeiboHelper;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.AccountActivity_;
import co.yishun.onemoment.app.ui.MainActivity_;
import co.yishun.onemoment.app.ui.common.WXRespActivity;

import static java.lang.String.valueOf;

/**
 * Created by yyz on 7/24/15.
 * <p>
 * Rename from WXEntryActivity to EntryActivity by jiangjin.
 * WXEntryActivity is also used for share, so create a transparent WXEntryActivity.
 * </p>
 */
@EActivity(R.layout.activity_login)
public class EntryActivity extends WXRespActivity implements LoginListener {
    private static final String TAG = "EntryActivity";
    private AuthHelper mAuthHelper;
    private Account mAccountService;
    private Snackbar snackbar;

    @AfterViews void setUp() {
        mAccountService = OneMomentV3.createAdapter().create(Account.class);
        if (mAuthHelper != null && mAuthHelper instanceof WeChatHelper)
            ((WeChatHelper) mAuthHelper).handleIntent(getIntent());
    }

    public void loginByPhoneClicked(final View view) {
        AccountActivity_.intent(this).start();
    }

    public void loginByWeChatClicked(final View view) {
        if (isAppInstalled("com.tencent.mm")) {
            showSnackProgress();
            mAuthHelper = new WeChatHelper(this);
            mAuthHelper.login(this);
        } else
            showSnackMsg("WeChat not installed!");
    }

    private void showSnackProgress() {
        snackbar = Snackbar.make(getSnackbarAnchorWithView(null), R.string.activity_wx_entry_login_progress, Snackbar.LENGTH_INDEFINITE);
        ProgressSnackBar.with(snackbar).show();
    }

    @Override
    public void onSuccess(OAuthToken token) {
        LogUtil.i(TAG, valueOf(token));
        handleToken(token);
    }

    @Override
    public void onFail() {
        LogUtil.e(TAG, "auth fail.");
        showSnackMsg("fail!");
    }

    @Override
    public void onCancel() {
        LogUtil.i(TAG, "user cancel auth");
        if (snackbar.isShown()) snackbar.dismiss();
    }

    public void loginByWeiBoClicked(final View view) {
        mAuthHelper = new WeiboHelper(this);
        mAuthHelper.login(this);
        showSnackProgress();
    }

    public void loginByQQClicked(final View view) {
        mAuthHelper = new QQHelper(this);
        mAuthHelper.login(this);
        showSnackProgress();
    }

    @NonNull @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return super.getSnackbarAnchorWithView(null);
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }

    @Override protected void onWXRespIntent(Intent intent) {
        if (mAuthHelper != null && mAuthHelper instanceof WeChatHelper)
            ((WeChatHelper) mAuthHelper).handleIntent(intent);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthHelper instanceof QQHelper)
            ((QQHelper) mAuthHelper).handleIntent(requestCode, resultCode, data);

        if (mAuthHelper instanceof WeiboHelper) {
            ((WeiboHelper) mAuthHelper).handleIntent(requestCode, resultCode, data);
        }
    }

    /**
     * try to login use token.
     *
     * @param token
     */
    @Background void handleToken(OAuthToken token) {
        switch (getType(mAuthHelper)) {
            case WeChat:
                handleUser(mAccountService.getUserInfoByWeChatUid(token.getId()), token);
                break;
            case Weibo:
                handleUser(mAccountService.getUserInfoByWeiboUid(token.getId()), token);
                break;
            case QQ:
                handleUser(mAccountService.getUserInfoByQQUid(token.getId()), token);
                break;
        }
    }

    /**
     * handle login result, if success, exit; if not exist, get user info.
     *
     * @param user
     * @param token
     */
    @SupposeBackground void handleUser(User user, OAuthToken token) {
        if (user.code == 1) {
            AccountManager.saveAccount(this, user);
            showSnackMsg(R.string.activity_wx_entry_login_success);
            SyncManager.syncNow(this);
            exitWithStartMain();
        } else if (user.errorCode == Constants.ErrorCode.ACCOUNT_DOESNT_EXIST) {
            LogUtil.i(TAG, "account not exist, start getting user info");
            getUserInfo(token);
        } else {
            LogUtil.i(TAG, "sign in failed: " + user.msg);
            showSnackMsg(R.string.activity_wx_entry_login_fail);
        }
    }

    /**
     * to get user info.
     *
     * @param token
     */
    @SupposeBackground void getUserInfo(OAuthToken token) {
        UserInfo info = mAuthHelper.getUserInfo(token);
        showSnackMsg(R.string.activity_wx_entry_auth_success);
        startActivity(AccountActivity_.intent(this).userInfo(info).type(getType(mAuthHelper)).get().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
        //TODO startActivity from mActivity context will fail on Huawei. Reason is not clear now.
        //TODO bug: snackbar not work
    }

    private AccessTokenKeeper.KeeperType getType(AuthHelper helper) {
        if (helper instanceof WeChatHelper) {
            return AccessTokenKeeper.KeeperType.WeChat;
        } else if (helper instanceof WeiboHelper) {
            return AccessTokenKeeper.KeeperType.Weibo;
        } else {
            return AccessTokenKeeper.KeeperType.QQ;
        }
    }

    @UiThread(delay = Constants.INT_EXIT_DELAY_MILLIS) void exitWithStartMain() {
        MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).start();
        finish();
    }
}


