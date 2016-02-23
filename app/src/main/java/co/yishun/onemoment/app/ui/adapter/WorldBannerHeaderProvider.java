package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.daimajia.slider.library.SliderLayout;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/20.
 */
public class WorldBannerHeaderProvider extends BannerHeaderProvider {
    public WorldBannerHeaderProvider(Context context) {
        super(context);
    }

    @Override
    public View getHeaderView(ViewGroup viewGroup) {
        SliderLayout sliderLayout = (SliderLayout) super.getHeaderView(viewGroup);
        viewGroup.removeView(sliderLayout);

        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_header_world, viewGroup, false);
        linearLayout.addView(sliderLayout, 0);
        return linearLayout;
    }
}
