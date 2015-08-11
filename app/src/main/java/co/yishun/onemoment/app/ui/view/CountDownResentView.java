package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import co.yishun.onemoment.app.R;

/**
 * Created by Carlos on 2015/8/11.
 */
public class CountDownResentView extends FrameLayout implements View.OnClickListener, CountDownView.OnCountDownEndListener {
    private CountDownView mCountDownView;
    private TextView mSuffixTextView;
    private TextView mPrefixTextView;
    private TextView mEndTextView;
    private ViewGroup mNotEndViewGroup;
    private CharSequence mEndText;
    private CharSequence mPrefix;
    private CharSequence mSuffix;
    private int mPrefixColor = getResources().getColor(android.R.color.darker_gray);
    private int mSuffixColor = getResources().getColor(android.R.color.darker_gray);
    private int mCountNumColor = getResources().getColor(android.R.color.white);
    private int mEndTextColor = getResources().getColor(android.R.color.darker_gray);
    private int mStartNum = 60;
    private boolean countEnded = true;
    private OnClickListenerWhenEnd mEndClicker;
    private OnClickListenerWhenNotEnd mNotEndClicker;
    private OnCountDownEndListener mEndListener;

    public CountDownResentView(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public CountDownResentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public CountDownResentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CountDownResentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CountDownResentView, defStyleAttr, defStyleRes);
        if (a.hasValue(R.styleable.CountDownResentView_prefixText))
            mPrefix = a.getString(R.styleable.CountDownResentView_prefixText);
        if (a.hasValue(R.styleable.CountDownResentView_suffixText))
            mSuffix = a.getString(R.styleable.CountDownResentView_suffixText);
        if (a.hasValue(R.styleable.CountDownResentView_endText))
            mEndText = a.getString(R.styleable.CountDownResentView_endText);

        mPrefixColor = a.getColor(R.styleable.CountDownResentView_prefixTextColor, mPrefixColor);
        mSuffixColor = a.getColor(R.styleable.CountDownResentView_suffixTextColor, mSuffixColor);
        mCountNumColor = a.getColor(R.styleable.CountDownResentView_countDownNumColor, mCountNumColor);
        mEndTextColor = a.getColor(R.styleable.CountDownResentView_endTextColor, mEndTextColor);

        mStartNum = a.getInt(R.styleable.CountDownResentView_startNum, mStartNum);

        a.recycle();

        LayoutInflater.from(getContext()).inflate(R.layout.merge_count_down_resent_view, this, true);
        mCountDownView = (CountDownView) findViewById(R.id.countDownView);
        mPrefixTextView = (TextView) findViewById(R.id.prefixTextView);
        mSuffixTextView = (TextView) findViewById(R.id.suffixTextView);
        mEndTextView = (TextView) findViewById(R.id.endTextView);
        mNotEndViewGroup = (ViewGroup) findViewById(R.id.notEndViewGroup);

        mPrefixTextView.setTextColor(mPrefixColor);
        mSuffixTextView.setTextColor(mSuffixColor);
        mEndTextView.setTextColor(mEndTextColor);

        mPrefixTextView.setText(mPrefix);
        mSuffixTextView.setText(mSuffix);
        mEndTextView.setText(mEndText);

        mCountDownView.setStartNumber(mStartNum);
        mCountDownView.setTextColor(mCountNumColor);

        this.setOnClickListener(this);
    }

    public void countDown() {
        countEnded = false;
        mEndTextView.setVisibility(INVISIBLE);
        mNotEndViewGroup.setVisibility(VISIBLE);
        mCountDownView.setOnCountDownEndListener(this);
        mCountDownView.startCountDown();
    }

    @Override
    public void onClick(View v) {
        if (countEnded && mEndClicker != null) mEndClicker.onClick(this);
        else if (mNotEndClicker != null) mNotEndClicker.onClick(this);
    }

    public void setOnCountDownEndListener(OnCountDownEndListener listener) {
        mEndListener = listener;
    }

    public void setOnClickListenerWhenEnd(OnClickListenerWhenEnd listenerWhenEnd) {
        mEndClicker = listenerWhenEnd;
    }

    public void setOnClickListenerWhenNotEnd(OnClickListenerWhenNotEnd listenerWhenNotEnd) {
        mNotEndClicker = listenerWhenNotEnd;
    }

    @Override
    public void onEnd() {
        countEnded = true;
        mEndTextView.setVisibility(VISIBLE);
        mNotEndViewGroup.setVisibility(INVISIBLE);
        if (mEndListener != null) {
            mEndListener.onEnd();
        }
    }

    public interface OnClickListenerWhenEnd {
        void onClick(CountDownResentView view);
    }

    public interface OnClickListenerWhenNotEnd {
        void onClick(CountDownResentView view);
    }

    public interface OnCountDownEndListener {
        void onEnd();
    }
}
