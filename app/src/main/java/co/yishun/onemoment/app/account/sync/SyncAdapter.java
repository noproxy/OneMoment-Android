package co.yishun.onemoment.app.account.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.j256.ormlite.dao.Dao;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.utils.Etag;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.SystemService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ApiMoment;
import co.yishun.onemoment.app.api.model.Domain;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.MomentLock;
import co.yishun.onemoment.app.data.VideoUtil;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import retrofit.RestAdapter;


/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 * <p>
 * Created by Carlos on 3/10/15.
 */
@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    private final static UploadManager mUploadManager = new UploadManager();
    private final static Misc mMiscService = OneMomentV3.createAdapter().create(Misc.class);

    private final List<Moment> toUpload = new ArrayList<>();
    private final List<Pair<ApiMoment, Moment>> toDownload = new ArrayList<>();
    private final List<ApiMoment> toDelete = new ArrayList<>();
    @SystemService ConnectivityManager connectivityManager;
    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> dao;
    private volatile int allTask = 0;
    private volatile int successTask = 0;
    private volatile int failTask = 0;

    public SyncAdapter(Context context) {
        super(context, true);
    }

    /**
     * convert array to HashMap
     * <p>
     * No generics is from Apache ArrayUtils, and generics version is from <a href="http://stackoverflow.com/questions/6416346/adding-generics-to-arrayutils-tomap">Stack Overflow</a>
     *
     * @param array
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> toMap(Object[] array) {
        if (array == null) {
            return null;
        }

        final Map<K, V> map = new HashMap<K, V>((int) (array.length * 1.5));
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            if (object instanceof Map.Entry) {
                Map.Entry<K, V> entry = (Map.Entry<K, V>) object;
                map.put(entry.getKey(), entry.getValue());
            } else if (object instanceof Object[]) {
                Object[] entry = (Object[]) object;
                if (entry.length < 2) {
                    throw new IllegalArgumentException("Array element " + i
                            + ", '" + object + "', has a length less than 2");
                }
                map.put((K) entry[0], (V) entry[1]);
            } else {
                throw new IllegalArgumentException("Array element " + i + ", '"
                        + object + "', is neither of type Map.Entry nor an Array");
            }
        }
        return map;
    }

    /**
     * To execute sync. When sync end, it will call {@link #onSyncEnd()} to send broadcast notify sync process ending. And in syncing process, it calls {@link #onSyncUpdate(int, int, int)} to broadcast progress.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "onPerformSync, account: " + account.name + ", Bundle: " + extras);
        if (!checkSyncOption(extras))
            return;

        RestAdapter restAdapter = OneMomentV3.createAdapter();
        co.yishun.onemoment.app.api.Account service = restAdapter.create(co.yishun.onemoment.app.api.Account.class);
        final List<ApiMoment> momentsOnServer = service.getVideoList(AccountManager.getAccountId(getContext()));
        Collections.sort(momentsOnServer);
        List<Moment> momentsOnDevice = null;
        try {
            momentsOnDevice = dao.queryBuilder().where().eq("owner", AccountManager.getAccountId(getContext())).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        divideTask(momentsOnServer, momentsOnDevice);
        digestTask();

    }

    /**
     * check sync option.
     *
     * @param extras Bundle of sync.
     * @return false if sync should give up.
     */
    private boolean checkSyncOption(Bundle extras) {
        if (!extras.getBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, false) &&
                !extras.getBoolean(SyncManager.SYNC_IGNORE_NETWORK, false) &&
                !connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            Log.i(TAG, "cancel sync because network is not permitted");
            return false;
        }
        Log.i(TAG, "sync option is OK");
        return true;
    }

    private void divideTask(List<ApiMoment> momentsOnServer, List<Moment> momentsOnDevice) {
        Log.i(TAG, "divide task, servers: " + momentsOnServer.size());
        Log.i(TAG, "divide task, local: " + momentsOnDevice.size());

        final HashMap<String, ApiMoment> apiMomentHashMap = new HashMap<>(momentsOnServer.size());
        for (ApiMoment apiMoment : momentsOnServer)
            apiMomentHashMap.put(apiMoment.getTime(), apiMoment);

        for (Moment moment : momentsOnDevice) {
            ApiMoment momentOnServer = apiMomentHashMap.get(moment.getTime());
            if (momentOnServer == null) {
                toUpload.add(moment);
            } else {
                if (moment.getUnixTimeStamp() > momentOnServer.getUnixTimeStamp()) {
                    toUpload.add(moment);
                    toDelete.add(momentOnServer);
                } else if (moment.getUnixTimeStamp() < momentOnServer.getUnixTimeStamp()) {
                    toDownload.add(Pair.create(momentOnServer, moment));
                } else if (!isFileHashSame(new File(moment.getPath()), momentOnServer)) {
                    toDownload.add(Pair.create(momentOnServer, moment));
                }
                apiMomentHashMap.remove(moment.getTime());
            }
        }

        for (ApiMoment apiMoment : apiMomentHashMap.values()) {
            toDownload.add(Pair.create(apiMoment, null));
        }

        Log.i(TAG, "divide end.");
        Log.i(TAG, "to upload: " + toUpload.size());
        Log.i(TAG, "to download: " + toDownload.size());
        Log.i(TAG, "to delete: " + toDelete.size());
    }

    private void cleanFile() {
    }

    private void digestTask() {
        ExecutorService executor = Executors.newCachedThreadPool();

        allTask = toDownload.size() + toUpload.size();
        failTask = 0;
        successTask = 0;
        onSyncStart();

        for (Moment moment : toUpload) {
            executor.submit(new UploadTask(moment));
        }

        for (Pair<ApiMoment, Moment> pair : toDownload) {
            executor.submit(new DownloadTask(pair.first, pair.second));
        }

        for (ApiMoment apiMoment : toDelete)
            executor.submit(new DeleteTask(apiMoment));

        try {
            Log.i(TAG, "Wait Sync Task end");
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Log.i(TAG, "executor is interrupted!");
            executor.shutdownNow();
        }
        onSyncEnd();
        cleanFile();
    }

    /**
     * send broadcast to notify syncing progress update.
     * <p>
     * progress is from 0 to 100.
     * <p>
     * {@link SyncManager#PROGRESS_NOT_AVAILABLE} means no progress data available.
     * {@link SyncManager#PROGRESS_ERROR} means error occurred.
     */
    private void onSyncUpdate() {
        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_PROGRESS);
        int progress = successTask * 100 / allTask;
        intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_PROGRESS_VALUE, progress);
        Log.i(TAG, "sync update progress, send a broadcast. progress: " + progress);
        getContext().sendBroadcast(intent);
    }

    /**
     * Sync end, to check result and send broadcast.
     */

    private void onSyncEnd() {
        Log.i(TAG, "sync end");
        if (allTask == 0)
            return;
        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_END);
        @SyncManager.EndResult int result;
        if (successTask + failTask < allTask)
            result = SyncManager.SYNC_BROADCAST_EXTRA_END_RESULT_CANCEL;
        else if (successTask == allTask) {
            result = SyncManager.SYNC_BROADCAST_EXTRA_END_RESULT_SUCCESS;
        } else {
            result = SyncManager.SYNC_BROADCAST_EXTRA_END_RESULT_FAIL;
        }
        intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_END_RESULT, result);
        getContext().sendBroadcast(intent);
    }

    private void onSyncStart() {
        Log.i(TAG, "sync start");
        if (allTask == 0)
            return;
        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_START);
        intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_START_TASK_NUM, allTask);
        getContext().sendBroadcast(intent);
    }

    private UploadManager getUploadManager() {
        return mUploadManager;
    }

    private boolean isFileHashSame(@NonNull File file, @NonNull ApiMoment apiMoment) {
        boolean isSame = false;
        try {
            isSame = Etag.file(file).equals(apiMoment.hash);
        } catch (IOException e) {
            isSame = false;
            Log.e(TAG, "exception when hash the fileSynced", e);
        }
        return isSame;
    }

    private class UploadTask implements Runnable {


        private final Moment mMoment;

        public UploadTask(Moment moment) {
            mMoment = moment;
        }


        @Override public void run() {
            Log.i(TAG, "upload a moment: " + mMoment);
            String qiNiuKey = mMoment.getKey();
            UploadToken token = mMiscService.getUploadToken(mMoment.getKey());
            if (!token.isSuccess()) {
                Log.e(TAG, "upload failed when get token");
                failTask++;
                onSyncUpdate();
                return;
            }
            getUploadManager().put(mMoment.getPath(), qiNiuKey, token.token, newHandler(), newOptions());
        }

        private UploadOptions newOptions() {
            return new UploadOptions(null, Constants.MIME_TYPE, true, null, null);
        }

        private UpCompletionHandler newHandler() {
            return (key, responseInfo, response) -> {
                Log.i(TAG, responseInfo.toString());
                if (responseInfo.isOK()) {
                    Log.i(TAG, "a moment upload ok: " + mMoment);
                    successTask++;
                } else {
                    Log.i(TAG, "a moment upload failed: " + mMoment);
                    failTask++;
                }
                onSyncUpdate();
            };
        }
    }

    private class DownloadTask implements Runnable {
        private final ApiMoment mApiMoment;
        private Moment mMoment;

        private DownloadTask(@NonNull ApiMoment mApiMoment, @Nullable Moment moment) {
            this.mApiMoment = mApiMoment;
            mMoment = moment;
        }

        @Override public void run() {
            Log.i(TAG, "download a moment: " + mApiMoment);
            Domain domain = mMiscService.getResourceDomain("video");
            if (!domain.isSuccess()) {
                Log.e(TAG, "download failed when get resource domain");
                failTask++;
                onSyncUpdate();
                return;
            }


            File fileSynced = FileUtil.getMomentStoreFile(getContext());
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(domain.domain + mApiMoment.getKey()).get().build();
            Log.i(TAG, "start download: " + request.urlString());

            if (!(fileSynced.exists() && isFileHashSame(fileSynced, mApiMoment))) {
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    Log.e(TAG, "exception when http call or close the stream", e);
                    failTask++;
                    onSyncUpdate();
                    return;
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
                        out = new FileOutputStream(fileSynced);

                        while ((count = inputStream.read(data)) != -1) {
                            total += count;
                            out.write(data, 0, count);

                            Log.v(TAG, "progress: " + (total / target));
                            if (Thread.interrupted()) {
                                Log.i(TAG, "cancel download");// canceled task not failTask++
                                return;
                            }
                        }
                        out.flush();
                        out.close();
                        inputStream.close();


                    }
                } catch (IOException e) {
                    failTask++;
                    Log.e(TAG, "download failed", e);
                    onSyncUpdate();
                    return;
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

            try {
                String pathToThumb = VideoUtil.createThumbImage(getContext(), mApiMoment, fileSynced.getPath());
                String pathToLargeThumb = VideoUtil.createLargeThumbImage(getContext(), mApiMoment, fileSynced.getPath());
                if (mMoment == null)
                    mMoment = new Moment();
                else {
                    MomentLock.lockMoment(getContext(), mMoment);
                    //TODO I don't delete old. To clean them by adding clean cache function or delete them here.
                }


                mMoment.setOwner(mApiMoment.getOwnerID());
                mMoment.setTime(mApiMoment.getTime());
                mMoment.setTimeStamp(mApiMoment.getUnixTimeStamp());
                mMoment.setPath(fileSynced.getPath());
                mMoment.setThumbPath(pathToThumb);
                mMoment.setLargeThumbPath(pathToLargeThumb);
                dao.createOrUpdate(mMoment);
                Log.i(TAG, "download ok: " + mMoment);

                MomentLock.unlockMomentIfLocked(getContext(), mMoment);

                successTask++;
                onSyncUpdate();
            } catch (SQLException e) {
                failTask++;
                onSyncUpdate();
                Log.e(TAG, "exception when save a moment into database", e);
            } catch (IOException e) {
                failTask++;
                onSyncUpdate();
                Log.e(TAG, "exception when create thumb image", e);
            } catch (Exception e) {
                failTask++;
                onSyncUpdate();
                Log.e(TAG, "unknown exception,may be thrown in MomentLock", e);
            }
        }
    }

    private class DeleteTask implements Runnable {
        private final ApiMoment mApiMoment;

        private DeleteTask(ApiMoment mApiMoment) {
            this.mApiMoment = mApiMoment;
        }


        @Override public void run() {
            Log.i(TAG, "delete a video: " + mApiMoment);
            mMiscService.deleteVideo(mApiMoment.getKey());
            Log.i(TAG, "delete a video ok: " + mApiMoment);
        }
    }
}
