package co.yishun.onemoment.app.account.auth;

import android.support.annotation.NonNull;

public class OAuthToken {

    private final String id;
    private final String token;
    private final long expiresIn;

    public OAuthToken(@NonNull String id, @NonNull String token, long expiresIn) {
        this.id = id;
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
