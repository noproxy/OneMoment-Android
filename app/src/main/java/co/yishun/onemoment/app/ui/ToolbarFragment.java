package co.yishun.onemoment.app.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/**
 * Created by yyz on 7/14/15.
 */
public abstract class ToolbarFragment extends Fragment {
    protected Toolbar toolbar;

    protected abstract @DrawableRes int getTitleDrawableRes();

    @CallSuper protected ActionBar setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);

        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        ab.setLogo(getTitleDrawableRes());
        Log.i("setupToolbar", "set home as up true");
        return ab;
    }

    @CallSuper @Override public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        setupToolbar(activity, toolbar);
        activity.syncToggle();
    }
}
