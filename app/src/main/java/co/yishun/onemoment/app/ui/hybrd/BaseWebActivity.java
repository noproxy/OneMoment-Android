package co.yishun.onemoment.app.ui.hybrd;

import android.support.annotation.CallSuper;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by Jinge on 2016/1/21.
 */
@EActivity
public abstract class BaseWebActivity extends BaseActivity {

    @Extra
    protected String title;
    @Extra
    protected String url;

    @ViewById
    protected Toolbar toolbar;
    protected BaseWebFragment mWebFragment;

    @CallSuper
    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle(title);
    }

    protected void setupFragment() {
        mWebFragment = CommonWebFragment_.builder().mUrl(url).build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, mWebFragment, BaseWebFragment.TAG_WEB).commit();
    }

    /**
     * Activities extend {@link BaseWebActivity} will call {@link #finish()} when the navigation up
     * button clicked.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
