package co.yishun.onemoment.app.ui.hybrd;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/21.
 */
@EActivity(R.layout.activity_tool_fragment)
public class CommonWebActivity extends BaseWebActivity {

    @AfterViews void setupViews() {
        setupToolbar();
        setupFragment();
    }

    @Override public void setPageInfo() {
        mPageName = "CommonWebActivity";
    }
}
