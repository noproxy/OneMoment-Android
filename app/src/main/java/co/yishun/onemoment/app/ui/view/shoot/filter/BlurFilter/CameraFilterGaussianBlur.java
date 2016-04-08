package co.yishun.onemoment.app.ui.view.shoot.filter.BlurFilter;

import android.content.Context;

import co.yishun.onemoment.app.ui.view.shoot.filter.CameraFilter;
import co.yishun.onemoment.app.ui.view.shoot.filter.FilterGroup;


public class CameraFilterGaussianBlur extends FilterGroup<CameraFilter> {

    public CameraFilterGaussianBlur(Context context, float blur) {
        super();
        addFilter(new CameraFilterGaussianSingleBlur(context, blur, false));
        addFilter(new CameraFilterGaussianSingleBlur(context, blur, true));
    }
}
