package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.view.View;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by Carlos on 2015/8/17.
 */

@EActivity(R.layout.activity_tag)
public class TagActivity extends BaseActivity {
    @Extra
    WorldTag tag;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }
}
