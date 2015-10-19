package co.yishun.onemoment.app;

import android.app.Application;

import co.yishun.onemoment.app.ui.view.shoot.video.TextureMovieEncoder;

/**
 * Created by Carlos on 2015/10/16.
 */
public class OMApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TextureMovieEncoder.initialize(getApplicationContext());
    }
}
