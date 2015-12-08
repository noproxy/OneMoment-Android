package co.yishun.onemoment.app.ui.home;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.j256.ormlite.dao.Dao;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
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
import co.yishun.onemoment.app.ui.common.ToolbarFragment;

/**
 * Created by yyz on 7/25/15.
 */

@EFragment
public class DiaryFragment extends ToolbarFragment implements MomentMonthView.MonthAdapter {
    private static final String TAG = "DiaryFragment";
    @ViewById MomentCalendar momentCalendar;

    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;

    @AfterViews
    void setCalendar() {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_diary, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_diary_action_share:
                //TODO add share function
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getTitleDrawableRes() {
        return R.drawable.pic_diary_tittle;
    }

    @Override
    public void setPageInfo() {
        mPageName = "DiaryFragment";
    }

    @Override public void onBindView(Calendar calendar, DayView dayView) {
        String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(calendar.getTime());
        try {
            Moment moment = momentDao.queryBuilder().where().eq("time", time).queryForFirst();
            if (moment != null) {
                Picasso.with(getContext()).load(new File(moment.getThumbPath())).into(new Target() {
                    @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        dayView.setImageBitmap(bitmap);
                    }

                    @Override public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
                Log.i(TAG, "moment found: " + moment.getTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
