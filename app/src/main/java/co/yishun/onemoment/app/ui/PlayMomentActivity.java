package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
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
