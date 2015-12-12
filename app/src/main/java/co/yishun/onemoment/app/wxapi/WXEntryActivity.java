package co.yishun.onemoment.app.wxapi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import co.yishun.onemoment.app.account.auth.WeChatHelper;
import co.yishun.onemoment.app.ui.common.BaseActivity;

public class WXEntryActivity extends BaseActivity {
    private static final String TAG = "WXEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(new FrameLayout(this));
        if (EntryActivity.mAuthHelper != null && EntryActivity.mAuthHelper instanceof WeChatHelper)
            ((WeChatHelper) EntryActivity.mAuthHelper).handleIntent(getIntent());
        finish();
    }

    @Nullable @Override public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @Override public void setPageInfo() {
        mIsPage = false;
    }
}
