package co.yishun.onemoment.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.net.result.AccountResult;

/**
 * Created by Jinge on 2016/1/9.
 */
public class DataMigration {
    private static final String TAG = "DataMigration";
    /* version 1.x
    app_identity:
        info {co.yishun.onemoment.app.net.result.AccountHelper$Data}
        tmp.lock
    app_moment:
        login : {user_id}-{date}-{timestamp}.mp4
        else : LOC-{date}-{timestamp}.mp4
        LAT-{date}-{timestamp}.png
        MIT-{date}-{timestamp}.png
    database:
        OneDataBase.db
    shared_prefs:
        com_onemoment_weibo.xml:
            access_token
            expires_in
            uid
        preferences.xml:
            is_first_launch
        ui.AlbumActivity_.xml:
            is_wifi_sync
            is_auto_sync
     */
    /* version 2.0
    app_identity:
        info {co.yishun.onemoment.app.api.model.User} (need)
    app_moment:
        {user_id}-{date}-{timestamp}.mp4
    app_thumb:
        LAT-{user_id}-{date}-{timestamp}.png (need)
        MIT-{user_id}-{date}-{timestamp}.png (need)
    database:
        OneDataBase.db
    shared_prefs:
        com_onemoment_weibo.xml:
            access_token
            expires_in
            uid
        preference.xml -> run_preferences.xml:
            is_first_launch
        {default_preference}:
            remind_everyday_vibrate
            sync_frequency
            sync_cellular_data
            last_remind
            remind_everyday
            sync
            remind_everyday_ringtone
     */

    private Context mContext;
    private Dao<Moment, Integer> momentDao;

    private DataMigration(Context mContext) throws SQLException {
        this.mContext = mContext;
        momentDao = OpenHelperManager.getHelper(mContext, MomentDatabaseHelper.class).getDao(Moment.class);
    }

    public static void dataInit(Context context) {
        try {
            DataMigration dataMigration = new DataMigration(context);
            if (dataMigration.checkVersion()) {
                dataMigration.migrateVersion();
                dataMigration.migrateUserData();
                dataMigration.migrateThumbs();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasLOCMoments(Context context) {
        try {
            DataMigration dataMigration = new DataMigration(context);
            return dataMigration.hasLOCMoment();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void moveLOCMomentsToUser(Context context) {
        try {
            DataMigration dataMigration = new DataMigration(context);
            dataMigration.moveLOCMomentsToOwner(AccountManager.getUserInfo(context)._id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean checkVersion() {
        LogUtil.e(TAG, "check version");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return preferences.getInt(mContext.getString(R.string.pref_key_version), 0) < 10;
    }

    private void migrateVersion() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        PackageInfo packageInfo;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            preferences.edit().putInt(mContext.getString(R.string.pref_key_version), packageInfo.versionCode).apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean migrateUserData() {
        try {
            File userInfoFile = new File(mContext.getDir(Constants.IDENTITY_DIR, Context.MODE_PRIVATE)
                    + "/" + Constants.IDENTITY_INFO_FILE_NAME);
            LogUtil.d(TAG, userInfoFile.getPath());
            if (userInfoFile.length() > 0) {
                FileInputStream fileInputStream = new FileInputStream(userInfoFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                Object o = objectInputStream.readObject();
                if (o instanceof AccountResult.Data) {
                    AccountResult.Data data = (AccountResult.Data) o;
                    User user = new User();
                    user._id = data.get_id();
                    user.nickname = data.getNickname();
                    user.gender = Account.Gender.format(data.getGender());
                    user.location = data.getArea();
                    user.avatarUrl = data.getAvatar_url();
                    AccountManager.saveAccount(mContext, user);
                    AccountManager.deleteOldAccount(mContext, user);
                    return true;
                } else return false;
            } else return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void migrateThumbs() {
        File thumbDir = FileUtil.getMediaStoreDir(mContext, FileUtil.THUMB_STORE_DIR);
        try {
            List<Moment> localMoments = momentDao.queryForAll();
            for (Moment m : localMoments) {
                String ownerId = m.getOwnerID();

                File oldLargeThumb = m.getLargeThumbPathFile();
                String oldLargeThumbName = oldLargeThumb.getName();
                String newLargeThumbName = oldLargeThumbName.substring(0, oldLargeThumbName.indexOf("-") + 1)
                        + ownerId + oldLargeThumbName.substring(oldLargeThumbName.indexOf("-"));
                File newLargeThumb = new File(thumbDir, newLargeThumbName);
                oldLargeThumb.renameTo(newLargeThumb);
                m.setLargeThumbPath(newLargeThumb.getPath());

                File oldSmallThumb = m.getThumbPathFile();
                String oldSmallThumbName = oldSmallThumb.getName();
                String newSmallThumbName = oldSmallThumbName.substring(0, oldSmallThumbName.indexOf("-") + 1)
                        + ownerId + oldSmallThumbName.substring(oldSmallThumbName.indexOf("-"));
                File newSmallThumb = new File(thumbDir, newSmallThumbName);
                oldSmallThumb.renameTo(newSmallThumb);
                m.setThumbPath(newSmallThumb.getPath());

                momentDao.createOrUpdate(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean hasLOCMoment() {
        boolean check = false;
        try {
            List<Moment> momentsNoUser = momentDao.queryBuilder().where().eq("owner", "LOC").query();
            check = momentsNoUser.size() != 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }

    private void moveLOCMomentsToOwner(String userId) {
        File thumbDir = FileUtil.getMediaStoreDir(mContext, FileUtil.THUMB_STORE_DIR);
        try {
            List<Moment> localMoments = momentDao.queryForAll();
            for (Moment m : localMoments) {
                if (TextUtils.equals(m.getOwnerID(), "LOC")) {
                    File oldMoment = m.getFile();
                    String oldMomentName = oldMoment.getName();
                    String newMomentName = oldMomentName.replace("LOC", userId);
                    File newMoment = new File(oldMoment.getParent(), newMomentName);
                    oldMoment.renameTo(newMoment);
                    m.setPath(newMoment.getPath());

                    File oldLargeThumb = m.getLargeThumbPathFile();
                    String oldLargeThumbName = oldLargeThumb.getName();
                    String newLargeThumbName = oldLargeThumbName.replace("LOC", userId);
                    File newLargeThumb = new File(thumbDir, newLargeThumbName);
                    oldLargeThumb.renameTo(newLargeThumb);
                    m.setLargeThumbPath(newLargeThumb.getPath());

                    File oldSmallThumb = m.getThumbPathFile();
                    String oldSmallThumbName = oldSmallThumb.getName();
                    String newSmallThumbName = oldSmallThumbName.replace("LOC", userId);
                    File newSmallThumb = new File(thumbDir, newSmallThumbName);
                    oldSmallThumb.renameTo(newSmallThumb);
                    m.setThumbPath(newSmallThumb.getPath());

                    m.setOwner(userId);
                    momentDao.createOrUpdate(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
