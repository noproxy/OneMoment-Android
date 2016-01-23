package co.yishun.onemoment.app.ui;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.common.BaseWebActivity;

/**
 * Created by Jinge on 2016/1/22.
 */
@EActivity(R.layout.activity_web_view)
public class CreateWorldActivity extends BaseWebActivity {

    @AfterInject void setDefault() {
        title = getString(R.string.activity_create_world_title);
        File hybrdDir = FileUtil.getInternalFile(this, Constants.HYBRD_UNZIP_DIR);
        url = Constants.FILE_URL_PREFIX + new File(hybrdDir, "build/pages/add_to_world/add_to_world.html").getPath();
    }

    @AfterViews void setupViews() {
        setupToolbar();
        setupFragment();
    }

    @Override public void setPageInfo() {
        mPageName = "CreateWorldActivity";
    }
}
