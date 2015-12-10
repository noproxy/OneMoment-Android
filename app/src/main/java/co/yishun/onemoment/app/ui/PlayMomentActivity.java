package co.yishun.onemoment.app.ui;

import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.play.PlayMomentFragment;
import co.yishun.onemoment.app.ui.play.PlayMomentFragment_;

@EActivity(R.layout.activity_play_moment)
public class PlayMomentActivity extends AppCompatActivity {

    @Extra String startDate;
    @Extra String endDate;

    @ViewById FrameLayout containerFrameLayout;

    private PlayMomentFragment playMomentFragment;

    @AfterViews void setUpViews(){
        playMomentFragment = PlayMomentFragment_.builder().startDate(startDate).endDate(endDate).build();
        getSupportFragmentManager().beginTransaction().replace(R.id.containerFrameLayout, playMomentFragment).commit();
    }
}
