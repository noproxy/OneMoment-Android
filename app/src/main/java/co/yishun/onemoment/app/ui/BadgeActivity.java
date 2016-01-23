package co.yishun.onemoment.app.ui;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.hybrd.BaseWebActivity;
import co.yishun.onemoment.app.ui.hybrd.BaseWebFragment;
import co.yishun.onemoment.app.ui.hybrd.CommonWebFragment_;

/**
 * Created by Jinge on 2016/1/23.
 */
@EActivity(R.layout.activity_empty_web)
public class BadgeActivity extends BaseWebActivity {

    @Extra String badgeDetail;

    @AfterInject void setDefault() {
        File hybrdDir = FileUtil.getInternalFile(this, Constants.HYBRD_UNZIP_DIR);
        url = Constants.FILE_URL_PREFIX + new File(hybrdDir, "build/pages/badge/badge.html").getPath();
    }

    @AfterViews void setupViews() {
        setupFragment();
    }

    @Override protected void setupFragment() {
        mWebFragment = CommonWebFragment_.builder().mUrl(url).mArg(badgeDetail).build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, mWebFragment, BaseWebFragment.TAG_WEB).commit();
    }

    @Override public void setPageInfo() {
        mPageName = "BadgeActivity";
    }
}
