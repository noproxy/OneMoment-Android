package co.yishun.onemoment.app.account.auth;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


/**
 * Created by Carlos on 2015/8/13.
 */
public class WeChatHelper implements AuthHelper, IWXAPIEventHandler {
    // 59b32a874e16913c1a3f0d50b047d608
    public static final String APP_ID = "wx669ce174488102f4";
    public static final String STATE = "just_for_safety";
    public static final String SCOPR = "snsapi_userinfo";
    private static final String TAG = "WeChatHelper";
    private final IWXAPI mApi;
    private LoginListener mListener;

    public WeChatHelper(Activity activity) {
        mApi = WXAPIFactory.createWXAPI(activity.getApplicationContext(), APP_ID, true);
        mApi.registerApp(APP_ID);
    }

    public void handleIntent(Intent intent) {
        mApi.handleIntent(intent, this);
    }

    @Override
    public UserInfo getUserInfo(@NonNull OAuthToken token) {
        return null;
    }

    @Override
    public void login(@NonNull LoginListener listener) {
        // send oauth request
        Log.i(TAG, "wechat login");
        mListener = listener;
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = SCOPR;
        req.state = STATE;
        mApi.sendReq(req);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp resp) {
        Log.i(TAG, "get resp: " + resp);
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
//      resp.toBundle(bundle);
//      Resp sp = new Resp(bundle);
//      String code = sp.code;<span style="white-space:pre">
//      Or
                if (!((SendAuth.Resp) resp).state.equals(STATE)) {
                    mListener.onFail();
                    return;
                }
                handleCode(((SendAuth.Resp) resp).code);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                mListener.onCancel();
                break;
            default:
                mListener.onFail();
                break;
        }

    }

    void handleCode(String code) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" +
                    APP_ID + "&secret=0dd0eaf61b02d5154130d2ca55c6da4d&code=" +
                    code + "&grant_type=authorization_code";
            Log.i(TAG, "handle code: " + url);

            try {
                Response response = client.newCall(new Request.Builder().url(url).build()).execute();
                TokenResponse tokenResponse = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(response.body().string(), TokenResponse.class);
                mListener.onSuccess(from(tokenResponse));
            } catch (Exception e) {
                e.printStackTrace();
                mListener.onFail();
            }
        }).start();
    }

    private OAuthToken from(TokenResponse response) {
        return new OAuthToken(response.openid, response.accessToken, response.expiresIn);
    }

    private static class TokenResponse {
        String accessToken;
        long expiresIn;
        String refreshToken;
        String openid;
        String scope;
    }
}
