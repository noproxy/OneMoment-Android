package co.yishun.onemoment.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.umeng.analytics.MobclickAgent;

import co.yishun.onemoment.app.api.authentication.OneMomentClient;

/**
 * Created by Carlos on 2015/10/16.
 */
public class OMApplication extends MultiDexApplication {

    private static final String TAG = "OMApplication";
    private static String mChannel;

    public static String getChannel() {
        return mChannel;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.setUp(this);
        readChannel();
        OneMomentClient.setUpCache(this);
        MobclickAgent.openActivityDurationTrack(false);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void readChannel() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String channel = bundle.getString("UMENG_CHANNEL");
            if (!TextUtils.isEmpty(channel)) {
                mChannel = channel;
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            LogUtil.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
    }
}
