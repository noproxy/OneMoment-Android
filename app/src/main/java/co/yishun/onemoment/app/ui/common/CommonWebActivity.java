package co.yishun.onemoment.app.ui.common;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/21.
 */
@EActivity(R.layout.activity_web_view)
public class CommonWebActivity extends BaseWebActivity {

    @AfterViews void setupViews() {
        setupToolbar();
    }

    @Override public void setPageInfo() {
        mPageName = "CommonWebActivity";
    }
}
