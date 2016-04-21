package co.yishun.onemoment.app.ui.view;

/**
 * Created by Jinge on 2015/11/5.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import co.yishun.onemoment.app.LogUtil;

public class ListPreferenceCompat extends ListPreference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LogUtil.d("ListPreferenceCompat", "create");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtil.d("ListPreferenceCompat", "create");
    }

    public ListPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtil.d("ListPreferenceCompat", "create");
    }

    public ListPreferenceCompat(Context context) {
        super(context);
        LogUtil.d("ListPreferenceCompat", "create");
    }

    // NOTE:
    // The framework forgot to call notifyChanged() in setValue() on previous versions of android.
    // This bug has been fixed in android-4.4_r0.7.
    // Commit: platform/frameworks/base/+/94c02a1a1a6d7e6900e5a459e9cc699b9510e5a2
    // Time: Tue Jul 23 14:43:37 2013 -0700
    //
    // However on previous versions, we have to workaround it by ourselves.
    @Override
    public void setValue(String value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super.setValue(value);
        } else {
            String oldValue = getValue();
            super.setValue(value);
            if (!TextUtils.equals(value, oldValue)) {
                notifyChanged();
            }
        }
    }
}