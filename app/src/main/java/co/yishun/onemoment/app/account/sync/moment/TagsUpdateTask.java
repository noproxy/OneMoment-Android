package co.yishun.onemoment.app.account.sync.moment;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Link;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.model.OMDataBase;
import co.yishun.onemoment.app.function.Callback;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created on 2015/12/22.
 */
public class TagsUpdateTask implements Runnable {
    private static final String TAG = "TagsUpdateTask";
    private final Context mContext;
    private final Callback mOnFail;
    private final Callback mOnSuccess;
    private final String remoteRealmFileName = "remoteRealm.realm";
    private final String mergedRealmFileName = "mergedRealm.realm";

    public TagsUpdateTask(Context context, Callback onFail, Callback onSuccess) {
        this.mContext = context;
        this.mOnFail = onFail;
        this.mOnSuccess = onSuccess;
    }

    @Override public void run() {
        Account account = OneMomentV3.createAdapter().create(Account.class);
        Link link = account.getTagUrl(AccountManager.getUserInfo(mContext)._id);
        File remoteRealmFile = FileUtil.getCacheFile(mContext, remoteRealmFileName);


    }

    private boolean downloadRealm(String link, File remoteRealmFile) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(link).get().build();
        Log.i(TAG, "download realm: " + request.urlString());

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
                out = new FileOutputStream(remoteRealmFile);

                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    out.write(data, 0, count);

                    if (Thread.interrupted()) {
                        Log.i(TAG, "cancel download");// canceled task not failTask++
                        mOnFail.call();
                        return false;
                    }
                    int progress = (int) (total * 100 / target);
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

    private boolean mergeRealm(File remoteRealmFile) {
        Realm localRealm = Realm.getInstance(new RealmConfiguration.Builder(mContext)
                .name("tag-" + AccountManager.getUserInfo(mContext)._id + ".realm").build());

        Realm remoteRealm = Realm.getInstance(new RealmConfiguration
                .Builder(FileUtil.getCacheDirectory(mContext, false))
                .name(remoteRealmFileName).build());

        Realm mergedRealm = Realm.getInstance(new RealmConfiguration
                .Builder(FileUtil.getCacheDirectory(mContext, false))
                .name(mergedRealmFileName).build());

        long localUpdate = Long.parseLong(localRealm.where(OMDataBase.class).findFirst().getUpdateTime().substring(9));
        long remoteUpdate = Long.parseLong(remoteRealm.where(OMDataBase.class).findFirst().getUpdateTime().substring(9));

        return false;
    }
}
