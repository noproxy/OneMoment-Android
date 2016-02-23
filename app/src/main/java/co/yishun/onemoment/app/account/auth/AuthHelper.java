package co.yishun.onemoment.app.account.auth;

import android.support.annotation.NonNull;

/**
 * A proxy to login and get user's info by three-party open api. <p> Created by yyz on 6/3/15.
 */
public interface AuthHelper {

    /**
     * To get user info by OAuth token
     *
     * @param token to access user info.
     * @return user info
     */
    UserInfo getUserInfo(@NonNull OAuthToken token);

    /**
     * request three-party login.
     *
     * @param listener to receive login result and token.
     */
    void login(@NonNull LoginListener listener);
}
