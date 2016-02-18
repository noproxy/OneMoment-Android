package co.yishun.onemoment.app.ui.hybrd;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/22.
 */
@EActivity(R.layout.activity_empty_web)
public class EmptyWebActivity extends BaseWebActivity {
    @AfterViews void setupViews() {
        setupFragment();
    }

    @Override public void setPageInfo() {
        mPageName = "EmptyWebActivity";
    }
}
