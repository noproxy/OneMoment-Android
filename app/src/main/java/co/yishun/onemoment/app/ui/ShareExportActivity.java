package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
    @ViewById TextView shareText;
    @ViewById TextView exportText;
    @ViewById TextView selectAllText;
    @ViewById TextView clearText;
    @ViewById TextView selectedText;

    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;

    private int totalMoments;
    private List<Moment> selectedMoment;

    @AfterViews void setupViews() {
        momentCalendar.setAdapter(this);
        DayView.setOnMomentSelectedListener(this);
        try {
            totalMoments = (int) momentDao.queryBuilder().countOf();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        selectedMoment = new ArrayList<>();
        updateSelectedText();
    }

    @AfterViews void setAppbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_share_export_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_action_back_close);
    }

    @Click(R.id.shareText) void shareTextClicked() {

    }

    @Click(R.id.exportText) void exportTextClicked() {

    }

    @Click(R.id.selectAllText) void selectAllTextClicked() {

    }

    @Click(R.id.clearText) void clearTextClicked() {

    }

    void updateSelectedText() {
        String content = String.format(getResources().getString(R.string.activity_share_export_selected),
                selectedMoment.size(), totalMoments);
        SpannableString ss = new SpannableString(content);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 2,
                content.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        selectedText.setText(ss);
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
