package co.yishun.onemoment.app.convert;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created on 2015/12/24.
 */
public class VideoConcat {
    private static final String TAG = "VideoConcat";
    private final List<StringBuilder> mTransCommands;
    private final List<StringBuilder> mFormatCommands;
    private final StringBuilder mConcatCommand;
    private final FFmpeg mFFmpeg;
    private ConcatListener mListener;
    private Context mContext;
    private FFmpegExecuteResponseHandler mTransHandler;
    private FFmpegExecuteResponseHandler mFormatHandler;
    private FFmpegExecuteResponseHandler mConcatHandler;

    public VideoConcat(Context context) {
        Log.i(TAG, " get instance time: " + System.currentTimeMillis());
        this.mContext = context;
        mTransCommands = new ArrayList<>();
        mFormatCommands = new ArrayList<>();
        mConcatCommand = new StringBuilder();
        createHandler();
        mFFmpeg = FFmpeg.getInstance(context);

        try {
            loadLibraryProxy(context, mFFmpeg, new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.e(TAG, "load error");
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "load success");
                }

                @Override
                public void onStart() {
                    Log.i(TAG, "load start");
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "load finish");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "proxy load exception", e);
        }
    }

    // force load armeabi-v7a, because others are removed by gradle
    private static void loadLibraryProxy(Context context, FFmpeg ffmpeg, FFmpegLoadBinaryResponseHandler handler) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        Constructor constructor = Class.forName("com.github.hiteshsondhi88.libffmpeg.FFmpegLoadLibraryAsyncTask").getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        AsyncTask<Void, Void, Boolean> task = (AsyncTask<Void, Void, Boolean>) constructor.newInstance(context, "armeabi-v7a", handler);
        task.execute();
    }

    public VideoConcat setFiles(List<File> files, File outputFile) {
        //ffmpeg -i v3.mp4 -vf transpose=passthrough=portrait v3p.mp4
        File transDir = FileUtil.getCacheFile(mContext, "trans");
        if (!transDir.exists()) transDir.mkdir();
        String transPath = transDir.getPath() + "/";

        File formatDir = FileUtil.getCacheFile(mContext, "format");
        if (!formatDir.exists()) formatDir.mkdir();
        String formatPath = formatDir.getPath() + "/";

        PrintWriter writer;
        File input;
        try {
            input = FileUtil.getCacheFile(mContext, "input.txt");
            if (input.exists()) input.delete();
            input.createNewFile();
            writer = new PrintWriter(input);
        } catch (IOException e) {
            e.printStackTrace();
            return this;
        }

        for (int i = 0; i < files.size(); i++) {
            File in = files.get(i);
            int end = in.getName().lastIndexOf('.');
            if (end < 0) end = in.getName().length() - 1;
            String inName = in.getName().substring(0, end);

            StringBuilder command = new StringBuilder();
            command.append(" -i ")
                    .append(in.getPath())
                    .append(" -vf transpose=passthrough=portrait ")
                    .append(" -preset ultrafast ")
                    .append(" -threads 5 ")
                    .append(" -strict -2 ")
                    .append(" -y ")
                    .append(transPath).append(inName).append(".mp4");
            mTransCommands.add(command);

            command = new StringBuilder();
            command.append(" -i ")
                    .append(transPath).append(inName).append(".mp4")
                    .append(" -vcodec copy -acodec copy -vbsf h264_mp4toannexb ")
                    .append(" -y ")
                    .append(formatPath).append(inName).append(".ts");
            mFormatCommands.add(command);

            writer.println("file " + formatPath + inName + ".ts");
        }
        writer.close();

        mConcatCommand.append(" -f concat -i ")
                .append(input.getPath())
                .append(" -acodec copy -vcodec copy -absf aac_adtstoasc ")
                .append(" -y ")
                .append(outputFile.getPath());

        return this;
    }

    public VideoConcat start() {
        trans();
        format();
        concat();
        return this;
    }

    public VideoConcat setListener(ConcatListener concatListener) {
        mListener = concatListener;
        return this;
    }

    void trans() {
        try {
            for (StringBuilder sb : mTransCommands) {
                String cmd = sb.toString();
                Log.i(TAG, "trans cmd: " + cmd);
                mFFmpeg.execute(cmd, mTransHandler);
            }
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    void format() {
        try {
            for (StringBuilder sb : mFormatCommands) {
                String cmd = sb.toString();
                Log.i(TAG, "format cmd: " + cmd);
                mFFmpeg.execute(cmd, mFormatHandler);
            }
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    void concat() {
        try {
            String cmd = mConcatCommand.toString();
            Log.i(TAG, "concat cmd: " + cmd);
            mFFmpeg.execute(cmd, mConcatHandler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void createHandler() {
        mTransHandler = new FFmpegExecuteResponseHandler() {
            @Override public void onSuccess(String message) {
                if (mListener != null) mListener.onTransSuccess();
            }

            @Override public void onProgress(String message) {
                Log.i(TAG, "onProgress: Trans " + message);
            }

            @Override public void onFailure(String message) {
                if (mListener != null) mListener.onFail();
            }

            @Override public void onStart() {}

            @Override public void onFinish() {}
        };

        mFormatHandler = new FFmpegExecuteResponseHandler() {
            @Override public void onSuccess(String message) {
                if (mListener != null) mListener.onFormatSuccess();
            }

            @Override public void onProgress(String message) {
                Log.i(TAG, "onProgress: Format " + message);
            }

            @Override public void onFailure(String message) {
                if (mListener != null) mListener.onFail();
            }

            @Override public void onStart() {}

            @Override public void onFinish() {}
        };

        mConcatHandler = new FFmpegExecuteResponseHandler() {
            @Override public void onSuccess(String message) {
                if (mListener != null) mListener.onConcatSuccess();
            }

            @Override public void onProgress(String message) {}

            @Override public void onFailure(String message) {
                Log.i(TAG, "concat onFailure: " + message);
                if (mListener != null) mListener.onFail();
            }

            @Override public void onStart() {}

            @Override public void onFinish() {}
        };
    }

    public interface ConcatListener {
        void onTransSuccess();

        void onFormatSuccess();

        void onConcatSuccess();

        void onFail();
    }
}
