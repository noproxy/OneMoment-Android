package co.yishun.library.calendarlibrary;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import java.util.Calendar;

/**
 * Created by yyz on 7/19/15.
 */
public class InternalMonthAdapter implements Adapter {
    private final Calendar passCalendar;
    private final Calendar NOW = Calendar.getInstance();
    private final MomentMonthView.MonthAdapter mAdapter;
    private int lastDay;

    protected InternalMonthAdapter(Calendar calendar, MomentMonthView.MonthAdapter adapter) {
        passCalendar = calendar;
        mAdapter = adapter;
        updateCalendar(calendar);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        throw new UnsupportedOperationException("Not implement");
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        throw new UnsupportedOperationException("Not implement");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getCount() {
        return lastDay;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DayView rootView = (DayView) convertView;
        if (convertView == null) {
            rootView = new DayView(parent.getContext(), position + 1);
        }

        passCalendar.set(Calendar.DAY_OF_MONTH, position + 1);

        if (passCalendar.get(Calendar.YEAR) == NOW.get(Calendar.YEAR)
                && passCalendar.get(Calendar.MONTH) == NOW.get(Calendar.MONTH)
                && passCalendar.get(Calendar.DAY_OF_MONTH) == NOW.get(Calendar.DAY_OF_MONTH)
                ) {
            rootView.setTimeStatus(DayView.TimeStatus.TODAY);
            rootView.setEnabled(true);
        } else if (passCalendar.compareTo(NOW) > 0) {
            rootView.setTimeStatus(DayView.TimeStatus.FUTURE);
            rootView.setEnabled(false);
        } else {
            rootView.setTimeStatus(DayView.TimeStatus.PAST);
            rootView.setEnabled(false);
        }

        if (mAdapter != null) {
            mAdapter.onBindView(passCalendar, rootView);
        }
        return rootView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    public void updateCalendar(Calendar calendar) {
        lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        passCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
    }
}
