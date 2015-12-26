package co.yishun.onemoment.app.account;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.compat.Contract;

/**
 * Created by Carlos on 12/13/15.
 */
public class SyncManager {
    public static final String SYNC_IGNORE_NETWORK = "boolean_ignore_network";

    public static final String SYNC_BROADCAST_ACTION_LOCAL_UPDATE = "co.yishun.onemoment.app.sync.action.localupdate";
    public static final String SYNC_BROADCAST_EXTRA_LOCAL_UPDATE_DATE = "extra_update_date";
    public static final String SYNC_BROADCAST_EXTRA_LOCAL_UPDATE_TIMESTAMP = "extra_update_timestamp";

    public static final String SYNC_BROADCAST_ACTION_START = "co.yishun.onemoment.app.sync.action.start";
    public static final String SYNC_BROADCAST_EXTRA_START_TASK_NUM = "extra_start_task_num";


    public static final String SYNC_BROADCAST_ACTION_END = "co.yishun.onemoment.app.sync.action.end";
    public static final String SYNC_BROADCAST_EXTRA_END_RESULT = "extra_end_result";
    public static final int SYNC_BROADCAST_EXTRA_END_RESULT_SUCCESS = 1;
    public static final int SYNC_BROADCAST_EXTRA_END_RESULT_CANCEL = 0;
    public static final int SYNC_BROADCAST_EXTRA_END_RESULT_FAIL = -1;
    public static final String SYNC_BROADCAST_ACTION_PROGRESS = "co.yishun.onemoment.app.sync.action.progress";
    public static final String SYNC_BROADCAST_EXTRA_PROGRESS_VALUE = "extra_int_progress_value";
    public static final int PROGRESS_MAX_VALUE = 100;

    public static final int PROGRESS_NOT_AVAILABLE = -1;
    public static final int PROGRESS_ERROR = -2;
    private static final String TAG = "SyncManager";

    /**
     * Request sync at once. Ignore any network settings.
     */
    public static void syncNow(Context context) {
        syncNow(context, true);
    }

    /**
     * request sync at once.
     */
    public static void syncNow(Context context, Boolean ignoreNetwork) {
        LogUtil.v(TAG, "sync at once");
        Account account = AccountManager.getAccount(context);
        LogUtil.i(TAG, "sync account: " + account);
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putBoolean(SYNC_IGNORE_NETWORK, ignoreNetwork);
        ContentResolver.requestSync(account, Contract.AUTHORITY, b);
    }

    public static void notifySyncSettingsChange(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isSync = pref.getBoolean(context.getString(R.string.pref_key_sync), true);
        boolean canCellular = pref.getBoolean(context.getString(R.string.pref_key_sync_cellular_data), false);
        long frequency = Long.parseLong(pref.getString(context.getString(R.string.pref_key_sync_frequency), "60"));// unit: minutes

        LogUtil.i(TAG, "notifySyncSettingsChange, isSync: " + isSync);

        Bundle b = new Bundle();
        b.putBoolean(SYNC_IGNORE_NETWORK, canCellular);

        ContentResolver.setMasterSyncAutomatically(isSync);
        ContentResolver.addPeriodicSync(AccountManager.getAccount(context), Contract.AUTHORITY, b, frequency * 60);
    }

    protected static void disableSync() {
        ContentResolver.setMasterSyncAutomatically(false);
    }

    @IntDef(value = {SYNC_BROADCAST_EXTRA_END_RESULT_SUCCESS, SYNC_BROADCAST_EXTRA_END_RESULT_FAIL, SYNC_BROADCAST_EXTRA_END_RESULT_CANCEL})
    public @interface EndResult {
    }
}
