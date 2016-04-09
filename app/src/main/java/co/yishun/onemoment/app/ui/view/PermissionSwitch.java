package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewDebug;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;

/**
 * Created by Carlos on 2015/11/1.
 */
public class PermissionSwitch extends LinearLayout {
    private static final String TAG = "PermissionSwitch";
    TextView hintText;
    SwitchCompat permissionSwitch;
    TextView publicText;
    TextView privateText;
    String onHintText;
    String offHintText;
    private int GRAY;
    private int ORANGE;
    // off == public, on == private

    public PermissionSwitch(Context context) {
        super(context);
        init();
    }

    public PermissionSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PermissionSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PermissionSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.CENTER);
        LayoutInflater.from(getContext()).inflate(R.layout.merge_permission_switch, this, true);
        offHintText = getContext().getString(R.string.activity_moment_create_public_hint);
        onHintText = getContext().getString(R.string.activity_moment_create_private_hint);
        GRAY = getResources().getColor(R.color.colorGray);
        ORANGE = getResources().getColor(R.color.colorOrange);

        hintText = (TextView) findViewById(R.id.hintText);
        permissionSwitch = (SwitchCompat) findViewById(R.id.permissionSwitch);
        publicText = (TextView) findViewById(R.id.permissionPublicText);
        privateText = (TextView) findViewById(R.id.permissionPrivateText);
        publicText.setTextColor(ORANGE);
        privateText.setTextColor(GRAY);
        hintText.setText(permissionSwitch.isChecked() ? onHintText : offHintText);

        // for different screen size
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        LogUtil.d(TAG, metrics.widthPixels + "  " + metrics.heightPixels);
        if (metrics.widthPixels * 1f / metrics.heightPixels > 0.62f) {
            hintText.setVisibility(GONE);
        } else if (metrics.widthPixels * 1f / metrics.heightPixels > 0.6f) {
            LayoutParams params = (LayoutParams) hintText.getLayoutParams();
            params.topMargin /= 2;
            hintText.setLayoutParams(params);
        }
    }

    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return permissionSwitch.isChecked();
    }

    public void setChecked(boolean checked) {
        permissionSwitch.setChecked(checked);
        publicText.setTextColor(checked ? GRAY : ORANGE);
        privateText.setTextColor(checked ? ORANGE : GRAY);
        hintText.setText(checked ? onHintText : offHintText);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        permissionSwitch.setOnCheckedChangeListener(listener);
    }
}
