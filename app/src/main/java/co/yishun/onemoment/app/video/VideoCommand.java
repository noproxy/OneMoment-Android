package co.yishun.onemoment.app.video;

import android.content.Context;
import android.os.AsyncTask;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import co.yishun.onemoment.app.LogUtil;

/**
 * Created on 2015/12/26.
 */
public abstract class VideoCommand {
    private static final String TAG = "VideoCommand";
    protected final FFmpeg mFFmpeg;
    protected VideoCommandListener mListener;
    protected Context mContext;

    public VideoCommand(Context context) {
        this.mContext = context;
        mFFmpeg = FFmpeg.getInstance(context);
        try {
            loadLibraryProxy(context, mFFmpeg, new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    LogUtil.e(TAG, "load error");
                }

                @Override
                public void onSuccess() {
                    LogUtil.i(TAG, "load success");
                }

                @Override
                public void onStart() {
                    LogUtil.i(TAG, "load start");
                }

                @Override
                public void onFinish() {
                    LogUtil.i(TAG, "load finish");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "proxy load exception", e);
        }
    }

    // force load armeabi-v7a, because others are removed by gradle
    private static void loadLibraryProxy(Context context, FFmpeg ffmpeg, FFmpegLoadBinaryResponseHandler handler) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        Constructor constructor = Class.forName("com.github.hiteshsondhi88.libffmpeg.FFmpegLoadLibraryAsyncTask").getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        AsyncTask<Void, Void, Boolean> task = (AsyncTask<Void, Void, Boolean>) constructor.newInstance(context, "armeabi-v7a", handler);
        task.execute();
    }

    public abstract void start();

    public enum VideoCommandType {
        COMMAND_TRANSPOSE,
        COMMAND_FORMAT,
        COMMAND_CONCAT,
        COMMAND_CONVERT
    }

    public interface VideoCommandListener {
        void onSuccess(VideoCommandType type);

        void onFail(VideoCommandType type);
    }
}
