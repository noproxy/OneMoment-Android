package co.yishun.onemoment.app.account;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.compat.Contract;

/**
 * Created by Carlos on 12/13/15.
 */
public class SyncManager {
    public static final String SYNC_IGNORE_NETWORK = "boolean_ignore_network";
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
        Log.v(TAG, "sync at once");
        Account account = AccountManager.getAccount(context);
        Log.i(TAG, "sync account: " + account);
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
        long frequency = pref.getLong(context.getString(R.string.pref_key_sync_frequency), 60L);// unit: minutes

        Bundle b = new Bundle();
        b.putBoolean(SYNC_IGNORE_NETWORK, canCellular);

        ContentResolver.setMasterSyncAutomatically(isSync);
        ContentResolver.addPeriodicSync(AccountManager.getAccount(context), Contract.AUTHORITY, b, frequency * 60);
    }
}
