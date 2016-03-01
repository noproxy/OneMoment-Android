package co.yishun.onemoment.app.ui;

import android.support.annotation.CallSuper;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.APIV4;
import co.yishun.onemoment.app.api.authentication.OneMomentV4;
import co.yishun.onemoment.app.api.modelv4.ApiModel;
import co.yishun.onemoment.app.api.modelv4.ShareInfo;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;
import co.yishun.onemoment.app.api.modelv4.WorldProvider;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.play.PlayTagVideoFragment;
import co.yishun.onemoment.app.ui.play.PlayTagVideoFragment_;
import co.yishun.onemoment.app.ui.play.PlayWorldFragment;
import co.yishun.onemoment.app.ui.play.PlayWorldFragment_;

/**
 * Created on 2015/10/26.
 */
@EActivity(R.layout.activity_play)
public class PlayActivity extends BaseActivity {
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_WORLD = 2;
    private static final String TAG = "PlayActivity";

    @Extra
    int type;
    @Extra
    VideoProvider video;
    @Extra
    WorldProvider world;
    @Extra
    boolean forWorld;

    @ViewById
    Toolbar toolbar;

    private FragmentManager fragmentManager;

    @Override
    public void setPageInfo() {
        mPageName = "PlayActivity";
    }

    @AfterViews
    void setupView() {
        fragmentManager = getSupportFragmentManager();
        setupToolbar(this, toolbar);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (!forWorld && !TextUtils.equals(today, world.getName()))
            findViewById(R.id.worldAdd).setVisibility(View.GONE);

        switch (type) {
            case TYPE_VIDEO:
                PlayTagVideoFragment playTagVideoFragment = PlayTagVideoFragment_.builder().video(video).build();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, playTagVideoFragment).commit();
                break;
            case TYPE_WORLD:
                PlayWorldFragment playWorldFragment = PlayWorldFragment_.builder().world(world).forWorld(forWorld).build();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, playWorldFragment).commit();
                break;
        }
    }

    @CallSuper
    protected ActionBar setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);

        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(world.getName());
        String num = String.valueOf(world.getVideosNum());
        SpannableString ss = new SpannableString(String.format(getString(R.string.fragment_world_suffix_people_count), world.getVideosNum()));
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, num.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        toolbar.setSubtitle(ss);
        LogUtil.i("setupToolbar", "set home as up true");
        return ab;
    }

    @Click(R.id.worldAdd)
    void addVideo(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        ShootActivity_.intent(this).transitionX(location[0] + view.getWidth() / 2)
                .transitionY(location[1] + view.getHeight() / 2).forWorld(forWorld).forToday(!forWorld).world(world).start();
    }

    @Click(R.id.worldShare)
    @Background
    void shareWorld(View view) {
        APIV4 apiv4 = OneMomentV4.createAdapter().create(APIV4.class);
        ShareInfo shareInfo = forWorld ? apiv4.shareWorld(world.getName(), AccountManager.getUserInfo(this)._id) :
                apiv4.shareToday(world.getName(), AccountManager.getUserInfo(this)._id);
        ShareActivity_.intent(this).shareInfo(shareInfo).shareType(ShareActivity.TYPE_SHARE_WORLD).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.activity_play_world_action_delete:// will only be called when it is
                // PlayWorldFragment
                if (video instanceof WorldVideo) {
                    WorldVideo worldVideo = (WorldVideo) video;
                    new MaterialDialog.Builder(this).cancelable(true).canceledOnTouchOutside(true)
                            .content(R.string.activity_play_world_dialog_delete_content).positiveText(R
                            .string.activity_play_world_dialog_delete_positive).negativeText(R
                            .string.activity_play_world_dialog_delete_negative).onPositive(
                            (dialog, which) -> dialog.dismiss()).onNegative((dialog1, which1) -> {
                        deleteWorldVideo(worldVideo);
                    }).show();
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (PlayActivity.TYPE_VIDEO == type) {
            getMenuInflater().inflate(R.menu.menu_activity_play_world, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Background
    void deleteWorldVideo(WorldVideo worldVideo) {
        showProgress(R.string.activity_play_world_progress_delete_content);
        APIV4 apiv4 = OneMomentV4.createAdapter().create(APIV4.class);
        ApiModel result = apiv4.deleteWorldVideo(worldVideo._id, AccountManager.getUserInfo(this)
                ._id);
        if (result.isSuccess()) {
            hideProgress();
            runOnUiThread(() -> {
                        Toast.makeText(this, R.string.activity_play_world_progress_delete_success,
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
            );
        } else {
            showSnackMsg(R.string.activity_play_world_progress_delete_fail);
            LogUtil.i(TAG, "delete world fail:" + result.toString());
        }
    }
}
