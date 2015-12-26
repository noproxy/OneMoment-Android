package co.yishun.onemoment.app.video;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;

/**
 * Created on 2015/12/26.
 */
public class VideoConvert extends VideoCommand {
    StringBuilder mCommand = new StringBuilder();
    private FFmpegExecuteResponseHandler mHandler;

    public VideoConvert(Context context) {
        super(context);
        createHandler();
    }

    public VideoConvert setListener(VideoCommandListener listener) {
        mListener = listener;
        return this;
    }

    public VideoConvert setFiles(File input, File output) {
        mCommand.append(" -i ")
                .append(input.getPath())
                .append(" -vf")
                .append(" transpose=passthrough=portrait")
                .append(" crop='if(gt(iw,ih),ih,iw)':'if(gt(iw,ih),ih,iw)',scale=480:480")
                .append(" -t 1.2")
                .append(" -y")
                .append(" -preset ultrafast")
                .append(" -threads 5")
                .append(" -strict")
                .append(" -2 ")
                .append(output.getPath());
        return this;
    }

    @Override public void start() {
        try {
            mFFmpeg.execute(mCommand.toString(), mHandler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void createHandler() {
        mHandler = new FFmpegExecuteResponseHandler() {
            @Override public void onSuccess(String message) {
                if (mListener != null) mListener.onSuccess(VideoCommandType.COMMAND_CONVERT);
            }

            @Override public void onProgress(String message) {}

            @Override public void onFailure(String message) {
                if (mListener != null) mListener.onFail(VideoCommandType.COMMAND_CONVERT);
            }

            @Override public void onStart() { }

            @Override public void onFinish() { }
        };
    }
}
