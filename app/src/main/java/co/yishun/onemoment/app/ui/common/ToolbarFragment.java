package co.yishun.onemoment.app.ui.common;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.ui.MainActivity;

/**
 * Created by yyz on 7/14/15.
 */
@EFragment
public abstract class ToolbarFragment extends BaseFragment {
    @ViewById
    protected Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    protected abstract
    @DrawableRes
    int getTitleDrawableRes();

    @CallSuper
    protected ActionBar setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);

        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setLogo(getTitleDrawableRes());
        LogUtil.i("setupToolbar", "set home as up true");
        return ab;
    }

    @CallSuper
    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        setupToolbar(activity, toolbar);
        activity.syncToggle();
    }
}
