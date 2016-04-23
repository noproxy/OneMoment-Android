package co.yishun.onemoment.app.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.lang.reflect.Field;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.CreateWorldActivity_;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;
import co.yishun.onemoment.app.ui.hybrd.BaseWebFragment;
import co.yishun.onemoment.app.ui.hybrd.CommonWebFragment;
import co.yishun.onemoment.app.ui.hybrd.CommonWebFragment_;
import co.yishun.onemoment.app.util.GuideUtil_;

/**
 * Created by yyz on 7/13/15.
 */
@EFragment(R.layout.fragment_world)
public class WorldFragment extends ToolbarFragment {
    CommonWebFragment worldWebFragment;
    @ViewById
    FrameLayout containerFrameLayout;

    @AfterViews
    void setUpViews() {
        File hybrdFile = FileUtil.getInternalFile(getActivity(), Constants.HYBRD_UNZIP_DIR);
        String url = Constants.FILE_URL_PREFIX + new File(hybrdFile, "build/pages/world/world.html")
                .getPath();
        LogUtil.d("WorldFragment","url = " + url);
        worldWebFragment = CommonWebFragment_.builder().mUrl(url).build();
        getFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, worldWebFragment, BaseWebFragment.TAG_WEB)
                .commit();

        worldWebFragment.setRefreshable(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        // ToolbarFragment set up toolbar in onResume, so I can only do this after super.onResume
        Context context = getContext();
        if (context != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            // if the showed guide version is less than current version, show it
            //noinspection PointlessBooleanExpression
            if (Constants.FORCE_SHOW_GUIDE || preferences.getInt(Constants.PrefKey.PREF_KEY_GUIDE_WORLD, -1) <
                    Constants.PrefKey.PREF_KEY_GUIDE_WORLD_CURRENT_VALUE) {
                showGuide();
                preferences.edit().putInt(Constants.PrefKey.PREF_KEY_GUIDE_WORLD,
                        Constants.PrefKey.PREF_KEY_GUIDE_WORLD_CURRENT_VALUE).apply();
            }
        }

    }


    @SuppressWarnings("TryWithIdenticalCatches")
    @UiThread(delay = 1000)
    void showGuide() {
        try {
            Field field = Toolbar.class.getDeclaredField("mNavButtonView");
            field.setAccessible(true);
            View view = (View) field.get(toolbar);
            Activity activity = getActivity();
            if (view != null && activity != null) {
                GuideUtil_.getInstance_(activity).showGuide(activity,
                        view, R.layout.layout_tooltip_world, R.drawable.pic_mask_world, Gravity.BOTTOM | Gravity.RIGHT);
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getTitleDrawableRes() {
        return R.drawable.pic_world_title;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_world, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.fragment_world_action_add) {
            CreateWorldActivity_.intent(getContext()).start();//TODO may ask for result, and remove refresh every time MainActivity onResume

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setPageInfo() {
        mPageName = "WorldFragment";
    }
}