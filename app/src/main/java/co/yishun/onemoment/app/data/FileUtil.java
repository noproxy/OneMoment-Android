package co.yishun.onemoment.app.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.model.ApiMoment;
import co.yishun.onemoment.app.api.model.QiniuKeyProvider;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.config.Constants;

import static co.yishun.onemoment.app.config.Constants.URL_HYPHEN;

/**
 * Created by Carlos on 2015/8/21.
 */
public class FileUtil {
    public static final String MOMENT_STORE_DIR = "moment";
    public static final String WORLD_STORE_DIR = "world";

    private static final String TAG = "FileUtil";

    public static File getThumbnailStoreFile(Context context, QiniuKeyProvider provider, Type type) {
        String dir = provider instanceof ApiMoment ? MOMENT_STORE_DIR : WORLD_STORE_DIR;
        return new File(getMediaStoreDir(context, dir), type.getPrefix(context) + provider.getName() + type.getSuffix());
    }

    public static File getWorldVideoStoreFile(Context context, Video video) {
        return new File(getMediaStoreDir(context, WORLD_STORE_DIR), video.fileName);
    }

    /**
     * @param unixTimeStamp when the moment is shot. Null if use now.
     * @return file of the local moment shot at sometime.
     */
    public static File getLocalMomentStoreFile(Context context, @Nullable Long unixTimeStamp) {
        File mediaStorageDir = getMediaStoreDir(context, MOMENT_STORE_DIR);
        return getMediaStoreFile(context, mediaStorageDir, Type.LOCAL, unixTimeStamp);
    }

    /**
     * Return supposing name of synced type of a video on server
     */
    public static File getSyncedMomentStoreFile(Context context, @NonNull ApiMoment apiMoment) {
        File mediaStorageDir = getMediaStoreDir(context, MOMENT_STORE_DIR);
        return getMediaStoreFile(context, mediaStorageDir, Type.SYNCED, apiMoment.getUnixTimeStamp());
    }

    private static File getMediaStoreFile(Context context, File dir, Type type, @Nullable Long unixTimeStamp) {
        Log.i(TAG, "timestamp: " + unixTimeStamp);
        String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(unixTimeStamp == null ? new Date() : unixTimeStamp * 1000);
        Log.i(TAG, "formatted time: " + time);
        return new File(dir, type.getPrefix(context) + time + URL_HYPHEN + unixTimeStamp + type.getSuffix());
    }

    private static File getMediaStoreDir(Context context, @MediaDir String dirType) {
        return context.getDir(dirType, Context.MODE_PRIVATE);
    }

    public static long parseTimeStamp(File file) {
        return parseTimeStamp(file.getPath());
    }

    public static long parseTimeStamp(String pathOrFileName) {
        return Long.parseLong(pathOrFileName.substring(pathOrFileName.lastIndexOf(URL_HYPHEN) + 1, pathOrFileName.lastIndexOf(".")));
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
                return AccountHelper.getAccountId(context) + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },
        RECORDED {
            @Override
            public String getPrefix(Context context) {
                return "VID" + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },
        LOCAL {
            @Override
            public String getPrefix(Context context) {
                return "LOC" + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.VIDEO_FILE_SUFFIX;
            }
        },
        LARGE_THUMB {
            @Override
            public String getPrefix(Context context) {
                return "LAT" + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.THUMB_FILE_SUFFIX;
            }
        },
        MICRO_THUMB {
            @Override
            public String getPrefix(Context context) {
                return "MIT" + URL_HYPHEN;
            }

            @Override
            public String getSuffix() {
                return Constants.THUMB_FILE_SUFFIX;
            }
        };

        public abstract String getPrefix(Context context);

        public abstract String getSuffix();
    }

    @StringDef({MOMENT_STORE_DIR, WORLD_STORE_DIR})
    public @interface MediaDir {
    }
}
