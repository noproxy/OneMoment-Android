package co.yishun.onemoment.app.account.remind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2015/12/16.
 */
public class ReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_UPDATE_REMIND = "co.yishun.onemoment.app.remind.update";
    private static final String TAG = "ReminderReceiver";
    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(TAG, "receive broadcast");
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(context.getString(R.string.pref_key_remind_everyday), true)) {
            resetDailyRemainder(context);
        } else {
            cancelDailyReminder(context);
        }
    }

    public void setDailyReminder(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0,
                new Intent(context, ReminderService.class), PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        alarmManager.cancel(alarmIntent);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public void resetDailyRemainder(Context context) {
        String time = preferences.getString(context.getString(R.string.pref_key_remind_time),
                context.getString(R.string.pref_summary_remind_time_default));
        String[] timeStrings = time.split(":");
        setDailyReminder(context, Integer.valueOf(timeStrings[0]), Integer.valueOf(timeStrings[1]));
    }

    private void cancelDailyReminder(Context context) {
        LogUtil.d(TAG, "cancel daily remind");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0,
                new Intent(context, ReminderService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(alarmIntent);
    }
}

