package co.yishun.onemoment.app.account.sync.moment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qiniu.android.utils.Etag;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ApiMoment;
import co.yishun.onemoment.app.api.model.Domain;
import co.yishun.onemoment.app.data.VideoUtil;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;

/**
 * Task to fix moment's video and thumbnail.
 * <p>
 * Created by Carlos on 2015/12/20.
 */
public class MomentFixTask implements Runnable {

    private static final String TAG = "MomentFixTask";
    private final static Misc mMiscService = OneMomentV3.createAdapter().create(Misc.class);
    private static Domain mDomain;
    private final ApiMoment mApiMoment;
    private final Moment mMoment;
    private final Callback mOnFail;
    private final Callback mOnSuccess;
    private final Consumer<Integer> mOnProgress;

    public MomentFixTask(@NonNull Moment moment, @Nullable ApiMoment mApiMoment, Callback mOnFail, Callback mOnSuccess, Consumer<Integer> mOnProgress) {
        this.mApiMoment = mApiMoment;
        this.mMoment = moment;
        this.mOnFail = mOnFail;
        this.mOnSuccess = mOnSuccess;
        this.mOnProgress = mOnProgress;
    }

    private boolean isFileHashSame(@NonNull File file, @NonNull ApiMoment apiMoment) {
        boolean isSame = false;
        try {
            isSame = Etag.file(file).equals(apiMoment.hash);
        } catch (IOException e) {
            Log.e(TAG, "exception when hash the fileSynced", e);
        }
        return isSame;
    }

    /**
     * Try to fix video file by download from server.
     *
     * @return true if fix success.
     */
    private boolean fixVideo() {
        Log.i(TAG, "download a moment: " + mMoment);

        if (mDomain == null) {
            Domain domain = mMiscService.getResourceDomain("video");
            if (!domain.isSuccess()) {
                Log.e(TAG, "download failed when get resource domain");
                mOnFail.call();
                return false;
            } else {
                mDomain = domain;
            }
        }


        File targetFile = mMoment.getFile();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(mDomain + mMoment.getKey()).get().build();
        Log.i(TAG, "start download: " + request.urlString());

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            Log.e(TAG, "exception when http call or close the stream", e);
            mOnFail.call();
            return false;
        }

        InputStream inputStream = null;
        FileOutputStream out = null;

        try {
            if (response.code() == 200) {
                byte[] data = new byte[1024];
                int count;
                double target = response.body().contentLength();
                double total = 0;
                inputStream = response.body().byteStream();
                out = new FileOutputStream(targetFile);

                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    out.write(data, 0, count);

                    if (Thread.interrupted()) {
                        Log.i(TAG, "cancel download");// canceled task not failTask++
                        mOnFail.call();
                        return false;
                    }
                    int progress = (int) (total * 100 / target);
                    Log.v(TAG, "progress: " + progress);
                    mOnProgress.accept(progress);
                }
                out.flush();
                out.close();
                inputStream.close();
                mOnSuccess.call();
                return true;
            } else {
                Log.i(TAG, "download video response != 200");
                mOnFail.call();
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "download failed", e);
            mOnFail.call();
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        File momentVideo = mMoment.getFile();
        boolean videoBad = momentVideo.length() == 0
                || mApiMoment != null && !isFileHashSame(momentVideo, mApiMoment);
        if (videoBad) {
            videoBad = !fixVideo();
        }

        boolean thumbBad = mMoment.getLargeThumbPathFile().length() == 0
                || mMoment.getThumbPathFile().length() == 0;

        if (!videoBad && thumbBad) {
            fixThumb();
        }

        Log.i(TAG, "fix end: " + mMoment);
    }

    /**
     * Try to reproduce thumbnail of moment. No video check.
     */
    private void fixThumb() {
        try {
            File large = mMoment.getLargeThumbPathFile();
            File small = mMoment.getThumbPathFile();
            for (int i = 0; i < 3; i++) {
                if (large.length() > 0 && small.length() > 0)
                    break;
                VideoUtil.createThumbs(mMoment.getPath(), large, small);
            }

            if (large.length() > 0 && small.length() > 0)
                Log.i(TAG, "create Thumb ok: " + mMoment);
            else
                mOnSuccess.call();
        } catch (IOException e) {
            mOnFail.call();
        }
    }
}
