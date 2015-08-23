package co.yishun.onemoment.app.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.model.Moment;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.config.Constants;

/**
 * Created by Carlos on 2015/8/21.
 */
public class FileUtil {

    private static final String TAG = "FileUtil";

    public static File getTagVideoStoreFile(Context context, TagVideo video) {
        File worldDir = context.getDir(Constants.WORLD_STORE_DIR, Context.MODE_PRIVATE);
        return new File(worldDir, video.fileName);
    }

    /**
     * Return supposing name of synced type of a video on server
     */
    public static File getOutputMediaFile(Context context, Moment syncedVideo) {
        return getOutputMediaFile(context, FileUtil.Type.SYNCED, syncedVideo.getTimeStamp());
    }

    public static String getOutputMediaPath(Context context, Type type, @Nullable Long unixTimestamp) {
        return getOutputMediaFile(context, type, unixTimestamp).getPath();
    }

    public static File getOutputMediaFile(Context context, Type type, @Nullable Long timestamp) {
        File mediaStorageDir = context.getDir(Constants.Moment_STORE_DIR, Context.MODE_PRIVATE);
        Log.i(TAG, "timestamp: " + timestamp);
        String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(timestamp == null ? new Date() : timestamp * 1000);
        Log.i(TAG, "formatted time: " + time);
        return new File(mediaStorageDir.getPath() + File.separator + type.getPrefix(context) + Constants.URL_HYPHEN + time + Constants.URL_HYPHEN + timestamp + type.getSuffix());
    }

    public static File getOutputMediaDir(Context context) {
        return context.getDir(Constants.Moment_STORE_DIR, Context.MODE_PRIVATE);
    }

    public static File getOutputMediaFile(Context context, Type type, @NonNull File file) {
        return getOutputMediaFile(context, type, parseTimeStamp(file));
    }

    public static long parseTimeStamp(File file) {
        return parseTimeStamp(file.getPath());
    }

    public static long parseTimeStamp(String pathOrFileName) {
        return Long.parseLong(pathOrFileName.substring(pathOrFileName.lastIndexOf(Constants.URL_HYPHEN) + 1, pathOrFileName.lastIndexOf(".")));
    }

    /**
     * return media type by file path.
     * <p>
     * You must ensure the file is one of {@link Type}. Otherwise it may cause wrong result.
     *
     * @param filePath of the media file
     * @return type of the media file
     */
    public static Type whatTypeOf(String filePath) {
        File file = new File(filePath);
        Type type = null;
        if (file.getName().length() > 3) {
            String prefix = file.getName().substring(0, 3);
            switch (prefix) {
                case "LOC":
                    type = Type.LOCAL;
                    break;
                case "VID":
                    type = Type.RECORDED;
                    break;
                case "LAT":
                    type = Type.LARGE_THUMB;
                    break;
                case "MIT":
                    type = Type.MICRO_THUMB;
                    break;
                default:
                    type = Type.SYNCED;
                    break;
            }
        }
        return type;
    }

    public enum Type {
        SYNCED {
            @Override
            public String getPrefix(Context context) {
                return AccountHelper.getAccountId(context);
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },
        RECORDED {
            @Override
            public String getPrefix(Context context) {
                return "VID";
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },
        LOCAL {
            @Override
            public String getPrefix(Context context) {
                return "LOC";
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },
        LARGE_THUMB {
            @Override
            public String getPrefix(Context context) {
                return "LAT";
            }

            @Override
            public String getSuffix() {
                return Constants.THUMB_FILE_SUFFIX;
            }
        },
        MICRO_THUMB {
            @Override
            public String getPrefix(Context context) {
                return "MIT";
            }

            @Override
            public String getSuffix() {
                return Constants.THUMB_FILE_SUFFIX;
            }
        };

        public abstract String getPrefix(Context context);

        public abstract String getSuffix();
    }
}
