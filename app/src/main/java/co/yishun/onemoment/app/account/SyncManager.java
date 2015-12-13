package co.yishun.onemoment.app.account;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import co.yishun.onemoment.app.account.sync.SyncAdapter;
import co.yishun.onemoment.app.data.compat.Contract;

/**
 * Created by Carlos on 12/13/15.
 */
public class SyncManager {
    private static final String TAG = "SyncManager";

    /**
     * request sync at once.
     */
    public static void syncNow(Context context) {
        Log.v(TAG, "sync at once");
        Account account = AccountManager.getAccount(context);
        Log.i(TAG, "sync account: " + account);
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putBoolean(SyncAdapter.BUNLDE_IGNORE_NETWORK, true);
        ContentResolver.requestSync(account, Contract.AUTHORITY, b);
    }
}
