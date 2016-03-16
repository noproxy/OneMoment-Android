package co.yishun.onemoment.app.account.sync.compat;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.OrmLiteDao;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;

import static co.yishun.onemoment.app.LogUtil.e;
import static co.yishun.onemoment.app.LogUtil.i;

/**
 * For onemoment v1.x.x. This class picks up old moment on user's phone that not sync with server.
 * Because v1.x.x give the chance that user shoots some videos without signing up. We cannot lost
 * these moments.
 * <p>
 * Created by carlos on 3/15/16.
 */
@EBean
public class OldMomentPickUpSync {
    private static final String TAG = "OldMomentPickUpSync";
    private final String id;
    private final Context mContext;
    private final File dir;
    @OrmLiteDao(helper = MomentDatabaseHelper.class)
    Dao<Moment, Integer> momentDao;

    public OldMomentPickUpSync(Context context) {
        mContext = context;
        this.id = AccountManager.getAccountId(context);
        dir = FileUtil.getMediaStoreDir(mContext, FileUtil.MOMENT_STORE_DIR);
    }

    public static void sync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        OldMomentPickUpSync_.getInstance_(context).sync();
    }

    void sync() {

        String[] oldMomentsName = dir.list((dir1, filename) -> filename.startsWith(FileUtil.Type.LOCAL.getPrefix(mContext)));

        for (String old : oldMomentsName) {
            String newName = getNewFileName(old);
            File oldFile = new File(dir, old);
            File newFile = new File(dir, newName);
            if (oldFile.renameTo(newFile)) {
                //noinspection ResultOfMethodCallIgnored
                oldFile.delete();

                Moment moment = new Moment.MomentBuilder(mContext).fromCompatFile(newFile).build();

                String videoPath = moment.getPath();
                try {
                    File largeThumb =
                            FileUtil.getThumbnailStoreFile(mContext, moment, FileUtil.Type.LARGE_THUMB);
                    File smallThumb =
                            FileUtil.getThumbnailStoreFile(mContext, moment, FileUtil.Type.MICRO_THUMB);
                    VideoUtil.createThumbs(videoPath, largeThumb, smallThumb);
                    moment.setLargeThumbPath(largeThumb.getPath());
                    moment.setThumbPath(smallThumb.getPath());

                    if (1 == momentDao.create(moment)) {
                        i(TAG, "new moment: " + moment);

                        //TODO send broadcast whenever local moment changed, those lines code copy from MomentSyncImpl
                        String timestamp = moment.getUnixTimeStamp();
                        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE);
                        intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_LOCAL_UPDATE_TIMESTAMP, timestamp);
                        LogUtil.i(TAG, "create new moment, send a broadcast. timestamp: " + timestamp);
                        mContext.sendBroadcast(intent);

                        return;
                    }
                } catch (IOException e) {
                    e(TAG, "create thumb failed for old moment");
                    e.printStackTrace();
                } catch (SQLException e) {
                    LogUtil.e(TAG, "failed to save old moment", e);
                    e.printStackTrace();
                }
            }
        }
    }


    private String getNewFileName(String oldName) {
        int start = oldName.indexOf('-');
        return id + oldName.substring(start);
    }

}
