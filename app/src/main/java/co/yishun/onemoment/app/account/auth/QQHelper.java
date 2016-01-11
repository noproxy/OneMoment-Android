package co.yishun.onemoment.app.account.auth;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

import co.yishun.onemoment.app.LogUtil;

import static java.lang.String.valueOf;

/**
 * A proxy implement to auth by Tencent open api.
 * <p>
 * Created by yyz on 5/30/15.
 */
public class QQHelper implements AuthHelper {
    public static final String APP_ID = "1104574591";
    public static final String SCOPE = "get_user_info";//scope we need access
    private static final String TAG = "QQHelper";

    private final Tencent mTencent;
    private final Activity mActivity;
    private CountDownLatch mLatch;//to make method synchronous
    private UserInfo mInfo = null;//user info to return
    private IUiListener mLoginListener;

    public QQHelper(Activity activity) {
        mActivity = activity;
        mTencent = Tencent.createInstance(APP_ID, mActivity.getApplicationContext());
    }

    public static QQToken format(OAuthToken token) {
        QQToken qqToken = new QQToken(QQHelper.APP_ID);
        qqToken.setAppId(QQHelper.APP_ID);
        qqToken.setOpenId(token.getId());
        qqToken.setAccessToken(token.getToken(), String.valueOf(token.getExpiresIn()));

        return qqToken;
    }

    @Override
    public void login(@NonNull LoginListener loginListener) {
        mLoginListener = createLoginListener(loginListener);
        mTencent.login(mActivity, SCOPE, mLoginListener);
    }

    @Override
    public UserInfo getUserInfo(@NonNull OAuthToken token) {
        com.tencent.connect.UserInfo info = new com.tencent.connect.UserInfo(mActivity, format(token));
        mLatch = new CountDownLatch(1);
        info.getUserInfo(createGetInfoListener());

        //wait asynchronous handle
        try {
            mLatch.await();
            return mInfo;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void handleIntent(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, mLoginListener);
    }

    private IUiListener createLoginListener(LoginListener loginListener) {
        return new IUiListener() {
            @Override
            public void onComplete(Object o) {
                LogUtil.e(TAG, "tencent auth success");
                LogUtil.d(TAG, o.toString());

                /*
                {"access_token":"AA65A9B44143F26EC3D807B82E2CEB30","expires_in":"7776000","openid":"FD86E47D72173F60C8D1FB39FE0E5B24","pay_token":"9814D7483AA85184B955106BBDBAA349","ret":"0","pf":"desktop_m_qq-10000144-android-2002-","pfkey":"c6408025713ffec6b1f659dcd3424531","sendinstall":"0","auth_time":"1433066096308","page_type":"1"}
                 */
                try {
                    JSONObject jsonObject = new JSONObject(o.toString());
                    String token = jsonObject.getString("access_token");
                    String id = jsonObject.getString("openid");
                    long expiresIn = jsonObject.getLong("expires_in");

                    mInfo = new UserInfo();
                    mInfo.id = id;

                    loginListener.onSuccess(new OAuthToken(id, token, expiresIn));
                } catch (JSONException e) {
                    e.printStackTrace();
                    loginListener.onFail();
                }
            }

            @Override
            public void onError(UiError uiError) {
                LogUtil.e(TAG, "tencent auth error: { " +
                        "message: " + uiError.errorMessage + ", " +
                        "detail: " + uiError.errorDetail + ", " +
                        "code: " + uiError.errorCode + "} ");
                loginListener.onFail();
            }

            @Override
            public void onCancel() {
                LogUtil.e(TAG, "tencent auth cancel");
                loginListener.onCancel();
            }
        };
    }

    private IUiListener createGetInfoListener() {
        return new IUiListener() {
            @Override
            public void onComplete(Object o) {
                LogUtil.e(TAG, "get qq info success");
                LogUtil.i(TAG, valueOf(o));

                try {
                    if (mInfo != null && o != null) {
                        JSONObject jsonObject = new JSONObject(String.valueOf(o));
                        mInfo.name = jsonObject.getString("nickname");
                        String gen = jsonObject.getString("gender");
                        switch (gen) {
                            case "男":
                                mInfo.gender = "m";
                                break;
                            case "女":
                                mInfo.gender = "f";
                                break;
                            default:
                                mInfo.gender = "n";
                                break;
                        }

                        try {
                            mInfo.avatar_large = jsonObject.getString("figureurl_qq_2");
                        } catch (JSONException e) {
                            LogUtil.e(TAG, "no qq profile");
                            mInfo.avatar_large = jsonObject.getString("figureurl_2");
                        }

                        mInfo.location = jsonObject.getString("province") + " " + jsonObject.getString("city");
                        mInfo.description = "";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /*
                {"ret":0,"msg":"","is_lost":0,"nickname":"( λ )","gender":"男","province":"加利福尼亚","city":"圣何塞","figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/222222\/FD86E47D72173F60C8D1FB39FE0E5B24\/30","figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/222222\/FD86E47D72173F60C8D1FB39FE0E5B24\/50","figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/222222\/FD86E47D72173F60C8D1FB39FE0E5B24\/100","figureurl_qq_1":"http:\/\/q.qlogo.cn\/qqapp\/222222\/FD86E47D72173F60C8D1FB39FE0E5B24\/40","figureurl_qq_2":"http:\/\/q.qlogo.cn\/qqapp\/222222\/FD86E47D72173F60C8D1FB39FE0E5B24\/100","is_yellow_vip":"0","vip":"0","yellow_vip_level":"0","level":"0","is_yellow_year_vip":"0"}
                 */
                mLatch.countDown();
                LogUtil.i(TAG, mInfo == null ? "info: null" : mInfo.toString());
            }

            @Override
            public void onError(UiError uiError) {
                LogUtil.e(TAG, "get qq info  error: { " +
                        "message: " + uiError.errorMessage + ", " +
                        "detail: " + uiError.errorDetail + ", " +
                        "code: " + uiError.errorCode + "} ");
                mLatch.countDown();
                mInfo = null;
            }

            @Override
            public void onCancel() {
                LogUtil.e(TAG, "get qq info cancel");
                mLatch.countDown();
                mInfo = null;
            }
        };
    }

}
