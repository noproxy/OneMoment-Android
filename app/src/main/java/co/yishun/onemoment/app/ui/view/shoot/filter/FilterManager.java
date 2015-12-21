package co.yishun.onemoment.app.ui.view.shoot.filter;

import android.content.Context;

import co.yishun.onemoment.app.R;


public class FilterManager {

    private static int[] mCurveArrays = new int[]{
            R.raw.cross_1, R.raw.cross_2, R.raw.cross_3, R.raw.cross_4, R.raw.cross_5,
            R.raw.cross_6, R.raw.cross_7, R.raw.cross_8, R.raw.cross_9, R.raw.cross_10,
            R.raw.cross_11,
    };

    private FilterManager() {
    }

    public static IFilter getCameraFilter(FilterType filterType, Context context) {
        switch (filterType) {
            case Normal:
                return new CameraFilter(context);
//            case Blend:
//                return new CameraFilterBlend(context, R.drawable.mask);
//            case SoftLight:
//                return new CameraFilterBlendSoftLight(context, R.drawable.mask);
            default:
                int index = filterType.ordinal() - 1;
                return new CameraFilterToneCurve(context, context.getResources().openRawResource(mCurveArrays[index % 10]));
        }
    }

    public enum FilterType {
        Normal,
        //        Blend, SoftLight,
        ToneCurve0,
        ToneCurve1,
        ToneCurve2,
//        ToneCurve3,
//        ToneCurve4,
//        ToneCurve5,
//        ToneCurve6,
//        ToneCurve7,
//        ToneCurve8,
//        ToneCurve9,
//        ToneCurve10
    }
}
