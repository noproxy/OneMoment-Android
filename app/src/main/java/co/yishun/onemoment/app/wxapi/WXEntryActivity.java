package co.yishun.onemoment.app.wxapi;

import android.content.Intent;
import android.os.Bundle;

import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.common.WXRespActivity;

public class WXEntryActivity extends BaseActivity {
    private static final String TAG = "WXEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
        intent.putExtras(getIntent().getExtras());
        intent.setAction(WXRespActivity.ACTION_WX_RESP);
        sendBroadcast(intent);
        finish();
    }

    @Override public void setPageInfo() {
        mIsPage = false;
    }
}
