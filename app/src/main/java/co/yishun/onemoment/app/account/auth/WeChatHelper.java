package co.yishun.onemoment.app.account.auth;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by Carlos on 2015/8/13.
 */
public class WeChatHelper implements AuthHelper {
    // 59b32a874e16913c1a3f0d50b047d608
    public static final String APP_ID = "59b32a874e16913c1a3f0d50b047d608";
    public static final String STATE = "just_for_safety";
    public static final String SCOPR = "snsapi_userinfo";
    private final IWXAPI mApi;

    public WeChatHelper(Context context) {
        mApi = WXAPIFactory.createWXAPI(context, APP_ID, true);
        mApi.registerApp(APP_ID);
    }

    @Override
    public UserInfo getUserInfo(@NonNull OAuthToken token) {
        return null;
    }

    @Override
    public void login(@NonNull LoginListener listener) {
        // send oauth request
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = SCOPR;
        req.state = STATE;
        mApi.sendReq(req);
        //TODO receive
    }
}
