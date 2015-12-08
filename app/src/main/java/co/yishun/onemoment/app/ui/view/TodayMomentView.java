package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
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
        setTodayMoment(TodayMoment.noMomentToday(new Date()));
    }

    public void setTodayMoment(@NonNull TodayMoment todayMoment) {
        mDateTextView.setText(todayMoment.date);
        mTagTextView.setText(todayMoment.tag == null ? NO_MOMENT : todayMoment.tag);
        if (todayMoment.moment != null) {
            Moment moment = todayMoment.moment;
            Picasso.with(getContext()).load(new File(moment.getThumbPath())).into(mMomentImageView);
        } else {
            Picasso.with(getContext()).load(R.drawable.pic_diary_none).resize(64 * 3, 64 * 3).centerInside().into(mMomentImageView);
        }
    }

    public static class TodayMoment {
        private static final String TAG = "TodayMoment";
        private String date;
        private Moment moment;
        private String tag;

        private TodayMoment() {
        }

        public static TodayMoment noMomentToday(Date date) {
            Log.i(TAG, "no moment today: " + date);
            TodayMoment todayMoment = new TodayMoment();
            todayMoment.date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(date);
            todayMoment.tag = null;
            return todayMoment;
        }

        public static TodayMoment momentTodayIs(Moment moment) {
            Log.i(TAG, "momentTodayIs: " + moment);
            TodayMoment todayMoment = new TodayMoment();
            todayMoment.moment = moment;
            todayMoment.date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(moment.getUnixTimeStamp() * 1000);
            List<OMLocalVideoTag> tags = Moment.readTags(moment);
            if (tags != null && tags.size() > 0) {
                todayMoment.tag = tags.get(0).getTagText();
            } else {
                todayMoment.tag = "";
            }
            return todayMoment;
        }
    }
}
