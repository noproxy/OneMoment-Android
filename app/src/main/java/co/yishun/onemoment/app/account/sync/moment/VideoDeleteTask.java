package co.yishun.onemoment.app.account.sync.moment;

import android.util.Log;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ApiMoment;

/**
 * Task to delete a apiMoment on server.
 * <p>
 * Created by Carlos on 2015/12/20.
 */
public class VideoDeleteTask implements Runnable {
    private final static Misc mMiscService = OneMomentV3.createAdapter().create(Misc.class);
    private static final String TAG = "VideoDeleteTask";
    private final ApiMoment mApiMoment;

    public VideoDeleteTask(ApiMoment mApiMoment) {
        this.mApiMoment = mApiMoment;
    }

    @Override
    public void run() {
        LogUtil.i(TAG, "delete a video: " + mApiMoment);
        mMiscService.deleteVideo(mApiMoment.getKey());
        LogUtil.i(TAG, "delete a video end: " + mApiMoment);
    }

}
