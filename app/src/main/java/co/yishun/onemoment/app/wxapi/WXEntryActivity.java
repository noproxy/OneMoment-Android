package co.yishun.onemoment.app.wxapi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.account.auth.AuthHelper;
import co.yishun.onemoment.app.account.auth.LoginListener;
import co.yishun.onemoment.app.account.auth.OAuthToken;
import co.yishun.onemoment.app.account.auth.QQHelper;
import co.yishun.onemoment.app.account.auth.WeChatHelper;
import co.yishun.onemoment.app.account.auth.WeiboHelper;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.MainActivity_;
import co.yishun.onemoment.app.ui.PhoneAccountActivity_;
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
        PhoneAccountActivity_.intent(this).start();
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

    @Background
    void handleToken(OAuthToken token) {
        if (mAuthHelper instanceof WeChatHelper) {
            handleUser(mAccountService.getUserInfoByWeChatUid(token.getId()), token);
        } else if (mAuthHelper instanceof WeiboHelper) {
            handleUser(mAccountService.getUserInfoByWeiboUid(token.getId()), token);
        } else if (mAuthHelper instanceof QQHelper) {
            handleUser(mAccountService.getUserInfoByQQUid(token.getId()), token);
        }
    }

    @SupposeBackground
    void handleUser(User user, OAuthToken token) {
        if (user.code == 1) {
            AccountHelper.saveAccount(mActivity, user);
            mActivity.showSnackMsg(R.string.activity_login_login_success);
            mActivity.exit();
        } else if (user.errorCode == Constants.ErrorCode.ACCOUNT_DOESNT_EXIST) {
            Log.i(TAG, "account not exist, start sign up");
            signUp(token);
        } else {
            Log.i(TAG, "sign in failed: " + user.msg);
            mActivity.showSnackMsg(R.string.activity_login_login_fail);
        }
    }

    @SupposeBackground
    void signUp(OAuthToken token) {
        if (mAuthHelper instanceof WeChatHelper) {
            handleUser(mAccountService.getUserInfoByWeChatUid(token.getId()));
        } else if (mAuthHelper instanceof WeiboHelper) {
            handleUser(mAccountService.getUserInfoByWeiboUid(token.getId()));
        } else if (mAuthHelper instanceof QQHelper) {
            handleUser(mAccountService.getUserInfoByQQUid(token.getId()));
        }
    }

    @SupposeBackground
    void handleUser(User user) {
        if (user.code == 1) {
            AccountHelper.saveAccount(mActivity, user);
            mActivity.showSnackMsg(R.string.activity_login_login_success);
            exitWithStartMain();
        } else {
            Log.i(TAG, "sign up failed: " + user.msg);
            mActivity.showSnackMsg(R.string.activity_login_login_fail);
        }
    }


    @UiThread(delay = 300)
    void exitWithStartMain() {
        mActivity.finish();
        MainActivity_.intent(mActivity).flags(Intent.FLAG_ACTIVITY_CLEAR_TASK).start();
    }
}


