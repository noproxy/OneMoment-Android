package co.yishun.onemoment.app.ui.common;

import android.support.annotation.CallSuper;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Jinge on 2016/1/21.
 */
@EActivity
public abstract class BaseWebActivity extends BaseActivity {

    @Extra protected String title;
    @Extra protected String url;

    @ViewById protected Toolbar toolbar;

    @CallSuper protected void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle(title);
    }
}
