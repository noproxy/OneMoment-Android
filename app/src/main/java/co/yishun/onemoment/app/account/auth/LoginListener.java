package co.yishun.onemoment.app.account.auth;

/**
 * Created by yyz on 5/30/15.
 */
public interface LoginListener {
    void onSuccess(OAuthToken token);

    void onFail();

    void onCancel();
}
