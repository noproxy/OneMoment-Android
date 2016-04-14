package co.yishun.onemoment.app.account.sync.moment;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Pair;

import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.OrmLiteDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ApiMoment;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import retrofit.RestAdapter;

/**
 * Created by Carlos on 2015/12/20.
 */
@EBean
public class MomentSyncImpl extends MomentSync {
    private static final String TAG = "MomentSyncImpl";
    private final Context mContext;
    /**
     * Moments that only exists locally, and need to upload to server.
     */
    private final List<Moment> toUpload = new ArrayList<>();
    /**
     * ApiMoment that only exists on server, which need to create in local database. The
     * corresponded Moment is the local old moment in the database which need to replace, it can be
     * null if there is no corresponded Moment locally.
     */
    private final List<Pair<ApiMoment, Moment>> toAdd = new ArrayList<>();
    /**
     * Moment whose data is not integrated. The missing may be thumbnails or video file.
     */
    private final List<Moment> toFix = new ArrayList<>();
    /**
     * Out of date ApiMoment on server which need to delete.
     */
    private final List<ApiMoment> toDelete = new ArrayList<>();
    @OrmLiteDao(helper = MomentDatabaseHelper.class)
    Dao<Moment, Integer> dao;
    private ExecutorService executor;
    private volatile int allTask = 0;
    private volatile int successTask = 0;
    private volatile int failTask = 0;

    public MomentSyncImpl(Context context) {
        mContext = context;
    }

    /**
     * convert array to HashMap <p> No generics is from Apache ArrayUtils, and generics version is
     * from <a href="http://stackoverflow.com/questions/6416346/adding-generics-to-arrayutils-tomap">Stack
     * Overflow</a>
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

    @Override
    void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        RestAdapter restAdapter = OneMomentV3.createAdapter();
        co.yishun.onemoment.app.api.Account service = restAdapter.create(co.yishun.onemoment.app.api.Account.class);
        final List<ApiMoment> momentsOnServer = service.getVideoList(AccountManager.getAccountId(mContext));
        Collections.sort(momentsOnServer);// Ensure old ApiMoment is first. So when converted to HashMap, the older will be replaced by the newer, if there are multiple ApiMoments at same day.

        List<Moment> momentsOnDevice = new ArrayList<>();
        try {
            momentsOnDevice.addAll(dao.queryBuilder().where().eq("owner", AccountManager.getAccountId(mContext)).query());
        } catch (SQLException e) {
            e.printStackTrace();
        }


        divideTask(momentsOnServer, momentsOnDevice);


        digestTask();
        cleanFile();

    }

    private void divideTask(List<ApiMoment> momentsOnServer, List<Moment> momentsOnDevice) {
        LogUtil.i(TAG, "divide task, servers: " + momentsOnServer.size());
        LogUtil.i(TAG, "divide task, local: " + momentsOnDevice.size());
        TagsUpdateTask tagsUpdateTask = new TagsUpdateTask(mContext, () -> LogUtil.e(TAG, "tag fix fail"), () -> LogUtil.i(TAG, "tag fix success"));
        final HashMap<String, ApiMoment> apiMomentHashMap = new HashMap<>(momentsOnServer.size());
        for (ApiMoment apiMoment : momentsOnServer)
            apiMomentHashMap.put(apiMoment.getTime(), apiMoment);

        for (Moment moment : momentsOnDevice) {
            ApiMoment momentOnServer = apiMomentHashMap.get(moment.getTime());
            // toUpload or (toUpload+toDelete or toAdd)
            if (momentOnServer == null) {
                toUpload.add(moment);
            } else {
                if (Long.parseLong(moment.getUnixTimeStamp()) > Long.parseLong(momentOnServer.getUnixTimeStamp())) {
                    toUpload.add(moment);
                    toDelete.add(momentOnServer);
                } else if (Long.parseLong(moment.getUnixTimeStamp()) < Long.parseLong(momentOnServer.getUnixTimeStamp())) {
                    toAdd.add(Pair.create(momentOnServer, moment));
                    tagsUpdateTask.useRemoteTags(moment.getTime());
                }
                apiMomentHashMap.remove(moment.getTime());
            }
        }

        for (ApiMoment apiMoment : apiMomentHashMap.values()) {
            toAdd.add(Pair.create(apiMoment, null));
            tagsUpdateTask.useRemoteTags(apiMoment.getTime());
        }

        // add to database, add to check
        for (Pair<ApiMoment, Moment> pair : toAdd) {
            ApiMoment apiMoment = pair.first;
            Moment moment = pair.second;
            if (moment == null) moment = new Moment();

            moment.setOwner(apiMoment.getOwnerID());
            moment.setTime(apiMoment.getTime());
            moment.setTimeStamp(apiMoment.getUnixTimeStamp());
            moment.setPath(FileUtil.getMomentStoreFile(mContext, apiMoment).toString());
            moment.setThumbPath(FileUtil.getThumbnailStoreFile(mContext, apiMoment, FileUtil.Type.MICRO_THUMB).getPath());
            moment.setLargeThumbPath(FileUtil.getThumbnailStoreFile(mContext, apiMoment, FileUtil.Type.LARGE_THUMB).getPath());

            try {
                dao.createOrUpdate(moment);
                toFix.add(moment);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        boolean needFix;

        // check moment on devices
        for (Moment moment : momentsOnDevice) {

            try {
                needFix = !moment.getFile().exists()
                        || moment.getFile().length() == 0
                        || !moment.getThumbPathFile().exists()
                        || moment.getThumbPathFile().length() == 0
                        || !moment.getLargeThumbPathFile().exists()
                        || moment.getLargeThumbPathFile().length() == 0;
            } catch (NullPointerException ignored) {
                needFix = true;
            }

            if (needFix) {
                toFix.add(moment);
            }
        }

        tagsUpdateTask.stopUpdateTags();

        LogUtil.i(TAG, "divide end.");
        LogUtil.i(TAG, "to upload: " + toUpload.size());
        LogUtil.i(TAG, "to add: " + toAdd.size());
        LogUtil.i(TAG, "to download: " + toFix.size());
        LogUtil.i(TAG, "to delete: " + toDelete.size());
    }

    //TODO clean disk room, delete useless file
    private void cleanFile() {
    }

    private void digestTask() {
        executor = Executors.newSingleThreadExecutor();
//        allTask = toAdd.size() + toUpload.size();
//        failTask = 0;
//        successTask = 0;
//        onSyncStart();
        allTask =  toDelete.size() + toFix.size() + toUpload.size();
        failTask = 0;
        successTask = 0;


        for (Moment moment : toUpload) {
            executor.submit(new MomentUploadTask(moment, this::onFail, this::onSuccess));
        }

        for (ApiMoment apiMoment : toDelete)
            executor.submit(new VideoDeleteTask(apiMoment));

        for (Moment moment : toFix) {
            executor.submit(new MomentFixTask(moment, null, this::onFail, this::onSuccess, this::onProgress));
        }


        try {
            LogUtil.i(TAG, "Wait Sync Task end");
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LogUtil.i(TAG, "executor is interrupted!");
            executor.shutdownNow();
        }
    }

    private void onFail(Moment moment) {
        failTask++;
        onSyncMomentFail(moment);
    }

    private void onSuccess(Moment moment) {
        successTask++;
        onSyncMomentUpdate(moment);
    }

    private void onProgress(Moment moment, Integer progress) {

    }

    /**
     * send broadcast to notify syncing progress update. <p> progress is from 0 to 100. <p> {@link
     * SyncManager#PROGRESS_NOT_AVAILABLE} means no progress data available. {@link
     * SyncManager#PROGRESS_ERROR} means error occurred.
     */
    private void onSyncUpdate() {
        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_PROGRESS);
        int progress = successTask * 100 / allTask;
        intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_PROGRESS_VALUE, progress);
        LogUtil.i(TAG, "sync update progress, send a broadcast. progress: " + progress);
        mContext.sendBroadcast(intent);
    }

    /**
     * send broadcast to notify local moment changed at some date. If sync task create a new moment
     * or update existed moment, it will be called. Upload task won't cause local moment changes.
     */
    private void onSyncMomentUpdate(Moment moment) {
        String timestamp = moment.getUnixTimeStamp();
        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE);
        intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_LOCAL_UPDATE_TIMESTAMP, timestamp);
        intent.putExtra("allTask",allTask);
        intent.putExtra("finishedTask",successTask);
        LogUtil.i(TAG, "sync update local, send a broadcast. timestamp: " + timestamp);
        mContext.sendBroadcast(intent);
    }

    private void onSyncMomentFail(Moment moment){
        String timestamp = moment.getUnixTimeStamp();
        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_UPDATA_FAIL);
        intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_LOCAL_UPDATE_TIMESTAMP, timestamp);
        intent.putExtra("failedTask",failTask);
        LogUtil.i(TAG,"sync update fail,send a broadcast. timestamp: " + timestamp);
        mContext.sendBroadcast(intent);
    }
}
