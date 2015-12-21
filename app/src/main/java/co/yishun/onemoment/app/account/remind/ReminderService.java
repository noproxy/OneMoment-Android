package co.yishun.onemoment.app.account.remind;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;
import java.util.Date;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.SplashActivity_;

/**
 * Created by Jinge on 2015/12/16.
 */
public class ReminderService extends Service {
    private static final int NOTIFICATION_ID = 1004;
    private static final String TAG = "ReminderService";
    private static final String CONTENT[] = new String[]{
            "为了强迫症，可别忘了拍今天的一瞬！",
            "就现在，让我占有你1.2秒吧(＞﹏＜)",
            "1.2秒而已，你不点开我跟你急哦！￣へ￣",
            "据说连续拍摄10000天能召唤神龙，今天别忘了哦<(￣▽￣)>",
            "告诉你个秘密，用一瞬为女/男神制作长视频的表白成功率为100%！",
            "你忍心这么无脑的拍掉今天的一瞬么？！╮(╯﹏╰）╭",
            "今天就没有那么一瞬值得被记录么？",
            "神说，永远别想重来昨天的一瞬，所以今天也要记录下来哦！"
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getPackageName());
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        Intent startApp = new Intent();
        startApp.setComponent(new ComponentName(this, SplashActivity_.class));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, startApp, 0);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean hasRingtone = preferences.contains(getString(R.string.pref_key_remind_ringtone));
        Uri ringtoneUri;
        if (hasRingtone)
            ringtoneUri = Uri.parse(preferences.getString(getString(R.string.pref_key_remind_ringtone), ""));
        else
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        boolean vibrate = preferences.getBoolean(getString(R.string.pref_key_remind_vibrate), true);

        int contentIndex = (int) (Math.random() * CONTENT.length);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this).extend(new NotificationCompat.CarExtender())
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(CONTENT[contentIndex])
                        .setContentIntent(pendingIntent)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(R.mipmap.ic_launcher_circual)
                        .setTicker(CONTENT[contentIndex])
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(CONTENT[contentIndex]))
                        .setSound(ringtoneUri)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .setAutoCancel(true);
        if (vibrate) mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String time = preferences.getString(getString(R.string.pref_key_remind_time),
                getString(R.string.pref_summary_remind_time_default));
        String[] timeStrings = time.split(":");
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY) - Integer.valueOf(timeStrings[0]);
        int minute = now.get(Calendar.MINUTE) - Integer.valueOf(timeStrings[1]);

        long lastRemindTime = preferences.getLong("last_remind", 0);
        boolean shouldNotify = false;
        if (hour * 60 + minute < 3) {
            shouldNotify = true;
        }
        if (lastRemindTime > 0) {
            Calendar lastRemind = Calendar.getInstance();
            lastRemind.setTime(new Date(lastRemindTime));

            if (lastRemind.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
                long timeDiff = now.getTimeInMillis() - lastRemindTime;
                if (timeDiff < 180000) shouldNotify = false;
                else if (hour * 60 + minute < 3) {
                    shouldNotify = true;
                }
            }
        }
        if (shouldNotify) {
            mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
            preferences.edit().putLong("last_remind", now.getTimeInMillis()).apply();
        }

        return super.onStartCommand(intent, flags, startId);
    }

}
