package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import co.yishun.library.calendarlibrary.DayView;
import co.yishun.library.calendarlibrary.MomentCalendar;
import co.yishun.library.calendarlibrary.MomentMonthView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;

@EActivity(R.layout.activity_share_export)
public class ShareExportActivity extends AppCompatActivity
        implements MomentMonthView.MonthAdapter, DayView.OnMomentSelectedListener {

    private static final String TAG = "ShareExportActivity";
    @ViewById Toolbar toolbar;
    @ViewById MomentCalendar momentCalendar;

    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;

    @AfterViews void setupViews() {
        momentCalendar.setAdapter(this);
        DayView.setOnMomentSelectedListener(this);
    }

    @AfterViews void setAppbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_share_export_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_action_back_close);

    }

    @Override public void onBindView(Calendar calendar, DayView dayView) {
        String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(calendar.getTime());
        try {
            Moment moment = momentDao.queryBuilder().where().eq("time", time).queryForFirst();
            if (moment != null) {
                dayView.setEnabled(true);
                dayView.setTag(moment);
                Picasso.with(this).load(new File(moment.getThumbPath())).into(dayView);
                Log.i(TAG, "moment found: " + moment.getTime());
            } else {
                dayView.setEnabled(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public void onSelected(DayView dayView) {
        Moment moment = (Moment) dayView.getTag();
        if (moment != null) {
            //            todayMomentView.setTodayMoment(TodayMomentView.TodayMoment.momentTodayIs(moment));
        } else {
            //            todayMomentView.setTodayMoment(TodayMomentView.TodayMoment.noMomentToday(new Date()));
            // TODO everyday can be select, update calendar selection.
        }
    }
}
