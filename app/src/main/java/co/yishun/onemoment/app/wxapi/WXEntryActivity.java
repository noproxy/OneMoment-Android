package co.yishun.onemoment.app.wxapi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.auth.LoginListener;
import co.yishun.onemoment.app.account.auth.OAuthToken;
import co.yishun.onemoment.app.account.auth.WeChatHelper;
import co.yishun.onemoment.app.ui.PhoneAccountActivity_;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by yyz on 7/24/15.
 */
// This Activity cannot use AndroidAnnotations because of WeChat login require WXEntryActivity naming
public class WXEntryActivity extends BaseActivity {
    public static final int REQUEST_WEIBO_LOGIN = 0;
    public static final int REQUEST_QQ_LOGIN = 1;
    public static final int REQUEST_WECHAT_LOGIN = 2;
    private static final String TAG = "WXEntryActivity";
    private static WeChatHelper mWeChatHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (mWeChatHelper != null) mWeChatHelper.handleIntent(getIntent());
        findViewById(R.id.loginByWeChat).setOnClickListener(this::loginByWeChatClicked);
    }

    public void loginByPhoneClicked(final View view) {
        PhoneAccountActivity_.intent(this).start();
    }

    public void loginByWeChatClicked(final View view) {
        mWeChatHelper = new WeChatHelper(this);
        mWeChatHelper.login(new LoginListener() {
            @Override
            public void onSuccess(OAuthToken token) {
                Log.i(TAG, String.valueOf(token));

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
        });
    }

    public void loginByWeiBoClicked(final View view) {

    }

    public void loginByQQClicked(final View view) {

    }

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        //TODO implement
        return null;
    }
}
