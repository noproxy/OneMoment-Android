package co.yishun.onemoment.app.account.sync.moment;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Abstract MomentSync module. Call {@link #sync(Context, Account, Bundle, String,
 * ContentProviderClient, SyncResult)} to sync. <p> Created by Carlos on 2015/12/20.
 */
public abstract class MomentSync {

    public static MomentSync getInstance(Context context) {
        return MomentSyncImpl_.getInstance_(context);
    }

    /**
     * Just sync.
     */
    public static void sync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        getInstance(context).onPerformSync(account, extras, authority, provider, syncResult);
    }

    abstract void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult);

}
