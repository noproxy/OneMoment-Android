package co.yishun.onemoment.app.wxapi;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SupposeBackground;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
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
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by yyz on 7/24/15.
 */
// This Activity cannot use AndroidAnnotations because of WeChat login require WXEntryActivity naming
public class WXEntryActivity extends BaseActivity implements LoginListener {
    private static final String TAG = "WXEntryActivity";
    static AuthHelper mAuthHelper;
    static Account mAccountService = OneMomentV3.createAdapter().create(Account.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (mAuthHelper != null && mAuthHelper instanceof WeChatHelper)
            ((WeChatHelper) mAuthHelper).handleIntent(getIntent());
        findViewById(R.id.loginByWeChat).setOnClickListener(this::loginByWeChatClicked);
    }

    public void loginByPhoneClicked(final View view) {
        AccountActivity_.intent(this).start();
    }

    public void loginByWeChatClicked(final View view) {
        mAuthHelper = new WeChatHelper(this);
        mAuthHelper.login(this);
    }

    @Override
    public void onSuccess(OAuthToken token) {
        Log.i(TAG, String.valueOf(token));
        AsyncHandler_.getInstance_(this).handleToken(token);
    }

    @Override
    public void onFail() {
        Log.e(TAG, "auth fail.");
        showSnackMsg("fail!");
    }

    @Override
    public void onCancel() {
        Log.i(TAG, "user cancel auth");
    }

    public void loginByWeiBoClicked(final View view) {
        mAuthHelper = new WeiboHelper(this);
        mAuthHelper.login(this);
    }

    public void loginByQQClicked(final View view) {
        mAuthHelper = new QQHelper(this);
        mAuthHelper.login(this);
    }

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }
}

@EBean
class AsyncHandler {
    private static final String TAG = "AsyncHandler";
    private WXEntryActivity mActivity;
    private AuthHelper mAuthHelper;
    private Account mAccountService;


    public AsyncHandler(Context context) {
        mActivity = (WXEntryActivity) context;
        mAuthHelper = WXEntryActivity.mAuthHelper;
        mAccountService = WXEntryActivity.mAccountService;
    }

    /**
     * try to login use token.
     *
     * @param token
     */
    @Background
    void handleToken(OAuthToken token) {
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
    @SupposeBackground
    void handleUser(User user, OAuthToken token) {
        if (user.code == 1) {
            AccountHelper.saveAccount(mActivity, user);
            mActivity.showSnackMsg(R.string.activity_wx_entry_login_success);
            mActivity.exit();
        } else if (user.errorCode == Constants.ErrorCode.ACCOUNT_DOESNT_EXIST) {
            Log.i(TAG, "account not exist, start getting user info");
            getUserInfo(token);
        } else {
            Log.i(TAG, "sign in failed: " + user.msg);
            mActivity.showSnackMsg(R.string.activity_wx_entry_login_fail);
        }
    }

    /**
     * to get user info.
     *
     * @param token
     */
    @SupposeBackground
    void getUserInfo(OAuthToken token) {
        UserInfo info = mAuthHelper.getUserInfo(token);
        AccountActivity_.intent(mActivity).userInfo(info).type(getType(mAuthHelper)).start();
        mActivity.showSnackMsg(R.string.activity_wx_entry_auth_success);
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
}


