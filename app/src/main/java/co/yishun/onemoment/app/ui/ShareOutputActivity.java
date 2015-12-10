package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import co.yishun.library.calendarlibrary.MomentCalendar;
import co.yishun.onemoment.app.R;

@EActivity(R.layout.activity_share_output)
public class ShareOutputActivity extends AppCompatActivity {

    @ViewById Toolbar toolbar;
    @ViewById MomentCalendar momentCalendar;

    @AfterViews void setupViews() {

    }

    @AfterViews void setAppbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_share_output_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_action_back_close);

    }
}
