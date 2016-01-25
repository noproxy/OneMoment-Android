package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Carlos on 2015/10/4.
 */
public class TodayMomentView extends RelativeLayout {
    private final String NO_MOMENT = getResources().getString(R.string.view_today_moment_no_moment);
    private final String NO_TAG = getResources().getString(R.string.view_today_moment_no_tag);
    private TextView mDateTextView;
    private TextView mTagTextView;
    private TextView mMomentNumTextView;
    private TextView mStartPlayTextView;
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
        mMomentNumTextView = (TextView) findViewById(R.id.momentNumText);
        mStartPlayTextView = (TextView) findViewById(R.id.startPlayTextView);
        mMomentImageView = (CircleImageView) findViewById(R.id.momentPreviewImageView);
        setTodayMoment(TodayMoment.noMomentToday(new Date()));
    }

    public void setTodayMoment(@NonNull TodayMoment todayMoment) {
        mDateTextView.setText(todayMoment.date);
        if (todayMoment.moment != null) {
            Moment moment = todayMoment.moment;
            Picasso.with(getContext()).load(new File(moment.getThumbPath())).error(R.drawable.pic_world_default).into(mMomentImageView);
            mTagTextView.setText(todayMoment.tag == null ? NO_TAG : todayMoment.tag);

            mMomentNumTextView.setVisibility(VISIBLE);

            Locale locale = getResources().getConfiguration().locale;
            String num;
            if (locale.getLanguage().equals(Locale.CHINA.getLanguage())) {
                num = String.valueOf(todayMoment.momentIndex);
            } else {
                num = Util.ordinal((int) todayMoment.momentIndex);
            }

            String prefix = getResources().getString(R.string.activity_moment_create_count_text_prefix);
            String suffix = getResources().getString(R.string.activity_moment_create_count_text_suffix);
            int colorAccent = getResources().getColor(R.color.colorAccent);
            SpannableString spannableString = new SpannableString(prefix + num + suffix);
            spannableString.setSpan(new ForegroundColorSpan(colorAccent), 0, prefix.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(colorAccent), prefix.length() + num.length(), prefix.length() + num.length() + suffix.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mMomentNumTextView.setText(spannableString);

            mStartPlayTextView.setVisibility(VISIBLE);
        } else {
            mMomentImageView.setImageResource(R.drawable.ic_diary_no_moment);
            mTagTextView.setText(NO_MOMENT);
            mMomentNumTextView.setVisibility(INVISIBLE);
            mStartPlayTextView.setVisibility(INVISIBLE);
        }
    }

    public static class TodayMoment {
        private static final String TAG = "TodayMoment";
        private String date;
        private Moment moment;
        private String tag;
        private long momentIndex;

        private TodayMoment() {
        }

        public static TodayMoment noMomentToday(Date date) {
            LogUtil.i(TAG, "no moment today: " + date);
            TodayMoment todayMoment = new TodayMoment();
            todayMoment.date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(date);
            todayMoment.tag = null;
            return todayMoment;
        }

        public static TodayMoment momentTodayIs(Moment moment, long count) {
            LogUtil.i(TAG, "momentTodayIs: " + moment);
            TodayMoment todayMoment = new TodayMoment();
            todayMoment.moment = moment;
            todayMoment.momentIndex = count;
            todayMoment.date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Long.parseLong(moment.getUnixTimeStamp()) * 1000);
            List<OMLocalVideoTag> tags = Moment.readTags(moment);
            if (tags != null && tags.size() > 0) {
                todayMoment.tag = tags.get(0).getTagText();
            } else {
                todayMoment.tag = null;
            }
            return todayMoment;
        }
    }
}
