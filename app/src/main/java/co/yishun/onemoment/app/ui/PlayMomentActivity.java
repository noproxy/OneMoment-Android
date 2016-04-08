package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.ViewById;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.data.MomentLock;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.play.PlayMomentFragment;
import co.yishun.onemoment.app.ui.play.PlayMomentFragment_;

@EActivity(R.layout.activity_play_moment)
public class PlayMomentActivity extends BaseActivity {

    private static final String TAG = "PlayMomentActivity";
    @Extra
    String startDate;
    @Extra
    String endDate;

    @ViewById
    Toolbar toolbar;
    @ViewById
    FrameLayout containerFrameLayout;

    @OrmLiteDao(helper = MomentDatabaseHelper.class)
    Dao<Moment, Integer> momentDao;

    private PlayMomentFragment playMomentFragment;
    private List<Moment> playingMoments;

    @AfterViews
    void setUpViews() {
        try {
            playingMoments = new ArrayList<>();
            List<Moment> momentInDatabase = momentDao.queryBuilder().where().eq("owner", AccountManager.getAccountId(this)).and()
                    .between("time", startDate, endDate).query();
            for (Moment m : momentInDatabase) {
                if (m.getFile().length() > 0) {
                    playingMoments.add(m);
                    MomentLock.lockMoment(this, m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        playMomentFragment = PlayMomentFragment_.builder().startDate(startDate).endDate(endDate).build();
        getSupportFragmentManager().beginTransaction().replace(R.id.containerFrameLayout, playMomentFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (startDate.equals(endDate)) {
            getMenuInflater().inflate(R.menu.menu_activity_play_moment, menu);
            return true;
        } else return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.activity_play_moment_delete == item.getItemId()) {
            if (playingMoments.size() == 1) {
                askDeleteMoment(playingMoments.get(0));
                return true;
            } else {
                return false;
            }
        } else
            return super.onOptionsItemSelected(item);
    }

    private void askDeleteMoment(Moment moment) {
        new MaterialDialog.Builder(this).cancelable(true).canceledOnTouchOutside(true)
                .content(R.string.activity_play_today_dialog_delete_content).negativeText(R
                .string.activity_play_today_dialog_delete_positive).positiveText(R
                .string.activity_play_today_dialog_delete_negative).onNegative(
                (dialog, which) -> dialog.dismiss()).onPositive((dialog1, which1) -> {
            showProgress();
            deleteMoment(moment);
        }).show();
    }

    @Background
    void deleteMoment(Moment moment) {
        ApiModel result = OneMomentV3.createAdapter().create(Misc.class).deleteVideo(moment.getKey());
        hideProgress();
        if (result.code != -99) {
            try {
                momentDao.delete(moment);

                //TODO send broadcast whenever local moment changed, those lines code copy from MomentSyncImpl
                String timestamp = moment.getUnixTimeStamp();
                Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE);
                intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_LOCAL_UPDATE_TIMESTAMP, timestamp);
                LogUtil.i(TAG, "delete moment, send a broadcast. timestamp: " + timestamp);
                this.sendBroadcast(intent);

            } catch (SQLException e) {
                e.printStackTrace();
            }
            showSnackMsg(R.string.activity_play_moment_msg_delete_ok);
            exit();
        } else {
            showSnackMsg(R.string.activity_play_moment_msg_delete_fail);
        }
    }

    @AfterViews
    void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        LogUtil.i("setupToolbar", "set home as up true");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            for (Moment m : playingMoments) {
                MomentLock.unlockMomentIfLocked(this, m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPageInfo() {
        mPageName = "PlayMomentActivity";
    }
}
