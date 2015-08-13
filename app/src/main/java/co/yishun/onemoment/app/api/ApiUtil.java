package co.yishun.onemoment.app.api;

import android.support.annotation.Nullable;
import android.util.Log;

import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Domain;

/**
 * Created by Carlos on 2015/8/12.
 */
public class ApiUtil {
    private static final String TAG = "ApiUtil";
    private static String mResourceDomain = null;

    /**
     * <strong>block thread</strong>
     */
    @Nullable
    public static String getVideoResourceDomain() {
        if (mResourceDomain == null) {
            Domain domain = OneMomentV3.createAdapter().create(Misc.class).getResourceDomain(Domain.Type.VIDEO);
            if (domain.code >= 0) {
                mResourceDomain = domain.domain.endsWith("/") ? domain.domain : domain.domain + "/";
            } else {
                Log.e(TAG, "get domain error: " + domain.msg);
            }
        }
        return mResourceDomain;
    }
}
