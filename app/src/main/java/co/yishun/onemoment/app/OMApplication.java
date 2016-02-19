package co.yishun.onemoment.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.umeng.analytics.MobclickAgent;

import co.yishun.onemoment.app.api.authentication.OneMomentClient;
import co.yishun.onemoment.app.api.authentication.OneMomentClientV3;
import co.yishun.onemoment.app.api.authentication.OneMomentClientV4;

/**
 * Created by Carlos on 2015/10/16.
 */
public class OMApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.setUp(this);
        OneMomentClient.setUpCache(this);
        MobclickAgent.openActivityDurationTrack(false);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
