package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import co.yishun.library.EditTagContainer;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.VideoTag;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by Carlos on 2015/11/2.
 */
@EActivity(R.layout.activity_tag_create)
public class TagCreateActivity extends BaseActivity {
    public static final int REQUEST_CODE_SEARCH = 1;
    private static final String TAG = "TagCreateActivity";
    @ViewById Toolbar toolbar;
    @Extra WorldTag worldTag;
    @Extra boolean forWorld = false;
    @Extra String videoPath;
    @ViewById EditTagContainer editTagContainer;
    @ViewById ImageView momentPreviewImageView;

    private float tagX;
    private float tagY;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setupViews() {

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
    void setEditTagContainer() {
        editTagContainer.setOnAddTagListener((x, y) -> {
            //TODO startActivity to add TAG
            tagX = x;
            tagY = y;
            if (forWorld) {
                TagSearchActivity_.intent(this).forWorld(true).worldTag(worldTag.name).startForResult(REQUEST_CODE_SEARCH);
            } else {
                TagSearchActivity_.intent(this).forWorld(false).startForResult(REQUEST_CODE_SEARCH);
            }
        });
    }

    @OnActivityResult(REQUEST_CODE_SEARCH)
    void result(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String tag = data.getStringExtra(TagSearchActivity.RESULT_DATA);
            editTagContainer.addTag(new BaseVideoTag(tag, tagX, tagY));
        }
    }

    @Click
    void nextBtnClicked(View view) {

    }
}
