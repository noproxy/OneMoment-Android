package co.yishun.onemoment.app.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.squareup.picasso.Picasso;

import java.util.List;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.ui.hybrd.CommonWebActivity_;

/**
 * Created by Carlos on 2015/8/14.
 */
public class BannerHeaderProvider implements HeaderRecyclerAdapter.HeaderProvider {
    SliderLayout worldSlider;
    protected Context context;


    public BannerHeaderProvider(Context context) {
        this.context = context;
    }

    @Override
    public View getHeaderView(ViewGroup viewGroup) {
        worldSlider = new SliderLayout(context);
        ViewGroup.LayoutParams params  = new ViewGroup.LayoutParams(viewGroup.getWidth(), (int) (viewGroup.getWidth() / 16.0f * 9));
        viewGroup.addView(worldSlider, params);
        worldSlider.addSlider(generateDefaultSliderView());
        worldSlider.addSlider(generateDefaultSliderView());
        worldSlider.addSlider(generateDefaultSliderView());
        return worldSlider;
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
//                    UrlDetailActivity_.intent(context).url(url).start();
                    CommonWebActivity_.intent(context).url(url).start();
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
                Picasso.with(context).load(R.drawable.pic_banner_default).into(imageView);
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
