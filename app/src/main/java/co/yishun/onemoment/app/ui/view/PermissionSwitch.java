package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewDebug;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import co.yishun.onemoment.app.R;

/**
 * Created by Carlos on 2015/11/1.
 */
public class PermissionSwitch extends LinearLayout {
    TextView hintText;
    Switch permissionSwitch;
    String onHintText;
    String offHintText;
    // off == private, on == public

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
        offHintText = getContext().getString(R.string.activity_moment_create_private_hint);
        onHintText = getContext().getString(R.string.activity_moment_create_public_hint);

        hintText = (TextView) findViewById(R.id.hintText);
        permissionSwitch = (Switch) findViewById(R.id.permissionSwitch);
        hintText.setText(permissionSwitch.isChecked() ? onHintText : offHintText);
    }

    @ViewDebug.ExportedProperty
    public boolean isChecked() {
        return permissionSwitch.isChecked();
    }

    public void setChecked(boolean checked) {
        permissionSwitch.setChecked(checked);
        hintText.setText(checked ? onHintText : offHintText);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        permissionSwitch.setOnCheckedChangeListener(listener);
    }
}
