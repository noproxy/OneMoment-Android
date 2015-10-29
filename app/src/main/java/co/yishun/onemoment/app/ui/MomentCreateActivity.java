package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.VideoView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by Carlos on 2015/10/29.
 */
@EActivity(R.layout.activity_moment_create)
public class MomentCreateActivity extends BaseActivity {

    @Extra String videoPath;
    @ViewById VideoView videoView;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }


    @AfterViews
    void setVideo() {
        videoView.setVideoPath(videoPath);
        videoView.start();
    }

    @Click
    void nextBtnClicked(View view) {
    }

}
