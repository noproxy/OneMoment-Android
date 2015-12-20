package co.yishun.onemoment.app.account.sync.moment;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by Carlos on 2015/12/20.
 */
public interface MomentSync {

    static void sync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        MomentSyncImpl_.getInstance_(context).onPerformSync(account, extras, authority, provider, syncResult);
    }

    void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult);

}
