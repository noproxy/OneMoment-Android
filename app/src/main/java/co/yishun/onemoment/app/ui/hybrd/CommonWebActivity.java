package co.yishun.onemoment.app.ui.hybrd;

import android.text.TextUtils;
import android.view.MenuItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/21.
 */
@EActivity(R.layout.activity_tool_fragment)
public class CommonWebActivity extends BaseWebActivity {

    @AfterInject void setupDefault() {
        if (TextUtils.isEmpty(title)) title = getString(R.string.app_name);
    }

    @AfterViews void setupViews() {
        setupToolbar();
        setupFragment();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void setPageInfo() {
        mPageName = "CommonWebActivity";
    }
}
