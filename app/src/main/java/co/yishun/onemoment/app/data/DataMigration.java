package co.yishun.onemoment.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
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

    public DataMigration(Context mContext, boolean versionCheck) {
        this.mContext = mContext;
        if ((!versionCheck || checkVersion()) && migrateUserData()) {
            migrateAppMoment();
            migratePref();
        }
    }

    private boolean checkVersion() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        PackageInfo packageInfo;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            if (preferences.getInt(mContext.getString(R.string.pref_key_version), 0) < packageInfo.versionCode) {
                preferences.edit().putInt(mContext.getString(R.string.pref_key_version), packageInfo.versionCode).apply();
                return true;
            } else return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
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
                    return true;
                } else return false;
            } else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void migrateAppMoment() {
        File momentDir = FileUtil.getMediaStoreDir(mContext, FileUtil.MOMENT_STORE_DIR);
        File thumbDir = FileUtil.getMediaStoreDir(mContext, FileUtil.THUMB_STORE_DIR);
        LogUtil.d(TAG, momentDir.getPath());
        File[] oldThumbs = momentDir.listFiles((dir, filename) -> (filename.startsWith("LAT") || (filename.startsWith("MIT"))));
        for (File old : oldThumbs) {
            String oldName = old.getName();
            String newName = oldName.substring(0, oldName.indexOf("-") + 1)
                    + AccountManager.getUserInfo(mContext)._id
                    + oldName.substring(oldName.indexOf("-"));
            old.renameTo(new File(thumbDir, newName));
        }

        File[] oldMoments = momentDir.listFiles((dir, filename) -> filename.startsWith("LOC"));
        for (File moment : oldMoments) {
            String oldName = moment.getName();
            String newName = AccountManager.getUserInfo(mContext)._id
                    + oldName.substring(oldName.indexOf("-"));
            moment.renameTo(new File(momentDir, newName));
        }
    }

    private void migratePref() {
    }
}
