package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
    @ViewById Toolbar toolbar;
    private TextView countTextView;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setCountTextView() {
        if (countTextView == null) return;
        int count = 110;//TODO get it from database
        final String prefixText = "<font color='" + getResources().getColor(R.color.colorAccent) + "'>" + getString(R.string.activity_moment_create_count_text_prefix) + "</font>";
        final String suffixText = "<font color='" + getResources().getColor(R.color.colorAccent) + "'>" + getString(R.string.activity_moment_create_count_text_suffix) + "</font>";
        final String countText = "<font color='" + getResources().getColor(R.color.textColorPrimaryDark) + "'>" + count + "</font>";
        countTextView.setText(Html.fromHtml(prefixText + " " + countText + " " + suffixText));
    }

    @AfterViews
    void setupToolbar() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_moment_create_title_text);
        Log.i("setupToolbar", "set home as up true");
    }

    @AfterViews
    void setVideo() {
        if (videoPath == null) return;
        videoView.setVideoPath(videoPath);
        videoView.start();
    }

    @Click
    void nextBtnClicked(View view) {
    }

}
