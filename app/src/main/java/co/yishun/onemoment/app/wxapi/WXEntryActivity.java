package co.yishun.onemoment.app.wxapi;

import android.os.Bundle;
import android.util.Log;

import co.yishun.onemoment.app.account.auth.WeChatHelper;
import co.yishun.onemoment.app.ui.common.BaseActivity;

public class WXEntryActivity extends BaseActivity {
    private static final String TAG = "WXEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Share", "onCreate");
        if (EntryActivity.mAuthHelper != null && EntryActivity.mAuthHelper instanceof WeChatHelper)
            ((WeChatHelper) EntryActivity.mAuthHelper).handleIntent(getIntent());
        finish();
    }

    @Override public void setPageInfo() {
        mIsPage = false;
    }
}
