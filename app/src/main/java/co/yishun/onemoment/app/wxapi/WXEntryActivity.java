package co.yishun.onemoment.app.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.account.auth.WeChatHelper;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.share.ShareActivity_;

public class WXEntryActivity extends BaseActivity {
    private static final String TAG = "WXEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("Share", "onCreate");
        if (EntryActivity.mAuthHelper != null && EntryActivity.mAuthHelper instanceof WeChatHelper)
            ((WeChatHelper) EntryActivity.mAuthHelper).handleIntent(getIntent());
        else {
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setClass(this, ShareActivity_.class);
            startActivity(intent);
        }
        finish();
    }

    @Override public void setPageInfo() {
        mIsPage = false;
    }
}
