package co.yishun.onemoment.app.ui.view;

/**
 * Created on 2015/12/16.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimePreference extends DialogPreference {
    private static final String TAG = "TimePreference";
    private int lastHour = 0;
    private int lastMinute = 0;
    private TimePicker picker = null;

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        DateFormat.is24HourFormat(getContext());
        picker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
        Log.d(TAG, DateFormat.is24HourFormat(getContext()) + "");
        return picker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, lastHour);
            calendar.set(Calendar.MINUTE, lastMinute);
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());

            if (callChangeListener(time)) {
                persistString(time);
            }
            setSummary(time);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            time = defaultValue.toString();
        }

        String[] timeString = time.split(":");
        lastHour = Integer.valueOf(timeString[0]);
        lastMinute = Integer.valueOf(timeString[1]);

        setSummary(time);
    }
}
