package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import co.yishun.onemoment.app.R;

/**
 * Created by Carlos on 2015/11/1.
 */
public class MomentCountDateView extends LinearLayout {
    public static final String DATE_FORMAT = "yyyy/MM/DD";
    public static final String HTML_PART0 = "<font color='";
    public static final String HTML_PART1 = "'>";
    public static final String HTML_PART2 = "</font>";
    TextView countTextView;
    TextView dateTextView;
    String countPrefix;
    String countSuffix;

    public MomentCountDateView(Context context) {
        super(context);
        init();
    }

    public MomentCountDateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MomentCountDateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MomentCountDateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.CENTER);
        LayoutInflater.from(getContext()).inflate(R.layout.merge_permission_switch, this, true);

        countTextView = (TextView) findViewById(R.id.countTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);

        Calendar calendar = Calendar.getInstance();
        String date = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(calendar);
        dateTextView.setText(date);

        Resources resources = getContext().getResources();
        countPrefix = resources.getString(R.string.activity_moment_create_count_text_prefix);
        countSuffix = resources.getString(R.string.activity_moment_create_count_text_suffix);

        countTextView.setText(getSpannedCountText(1));
    }

    private Spanned getSpannedCountText(int count) {
        //TODO set color from xml value
        final String prefixText = HTML_PART0 + getResources().getColor(R.color.colorAccent) + HTML_PART1 + countPrefix + HTML_PART2;
        final String suffixText = HTML_PART0 + getResources().getColor(R.color.colorAccent) + HTML_PART1 + countSuffix + HTML_PART2;
        final String countText = HTML_PART0 + getResources().getColor(R.color.textColorPrimaryDark) + HTML_PART1 + count + HTML_PART2;
        return Html.fromHtml(prefixText + " " + countText + " " + suffixText);
    }

    public void setMomentCount(int count) {
        countTextView.setText(getSpannedCountText(count));
    }
}
