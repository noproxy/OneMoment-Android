package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.common.BaseFragment;
import co.yishun.onemoment.app.ui.home.DiaryFragment_;
import co.yishun.onemoment.app.ui.home.DiscoveryFragment_;
import co.yishun.onemoment.app.ui.home.MeFragment_;
import co.yishun.onemoment.app.ui.home.WorldFragment_;

/**
 * Created by Jinge on 2016/1/24.
 */
@EActivity(R.layout.activity_tool_fragment)
public class HomeContainerActivity extends BaseActivity {
    private static final String TAG = "HomeContainerActivity";
    @ViewById
    protected Toolbar toolbar;
    @Extra
    String type;
    private BaseFragment fragment;

    @AfterInject
    void setDefault() {
        if (TextUtils.equals(type, "world")) {
            fragment = WorldFragment_.builder().build();
        } else if (TextUtils.equals(type, "diary")) {
            fragment = DiaryFragment_.builder().build();
        } else if (TextUtils.equals(type, "explore")) {
            fragment = DiscoveryFragment_.builder().build();
        } else if (TextUtils.equals(type, "mine")) {
            fragment = MeFragment_.builder().build();
        } else {
            LogUtil.e(TAG, "unknown type");
        }
    }

    @AfterViews
    void setupViews() {
        setupToolbar();
        getSupportFragmentManager().beginTransaction().replace(R.id.containerFrameLayout, fragment).commit();
    }

    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }
}
