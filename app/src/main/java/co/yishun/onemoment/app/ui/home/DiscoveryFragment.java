package co.yishun.onemoment.app.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

import com.malinskiy.superrecyclerview.HeaderCompatibleSuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.Field;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.modelv4.World;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.WorldVideosActivity_;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.BannerHeaderProvider;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;
import co.yishun.onemoment.app.ui.controller.DiscoveryController_;
import co.yishun.onemoment.app.util.GuideUtil_;

/**
 * Created by yyz on 7/20/15.
 */
@EFragment(R.layout.fragment_discovery)
public class DiscoveryFragment extends ToolbarFragment implements AbstractRecyclerViewAdapter.OnItemClickListener<World> {

    private static final String TAG = "DiscoveryFragment";
    @ViewById
    HeaderCompatibleSuperRecyclerView recyclerView;


    @Override
    public void onResume() {
        super.onResume();
        BannerHeaderProvider.startSliderAutoCycle();

        Context context = getContext();
        if (context != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            // if the showed guide version is less than current version, show it
            //noinspection PointlessBooleanExpression
            if (Constants.FORCE_SHOW_GUIDE
                    || preferences.getInt(Constants.PrefKey.PREF_KEY_GUIDE_EXPLORE, -1) <
                    Constants.PrefKey.PREF_KEY_GUIDE_EXPLORE_CURRENT_VALUE) {
                showGuide();
                preferences.edit().putInt(Constants.PrefKey.PREF_KEY_GUIDE_EXPLORE,
                        Constants.PrefKey.PREF_KEY_GUIDE_EXPLORE_CURRENT_VALUE).apply();
            }
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    @UiThread(delay = 1000)
    void showGuide() {
        try {
            Field field = Toolbar.class.getDeclaredField("mLogoView");
            field.setAccessible(true);
            View view = (View) field.get(toolbar);
            Activity activity = getActivity();
            if (view != null && activity != null) {
                GuideUtil_.getInstance_(activity).showGuide(activity,
                        view, R.layout.layout_tooltip_discovery, R.drawable.pic_mask_explore, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
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
    public void onPause() {
        super.onPause();
        BannerHeaderProvider.stopSliderAutoCycle();
    }

    @AfterViews
    void setupViews() {
        DiscoveryController_.getInstance_(getContext()).setUp(getContext(), recyclerView, this);
    }

    @Override
    protected int getTitleDrawableRes() {
        return R.drawable.pic_explore_title;
    }

    @Override
    public void onClick(View view, World item) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        LogUtil.d(TAG, rect.toString());
        WorldVideosActivity_.intent(this).world(item).forWorld(false)
                .imageRect(rect).today(true).imageCorner(12).flags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .start();
    }

    @Override
    public void setPageInfo() {
        mPageName = "DiscoveryFragment";
    }
}
