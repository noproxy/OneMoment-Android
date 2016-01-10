package co.yishun.onemoment.app.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.SdkVersionHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.remind.ReminderReceiver;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.SplashCover;
import co.yishun.onemoment.app.data.DataMigration;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.wxapi.EntryActivity_;

/**
 * Created by yyz on 7/23/15.
 */
@EActivity(R.layout.activity_splash)
public class SplashActivity extends BaseActivity {
    public static final String RUNTIME_PREFERENCE = "run_preference";
    public static final String PREFERENCE_IS_FIRST_LAUNCH = "is_first_launch";
    public static final String PREFERENCE_SPLASH_UPDATE_TIME = "splash_update_time";
    public static final String PREFERENCE_SPLASH_COVER_NAME = "splash_cover_name";
    public static final String PREFERENCE_SPLASH_STAY = "splash_stay";
    public static final String DEFAULT_SPLASH_COVER_NAME = "splash_cover_0.png";
    private static final String TAG = "SplashActivity";

    @ViewById ImageView splashImageView;
    @ViewById ImageView splashCover;

    private SharedPreferences preferences;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SdkVersionHelper.getSdkInt() >= 16) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        preferences = getSharedPreferences(RUNTIME_PREFERENCE, MODE_PRIVATE);
        delayShowCover();
        new DataMigration(this, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    boolean isFirstLaunch() {
        return preferences.getBoolean(PREFERENCE_IS_FIRST_LAUNCH, true);
    }

    @UiThread(delay = 1600) void delayShowCover() {
        String coverName = preferences.getString(PREFERENCE_SPLASH_COVER_NAME, DEFAULT_SPLASH_COVER_NAME);
        File coverFile = FileUtil.getInternalFile(this, coverName);
        if (coverFile.length() > 0) {
            Picasso.with(this).load(coverFile).into(splashCover);
            int stay = preferences.getInt(PREFERENCE_SPLASH_STAY, 1000);
            if (stay <= 0) stay = 1000;
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(this::endWithStartMain, stay);
        } else {
            endWithStartMain();
        }
        updateCover(coverFile);
    }

    void endWithStartMain() {
        this.finish();
        if (isFirstLaunch()) {
            sendBroadcast(new Intent(ReminderReceiver.ACTION_UPDATE_REMIND));
            GuideActivity_.intent(this).isFirstLuanch(true).start();
            getSharedPreferences(RUNTIME_PREFERENCE, MODE_PRIVATE).edit()
                    .putBoolean(PREFERENCE_IS_FIRST_LAUNCH, false).apply();
        } else if (AccountManager.isLogin(this))
            MainActivity_.intent(this).start();
        else
            EntryActivity_.intent(this).start();
    }

    @Background void updateCover(File coverFile) {
        LogUtil.d(TAG, "get cover url");
        Misc misc = OneMomentV3.createAdapter().create(Misc.class);
        SplashCover splashCover = misc.getSplashCover();
        long lastUpdateTime = preferences.getLong(PREFERENCE_SPLASH_UPDATE_TIME, 0L);

        if (splashCover.updateTime > lastUpdateTime) {
            OkHttpClient client = new OkHttpClient();
            Call call = client.newCall(new Request.Builder().url(splashCover.url).build());
            InputStream input = null;
            FileOutputStream output = null;
            try {
                Response response = call.execute();
                if (response.code() == 200) {
                    input = response.body().byteStream();
                    long inputLength = response.body().contentLength();
                    LogUtil.d(TAG, "get image " + splashCover.url + " length " + inputLength);
                    if (inputLength == 0) {
                        return;
                    }
                    File newCover = new File(coverFile.getParent(), "splash_cover_" + splashCover.updateTime + ".png");
                    if (newCover.exists()) newCover.delete();
                    newCover.createNewFile();
                    LogUtil.d(TAG, "into " + newCover.getPath());
                    output = new FileOutputStream(newCover);

                    byte data[] = new byte[2048];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    preferences.edit()
                            .putLong(PREFERENCE_SPLASH_UPDATE_TIME, splashCover.updateTime)
                            .putInt(PREFERENCE_SPLASH_STAY, splashCover.stay)
                            .putString(PREFERENCE_SPLASH_COVER_NAME, newCover.getName())
                            .apply();
                    coverFile.delete();
                    LogUtil.i(TAG, "finish image download");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    @Override
    public void setPageInfo() {
        mIsPage = true;
        mPageName = "SplashActivity";
    }
}
