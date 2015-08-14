package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.squareup.picasso.Picasso;

import java.util.List;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.Banner;

/**
 * Created by Carlos on 2015/8/14.
 */
public class BannerHeaderProvider implements HeaderRecyclerAdapter.HeaderProvider {
    SliderLayout worldSlider;
    private Context context;


    public BannerHeaderProvider(Context context) {
        this.context = context;
    }

    @Override
    public View getHeaderView(ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.layout_world_header_slider, viewGroup, false);
        worldSlider = (SliderLayout) rootView.findViewById(R.id.worldSlider);
        worldSlider.addSlider(generateDefaultSliderView());
        worldSlider.addSlider(generateDefaultSliderView());
        worldSlider.addSlider(generateDefaultSliderView());
        return rootView;
    }

    private BaseSliderView generateBannerSliderView(Banner banner) {
        return new BaseSliderView(context.getApplicationContext()) {
            @Override
            public View getView() {
                ImageView imageView = (ImageView) View.inflate(context, R.layout.layout_slider_image, null);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Picasso.with(context).load(banner.imageUrl).into(imageView);
                imageView.setOnClickListener(v -> {
                    String url = banner.href;
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://" + url;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(browserIntent);
                });
                return imageView;
            }
        };
    }

    private BaseSliderView generateDefaultSliderView() {
        return new BaseSliderView(context.getApplicationContext()) {
            @Override
            public View getView() {
                ImageView imageView = (ImageView) View.inflate(context, R.layout.layout_slider_image, null);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Picasso.with(context).load(R.drawable.pic_slider_loading).into(imageView);
                return imageView;
            }
        };
    }

    public void setupBanners(List<Banner> banners) {
        worldSlider.removeAllSliders();
        for (Banner b : banners) {
            worldSlider.addSlider(generateBannerSliderView(b));
        }
    }
}
