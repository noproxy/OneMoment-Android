package co.yishun.onemoment.app.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.SdkVersionHelper;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.remind.ReminderReceiver;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.wxapi.EntryActivity;

/**
 * Created by yyz on 7/23/15.
 */
@EActivity(R.layout.activity_splash)
public class SplashActivity extends BaseActivity {
    public static final String RUNTIME_PREFERENCE = "run_preference";
    public static final String PREFERENCE_IS_FIRST_LAUNCH = "is_first_launch";

    @ViewById
    ImageView splashImageView;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SdkVersionHelper.getSdkInt() >= 16) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRecording();
    }

    @AfterViews void setResource() {
        // do nothing because don't need different image
//        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
//        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
//
//        if (hasBackKey && hasHomeKey) {
//            // no navigation bar, unless it is enabled in the settings
//            splashImageView.setImageResource(R.drawable.bg_welcome_no_nav);
//        } else {
//            // 99% sure there's a navigation bar
//            splashImageView.setImageResource(R.drawable.bg_welcome);
//        }
    }

    boolean isFirstLaunch() {
        return getSharedPreferences(RUNTIME_PREFERENCE, MODE_PRIVATE).getBoolean(PREFERENCE_IS_FIRST_LAUNCH, true);
    }


    @UiThread(delay = 1600) void startRecording() {
        sendBroadcast(new Intent(ReminderReceiver.ACTION_UPDATE_REMIND));
        this.finish();
//        if (BuildConfig.DEBUG) new EveryDayNotification().onReceive(this, null);
        if (isFirstLaunch()) {
            GuideActivity_.intent(this).isFirstLuanch(true).start();
            getSharedPreferences(RUNTIME_PREFERENCE, MODE_PRIVATE).edit()
                    .putBoolean(PREFERENCE_IS_FIRST_LAUNCH, false).apply();
        } else if (AccountManager.isLogin(this))
            MainActivity_.intent(this).start();
        else
            startActivity(new Intent(this, EntryActivity.class));
    }

    @Override
    public void setPageInfo() {
        mIsPage = true;
        mPageName = "SplashActivity";
    }
}
