package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.model.Moment;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Carlos on 2015/10/4.
 */
public class TodayMomentView extends RelativeLayout {
    private final String NO_MOMENT = getResources().getString(R.string.view_today_moment_no_moment);
    private TextView mDateTextView;
    private TextView mTagTextView;
    private CircleImageView mMomentImageView;

    public TodayMomentView(Context context) {
        super(context);
        init();
    }

    public TodayMomentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TodayMomentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TodayMomentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.merge_today_moment, this);

        mDateTextView = (TextView) findViewById(R.id.dateTextView);
        mTagTextView = (TextView) findViewById(R.id.tagTextView);
        mMomentImageView = (CircleImageView) findViewById(R.id.momentPreviewImageView);
    }

    public void setTodayMoment(@NonNull TodayMoment todayMoment) {
        mDateTextView.setText(todayMoment.date);
        if (todayMoment.moment != null) {
            Moment moment = todayMoment.moment;
            Picasso.with(getContext()).load(moment.getPath()//TODO  add "file://" ?
            ).into(mMomentImageView);
            //TODO add tag into Moment and set tag
        }
    }

    public static class TodayMoment {
        private String date;
        private Moment moment;

        private TodayMoment() {
        }

        public static TodayMoment noMomentToday(Date date) {
            TodayMoment todayMoment = new TodayMoment();
            todayMoment.date = new SimpleDateFormat("yyyy/MM/DD", Locale.getDefault()).format(date);
            return todayMoment;
        }

        public static TodayMoment momentTodayIs(Moment moment) {
            TodayMoment todayMoment = new TodayMoment();
            todayMoment.moment = moment;
            todayMoment.date = new SimpleDateFormat("yyyy/MM/DD", Locale.getDefault()).format(moment.getTimeStamp() * 1000);
            return todayMoment;
        }
    }
}
