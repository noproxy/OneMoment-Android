package co.yishun.onemoment.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import co.yishun.onemoment.app.ui.view.shoot.video.TextureMovieEncoder;

/**
 * Created by Carlos on 2015/10/16.
 */
public class OMApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        TextureMovieEncoder.initialize(getApplicationContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
