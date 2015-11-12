package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by Jinge on 2015/11/12.
 */
@EActivity(R.layout.activity_user_info)
public class UserInfoActivity extends BaseActivity {
    @ViewById
    Toolbar toolbar;
    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setupToolbar() {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("个人资料");
        Log.i("setupToolbar", "set home as up true");
    }
}
