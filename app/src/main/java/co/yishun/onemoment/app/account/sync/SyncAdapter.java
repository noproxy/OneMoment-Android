package co.yishun.onemoment.app.account.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.account.sync.moment.MomentSync;


/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 * <p>
 * Created by Carlos on 3/10/15.
 */
@EBean
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    @SystemService ConnectivityManager connectivityManager;


    public SyncAdapter(Context context) {
        super(context, true);
    }


    /**
     * To execute sync. When sync end, it will call {@link #onSyncEnd()}
     * to send broadcast notify sync process ending.
     * <p>
     * Sub sync module may provide other intent to broadcast their progress.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "onPerformSync, account: " + account.name + ", Bundle: " + extras);
        if (!checkSyncOption(extras))
            return;
        onSyncStart();
        MomentSync.sync(getContext(), account, extras, authority, provider, syncResult);
        onSyncEnd();
    }

    /**
     * check sync option.
     *
     * @param extras Bundle of sync.
     * @return false if sync should give up.
     */
    private boolean checkSyncOption(Bundle extras) {
        if (!extras.getBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, false) &&
                !extras.getBoolean(SyncManager.SYNC_IGNORE_NETWORK, false) &&
                !connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            Log.i(TAG, "cancel sync because network is not permitted");
            return false;
        }
        Log.i(TAG, "sync option is OK");
        return true;
    }

    private void onSyncEnd() {
        Log.i(TAG, "sync end");
        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_END);
        getContext().sendBroadcast(intent);
    }

    private void onSyncStart() {
        Log.i(TAG, "sync start");
        Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_START);
        getContext().sendBroadcast(intent);
    }
}
