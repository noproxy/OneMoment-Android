package co.yishun.onemoment.app.account.sync.moment;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.qiniu.android.storage.UploadManager;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Link;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.model.OMDataBase;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import co.yishun.onemoment.app.function.Callback;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created on 2015/12/22.
 */
public class TagsUpdateTask implements Runnable {
    public static final int MSG_REALM_TAG_LOCAL = 0x1;
    public static final int MSG_REALM_TAG_REMOTE = 0x2;
    public static final int MSG_QUIT = 0x4;
    private static final String TAG = "TagsUpdateTask";
    private final Object mReadyFence = new Object();
    private final Context mContext;
    private final Callback mOnFail;
    private final Callback mOnSuccess;
    private final File remoteRealmFolder;
    private final String remoteRealmFileName = "remoteRealm.realm";
    private Realm mLocalRealm;
    private Realm mRemoteRealm;
    private boolean mReady;
    private boolean mRemoteExist = false;
    private volatile TagsUpdateHandler mHandler;

    public TagsUpdateTask(Context context, Callback onFail, Callback onSuccess) {
        new Thread(this, "TagsUpdateTask").start();
        this.mContext = context;
        this.mOnFail = onFail;
        this.mOnSuccess = onSuccess;
        remoteRealmFolder = FileUtil.getCacheDirectory(context, false);

        //make sure the looper is ready to receive message
        while (!mReady) {
        }
        LogUtil.d(TAG, "ready");
    }

    @Override public void run() {
        Looper.prepare();
        synchronized (mReadyFence) {
            mHandler = new TagsUpdateHandler(this);
            mReady = true;
            mReadyFence.notify();
        }

        Account account = OneMomentV3.createAdapter().create(Account.class);
        Link link = account.getTagUrl(AccountManager.getUserInfo(mContext)._id);
        File remoteRealmFile = FileUtil.getCacheFile(mContext, remoteRealmFileName);

        //if there is no realm on server, don't handle any message
        mRemoteExist = !TextUtils.isEmpty(link.link) && downloadRealm(link.link, remoteRealmFile);

        initRealm();
        if (mRemoteExist) {
            LogUtil.i(TAG, "download success");
            Looper.loop();
        } else {
            LogUtil.i(TAG, "download fail");
            Looper looper = Looper.myLooper();
            if (looper != null) looper.quit();
        }

        LogUtil.d(TAG, "TagUpdate thread exiting");
        synchronized (mReadyFence) {
            mReady = false;
            mHandler = null;
        }

        restoreRealm();
        uploadRealm();
    }

    private void initRealm() {
        mLocalRealm = Realm.getInstance(new RealmConfiguration.Builder(mContext)
                .name("tag-" + AccountManager.getUserInfo(mContext)._id + ".realm").build());

        OMDataBase localDatabase = mLocalRealm.where(OMDataBase.class).findFirst();
        if (localDatabase == null) {
            localDatabase = mLocalRealm.createObject(OMDataBase.class);
            String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault())
                    .format(new Date()) + "-" + Util.unixTimeStamp();
            localDatabase.setCreateTime(time);
            localDatabase.setUpdateTime(time);
        }

        if (mRemoteExist) {
            mRemoteRealm = Realm.getInstance(new RealmConfiguration.Builder(remoteRealmFolder)
                    .name(remoteRealmFileName).build());

            long localCreate = Long.parseLong(localDatabase.getCreateTime().substring(9));
            long remoteCreate = Long.parseLong(mRemoteRealm.where(OMDataBase.class).findFirst().getCreateTime().substring(9));
            if (localCreate > remoteCreate) {
                mLocalRealm.beginTransaction();
                mLocalRealm.where(OMDataBase.class).findFirst().setCreateTime(
                        mRemoteRealm.where(OMDataBase.class).findFirst().getCreateTime());
                mLocalRealm.commitTransaction();
            }
        }
    }

    private void restoreRealm() {
        if (mRemoteExist) {
            long localUpdate = Long.parseLong(mLocalRealm.where(OMDataBase.class).findFirst().getUpdateTime().substring(9));
            long remoteUpdate = Long.parseLong(mRemoteRealm.where(OMDataBase.class).findFirst().getUpdateTime().substring(9));
            if (remoteUpdate > localUpdate) {
                mLocalRealm.beginTransaction();
                mLocalRealm.where(OMDataBase.class).findFirst().setUpdateTime(
                        mRemoteRealm.where(OMDataBase.class).findFirst().getUpdateTime());
                mLocalRealm.commitTransaction();
            }
            File fileToDelete[] = remoteRealmFolder.listFiles((dir, filename) -> filename.contains(remoteRealmFileName));
            for (File file : fileToDelete){
                file.delete();
            }
        }
    }

    private boolean downloadRealm(String link, File remoteRealmFile) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(link).get().build();
        LogUtil.i(TAG, "download realm: " + request.urlString());

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            LogUtil.e(TAG, "exception when http call or close the stream", e);
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
                out = new FileOutputStream(remoteRealmFile);

                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    out.write(data, 0, count);

                    if (Thread.interrupted()) {
                        LogUtil.i(TAG, "cancel download");// canceled task not failTask++
                        mOnFail.call();
                        return false;
                    }
                    int progress = (int) (total * 100 / target);
                    LogUtil.i(TAG, "tag file " + progress);
                }
                out.flush();
                out.close();
                inputStream.close();
                mOnSuccess.call();
                return true;
            } else {
                mOnFail.call();
                return false;
            }
        } catch (IOException e) {
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

    private boolean uploadRealm() {
        File realmFile = new File(mLocalRealm.getConfiguration().getRealmFolder(),
                mLocalRealm.getConfiguration().getRealmFileName());

        LogUtil.d(TAG, "upload " + realmFile.getName());
        UploadToken token = OneMomentV3.createAdapter().create(Misc.class)
                .getUploadToken(realmFile.getName(), "tag");
        if (token.code <= 0) {
            LogUtil.e(TAG, "get upload token error: " + token.msg);
            mOnFail.call();
            return false;
        }

        UploadManager uploadManager = new UploadManager();
        uploadManager.put(realmFile, realmFile.getName(), token.token,
                (s, responseInfo, jsonObject) -> {
                    LogUtil.i(TAG, responseInfo.toString());
                    if (responseInfo.isOK()) {
                        LogUtil.d(TAG, "loaded " + responseInfo.path);
                        LogUtil.i(TAG, "profile upload ok");
                    } else {
                        LogUtil.e(TAG, "profile upload error: " + responseInfo.error);
                    }
                }, null
        );
        return true;
    }

    public void useRemoteTags(String date) {
        synchronized (mReadyFence) {
            if (!mReady) {
                LogUtil.i(TAG, " not ready error");
                return;
            }
        }
        LogUtil.i(TAG, "try to send MSG_REALM_TAG_REMOTE");
        mHandler.sendMessage(mHandler.obtainMessage(MSG_REALM_TAG_REMOTE, date));
    }

    public void stopUpdateTags() {
        synchronized (mReadyFence) {
            if (!mReady) {
                LogUtil.i(TAG, " not ready error");
                return;
            }
        }
        LogUtil.i(TAG, "try to send MSG_QUIT");
        mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
    }

    private void handleUseRemoteTags(String date) {
        LogUtil.d(TAG, "let's update tags of " + date);
        if (mRemoteExist) {
            mLocalRealm.beginTransaction();
            mLocalRealm.where(OMLocalVideoTag.class).equalTo("tagDate", date).findAll().clear();
            mLocalRealm.copyToRealm(mRemoteRealm.where(OMLocalVideoTag.class).equalTo("tagDate", date).findAll());
            mLocalRealm.commitTransaction();
        }
    }

    private static class TagsUpdateHandler extends Handler {
        private WeakReference<TagsUpdateTask> mWeakTask;

        public TagsUpdateHandler(TagsUpdateTask tagsUpdateTask) {
            this.mWeakTask = new WeakReference<>(tagsUpdateTask);
        }

        @Override public void handleMessage(Message msg) {
            int what = msg.what;
            Object obj = msg.obj;
            switch (what) {
                case MSG_REALM_TAG_LOCAL:
                    break;
                case MSG_REALM_TAG_REMOTE:
                    LogUtil.i(TAG, "MSG_REALM_TAG_REMOTE");
                    mWeakTask.get().handleUseRemoteTags((String) obj);
                    break;
                case MSG_QUIT:
                    LogUtil.i(TAG, "MSG_QUIT");
                    Looper looper = Looper.myLooper();
                    if (looper != null) {
                        looper.quit();
                    }
                    break;
                default:
                    throw new RuntimeException("Unhandled msg what=" + what);
            }
        }
    }
}
