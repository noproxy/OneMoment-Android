package co.yishun.onemoment.app.ui.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;

import co.yishun.onemoment.app.LogUtil;

/**
 * Created on 2015/12/28.
 */
public abstract class WXRespActivity extends BaseActivity {
    public static final String ACTION_WX_RESP = "co.yishun.onemoment.app.wxresp";
    private static final String TAG = "WXRespActivity";
    protected WXRespReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new WXRespReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_WX_RESP);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
    }

    protected abstract void onWXRespIntent(Intent intent);

    public class WXRespReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "receiver the broadcast");
            onWXRespIntent(intent);
        }
    }
}
