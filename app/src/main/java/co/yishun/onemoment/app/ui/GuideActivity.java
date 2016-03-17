package co.yishun.onemoment.app.ui;

import org.androidannotations.annotations.EActivity;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Simple activity to show the whole product tour.
 */
@EActivity(R.layout.activity_guide)
public class GuideActivity extends BaseActivity {

    @Override
    public void setPageInfo() {
        mPageName = "GuideActivity";
    }
}
