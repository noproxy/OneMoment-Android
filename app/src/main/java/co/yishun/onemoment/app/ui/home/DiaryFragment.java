package co.yishun.onemoment.app.ui.home;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.library.calendarlibrary.MomentCalendar;
import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/25/15.
 */

@EFragment(R.layout.fragment_diary)
public class DiaryFragment extends Fragment {
    @ViewById MomentCalendar momentCalendar;

    @AfterViews
    void setCalendar() {
        momentCalendar.setAdapter((calendar, dayView) -> {

        });
    }

}
