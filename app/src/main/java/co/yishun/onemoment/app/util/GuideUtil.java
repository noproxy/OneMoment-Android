package co.yishun.onemoment.app.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.R;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

/**
 * Created by carlos on 4/6/16.
 */
@EBean
public class GuideUtil {


    @UiThread
    public void showGuide(Activity activity, View target, @LayoutRes int layoutRes, @DrawableRes int guideImageRes, int gravity) {
        Animation animation = new AlphaAnimation(0f, 1f);
        animation.setDuration(1000);
        animation.setFillAfter(true);

        @SuppressLint("InflateParams")
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(activity)
                .inflate(layoutRes, null);

        ImageView guide = (ImageView) viewGroup.findViewById(R.id.imageView);
        guide.setImageResource(guideImageRes);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) guide.getLayoutParams();

        @SuppressLint("RtlHardcoded")
        ToolTip toolTip = new ToolTip().setCustomView(viewGroup).setEnterAnimation(animation)
                .setGravity(gravity)
                .setBackgroundColor(Color.TRANSPARENT).setShadow(false);

        TourGuide tourGuide = TourGuide.init(activity).with(TourGuide.Technique.Click)
                .setToolTip(toolTip).playOn(target);

        toolTip.setOnClickListener(v -> tourGuide.cleanUp());

        hideGuide(tourGuide);
    }

    @UiThread(delay = 5000)
    void hideGuide(TourGuide tourGuide) {
        tourGuide.cleanUp();
    }
}
