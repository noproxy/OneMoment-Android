package co.yishun.onemoment.app.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.library.calendarlibrary.MomentCalendar;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;

/**
 * Created by yyz on 7/25/15.
 */

@EFragment
public class DiaryFragment extends ToolbarFragment {
    @ViewById MomentCalendar momentCalendar;

    @AfterViews
    void setCalendar() {
        momentCalendar.setAdapter((calendar, dayView) -> {

        });
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
}
