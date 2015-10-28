package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.view.View;

import org.androidannotations.annotations.EActivity;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by Carlos on 2015/10/29.
 */
@EActivity(R.layout.activity_moment_create)
public class MomentCreateActivity extends BaseActivity {
    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }
}
