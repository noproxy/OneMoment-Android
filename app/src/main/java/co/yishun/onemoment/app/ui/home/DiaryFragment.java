package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.yishun.library.calendarlibrary.DayView;
import co.yishun.library.calendarlibrary.MomentCalendar;
import co.yishun.library.calendarlibrary.MomentMonthView;
import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.ui.MainActivity;
import co.yishun.onemoment.app.ui.PlayMomentActivity_;
import co.yishun.onemoment.app.ui.ShareExportActivity_;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;
import co.yishun.onemoment.app.ui.view.TodayMomentView;

/**
 * Created by yyz on 7/25/15.
 */

@EFragment
public class DiaryFragment extends ToolbarFragment
        implements MomentMonthView.MonthAdapter, DayView.OnMomentSelectedListener {
    private static final String TAG = "DiaryFragment";
    @ViewById MomentCalendar momentCalendar;
    @ViewById TodayMomentView todayMomentView;

    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;

    private Moment selectMoment;

    /**
     * judge whether a {@link Date} is in current display month.
     *
     * @param date
     * @return
     */
    public boolean isCurrentMonth(Date date) {
        Calendar calendar = momentCalendar.getCurrentCalendar();
        Calendar toChanged = Calendar.getInstance();
        toChanged.setTime(date);

        return toChanged.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && toChanged.get(Calendar.MONTH) == calendar.get(Calendar.MONTH);
    }

    @AfterViews void setCalendar() {
        momentCalendar.setAdapter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_diary, container, false);
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        DayView.setTodayAvailableListener(this::onSelected);
        DayView.setMultiSelection(false);
        DayView.setOnMomentSelectedListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_diary, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Click(R.id.momentPreviewImageView) void previewClick() {
        if (selectMoment != null)
            PlayMomentActivity_.intent(this.getActivity()).startDate(selectMoment.getTime()).endDate(selectMoment.getTime()).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_diary_action_share:
                //TODO add share function
                ShareExportActivity_.intent(this.getActivity()).start();
                return true;
            case R.id.fragment_diary_action_all_play:
                try {
                    List<Moment> momentList = momentDao.queryBuilder().orderBy("time", true).query();
                    if (momentList.size() == 0)
                        ((MainActivity) this.getActivity()).showSnackMsg("No moment");
                    else
                        PlayMomentActivity_.intent(this.getActivity()).startDate(momentList.get(0).getTime())
                                .endDate(momentList.get(momentList.size() - 1).getTime()).start();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getTitleDrawableRes() {
        return R.drawable.pic_diary_title;
    }

    @Override
    public void setPageInfo() {
        mPageName = "DiaryFragment";
    }

    @Override
    public void onBindView(Calendar calendar, DayView dayView) {
        String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(calendar.getTime());
        try {
            Moment moment = momentDao.queryBuilder().where().eq("time", time).queryForFirst();
            if (moment != null) {
                dayView.setEnabled(true);
                dayView.setTag(moment);
                //fix DayView, change to into(dayView)
                Picasso.with(getContext()).load(new File(moment.getThumbPath())).into(dayView);
                LogUtil.i(TAG, "moment found: " + moment.getTime());
            } else {
                dayView.setEnabled(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSelected(@NonNull DayView dayView) {
        LogUtil.i(TAG, "onSelected: " + dayView);
        Moment moment = (Moment) dayView.getTag();
        selectMoment = moment;
        if (moment != null) {
            todayMomentView.setTodayMoment(TodayMomentView.TodayMoment.momentTodayIs(moment));
        } else {
            todayMomentView.setTodayMoment(TodayMomentView.TodayMoment.noMomentToday(new Date()));
            // TODO everyday can be select, update calendar selection.
        }
    }
}
