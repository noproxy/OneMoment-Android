package co.yishun.onemoment.app.data;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.model.ApiMoment;
import co.yishun.onemoment.app.api.model.QiniuKeyProvider;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.config.Constants;

import static co.yishun.onemoment.app.config.Constants.URL_HYPHEN;

/**
 * Util to create storing paths of videos, images and read information from those file name. <p> You
 * should always give a {@link QiniuKeyProvider} to provide a standard naming part for images and
 * videos. <p> Created by Carlos on 2015/8/21.
 */
public class FileUtil {
    public static final String MOMENT_STORE_DIR = "moment";
    public static final String WORLD_STORE_DIR = "world";
    public static final String THUMB_STORE_DIR = "thumb";

    private static final String TAG = "FileUtil";

    /**
     * Retrieve the corresponding thumbnail path of an {@link QiniuKeyProvider}
     *
     * @param context  to retrieve application private store space.
     * @param provider to provide standard naming part.
     * @param type     of the thumbnail
     * @return path of the thumbnail, it may not exist.
     */
    public static File getThumbnailStoreFile(Context context, QiniuKeyProvider provider, Type type) {
        String dir = THUMB_STORE_DIR;
        final String key = provider.getKey();
        //        final String name = key.substring(0, key.lastIndexOf('.'));
        return new File(getMediaStoreDir(context, dir), type.getPrefix(context) + key + type.getSuffix());
    }

    /**
     * Retrieve the video file path of an {@link Video}
     *
     * @param context to retrieve application private store space.
     * @param video   to provide standard naming part.
     * @return path of the Video, it may not exist.
     */
    public static File getWorldVideoStoreFile(Context context, QiniuKeyProvider video) {
        return new File(getMediaStoreDir(context, WORLD_STORE_DIR), video.getKey());
    }

    /**
     * Get database path for WebView.
     *
     * @return path
     */
    public static String getDatabasePath(Context context) {
        return context.getApplicationInfo().dataDir + "/databases/";
    }

    /**
     * Retrieve the moment file path.
     *
     * @return file of the moment shot at this time.
     */
    public static File getMomentStoreFile(Context context) {
        File mediaStorageDir = getMediaStoreDir(context, MOMENT_STORE_DIR);
        return getMediaStoreFile(context, mediaStorageDir, Type.SYNCED, null);
    }

    /**
     * Retrieve the moment file path by unix timestamp.
     *
     * @param unixTimeStamp when the moment is shot. Null if use now.
     * @return file of the moment shot at sometime.
     */
    public static File getMomentStoreFile(Context context, String unixTimeStamp) {
        File mediaStorageDir = getMediaStoreDir(context, MOMENT_STORE_DIR);
        return getMediaStoreFile(context, mediaStorageDir, Type.SYNCED, unixTimeStamp);
    }

    /**
     * Retrieve the moment file path by {@link ApiMoment}.
     */
    public static File getMomentStoreFile(Context context, @NonNull ApiMoment apiMoment) {
        File mediaStorageDir = getMediaStoreDir(context, MOMENT_STORE_DIR);
        return new File(mediaStorageDir, apiMoment.getKey());
    }

    private static File getMediaStoreFile(Context context, File dir, Type type,
                                          @Nullable String unixTimeStamp) {
        if (unixTimeStamp == null) {
            unixTimeStamp = String.valueOf(new Date().getTime() / 1000);
        }
        LogUtil.i(TAG, "timestamp: " + unixTimeStamp);
        String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(Long.parseLong(unixTimeStamp) * 1000);
        LogUtil.i(TAG, "formatted time: " + time);
        return new File(dir, type.getPrefix(context) + time + URL_HYPHEN + unixTimeStamp + type.getSuffix());
    }

    public static File getMediaStoreDir(Context context, @MediaDir String dirType) {
        return context.getDir(dirType, Context.MODE_PRIVATE);
    }

    /**
     * Retrieve unix timestamp from file.
     *
     * @param file to retrieve.
     * @return unix timestamp in filename.
     */
    public static String parseTimeStamp(File file) {
        return parseTimeStamp(file.getPath());
    }

    /**
     * Retrieve unix timestamp from string.
     *
     * @param pathOrFileName to retrieve.
     * @return unix timestamp in filename.
     */
    public static String parseTimeStamp(String pathOrFileName) {
        return pathOrFileName.substring(pathOrFileName.lastIndexOf(URL_HYPHEN) + 1, pathOrFileName.lastIndexOf("."));
    }

    /**
     * return media type by file path. <p> You must ensure the file is one of {@link Type}.
     * Otherwise it may cause wrong result.
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

    public static File getCacheFile(Context context, String fileName) {
        return new File(getCacheDirectory(context, false), fileName);
    }

    public static File getVideoCacheFile(Context context) {
        return new File(getCacheDirectory(context, false), "video-" + System.currentTimeMillis() + ".mp4");
    }

    /*
    * for cache normal
    */
    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;

        if (preferExternal && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            appCacheDir = getExternalDirectory(context);
        }

        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }

        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache_short/";
            LogUtil.d(FileUtil.class.getName(), "Can't define system cache directory! use " + cacheDirPath);
            appCacheDir = new File(cacheDirPath);
        }

        return appCacheDir;
    }

    /*
     * for cache only
     */
    public static File getLongCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;

        if (preferExternal && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            appCacheDir = getExternalDirectory(context);
        }

        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }

        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            LogUtil.d(FileUtil.class.getName(), "Can't define system cache directory! use " + cacheDirPath);
            appCacheDir = new File(cacheDirPath);
        }

        return appCacheDir;
    }

    private static File getExternalDirectory(Context context) {

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                LogUtil.d(FileUtil.class.getName(), "无法创建SDCard cache");
                return null;
            }

            //try {
            //    new File(cacheDir, ".nomedia").createNewFile();
            //} catch (IOException e) {
            //    Log.d(FileUtil.class.getName(), "无法创建 .nomedia 文件");
            //}
        }

        return cacheDir;
    }

    public static File getExportVideoFile() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/" +
                Environment.DIRECTORY_DCIM + "/yishun");
        if (!dir.exists()) dir.mkdir();
        String time = new SimpleDateFormat(Constants.TIEM_FORMAT_EXPORT,
                Locale.getDefault()).format(new Date());
        return new File(dir.getPath(), Constants.EXPORT_VIDEO_PREFIX
                + time + Constants.VIDEO_FILE_SUFFIX);
    }

    /**
     * Copy file from {@code src} to {@code dst}. {@link File#renameTo(File)} cannot move file from
     * internal storage to external storage. This method won't delete the {@code src} file. If you
     * want to move instead of copy, you should delete the {@code src} file.
     *
     * @return true, if the copy success; false, otherwise.
     */
    public static boolean copyFile(File src, File dst) {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inChannel != null)
                    inChannel.close();
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyResToFile(Context context, int id, String path) {
        InputStream in = context.getResources().openRawResource(id);
        FileOutputStream out = null;
        byte[] buff = new byte[1024];
        int read;
        try {
            out = new FileOutputStream(path);
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File getInternalFile(Context context, String filename) {
        return new File(context.getFilesDir(), filename);
    }

    public static boolean unZip(String zipPath, String outputPath) {
        InputStream inputStream = null;
        ZipInputStream zipInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            String filename;
            inputStream = new FileInputStream(zipPath);
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            ZipEntry zipEntry;
            File entryFile;
            byte[] buffer = new byte[1024];
            int count;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                filename = zipEntry.getName();
                entryFile = new File(outputPath, filename);
                if (zipEntry.isDirectory()) {
                    entryFile.mkdirs();
                    continue;
                }

                if (entryFile.exists()) entryFile.delete();
                fileOutputStream = new FileOutputStream(entryFile);
                while ((count = zipInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.close();
                zipInputStream.closeEntry();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (zipInputStream != null) zipInputStream.close();
                if (fileOutputStream != null) fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LogUtil.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            LogUtil.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LogUtil.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    public enum Type {
        SYNCED {
            @Override
            public String getPrefix(Context context) {
                return AccountManager.getAccountId(context) + URL_HYPHEN;
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

        /**
         * @deprecated User is always login, prefix video name with user id.
         */
        @Deprecated
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

    @StringDef({MOMENT_STORE_DIR, WORLD_STORE_DIR, THUMB_STORE_DIR})
    public @interface MediaDir {
    }
}
