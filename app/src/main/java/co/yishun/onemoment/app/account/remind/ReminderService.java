package co.yishun.onemoment.app.account.remind;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.SplashActivity_;

/**
 * Created by Jinge on 2015/12/16.
 */
public class ReminderService extends Service {
    private static final int NOTIFICATION_ID = 1004;
    private static final String TAG = "ReminderService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent startApp = new Intent(this, SplashActivity_.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, startApp, PendingIntent.FLAG_UPDATE_CURRENT);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean hasRingtone = preferences.contains(getString(R.string.pref_key_remind_ringtone));
        Uri ringtoneUri;
        if (hasRingtone)
            ringtoneUri = Uri.parse(preferences.getString(getString(R.string.pref_key_remind_ringtone), ""));
        else
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        boolean vibrate = preferences.getBoolean(getString(R.string.pref_key_remind_vibrate), true);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Hello" + System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setSound(ringtoneUri);
        if (vibrate) mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String time = preferences.getString(getString(R.string.pref_key_remind_time),
                getString(R.string.pref_summary_remind_time_default));
        String[] timeStrings = time.split(":");
        Calendar now = Calendar.getInstance();
        int hour = Integer.valueOf(timeStrings[0]) - now.get(Calendar.HOUR_OF_DAY);
        int minute = Integer.valueOf(timeStrings[1]) - now.get(Calendar.MINUTE);
        if (Math.abs(hour * 60 + minute) < 5)
            mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());

        return super.onStartCommand(intent, flags, startId);
    }

}
