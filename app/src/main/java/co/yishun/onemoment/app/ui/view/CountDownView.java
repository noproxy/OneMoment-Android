package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/25/15.
 */
public class CountDownView extends TextView {
    public static final int DEFAULT_START_NUMBER = 60;
    public static final int COUNT_DOWN_DELAY = 1000;
    private static final int NOT_START = 0;
    private static final int STARTED = 1;
    private static final int END = 2;
    private AtomicInteger startNumber;
    private int status = NOT_START;
    private Handler handler = new Handler();
    private OnCountDownEndListener listener;
    private Runnable runnable = new Runnable() {
        @Override public void run() {
            int text = startNumber.decrementAndGet();
            CountDownView.this.setText(String.valueOf(startNumber.get()));
            if (text <= 0) {
                status = END;
                onEnd();
            } else {
                handler.postDelayed(runnable, COUNT_DOWN_DELAY);
            }
        }
    };

    public CountDownView(Context context) {
        super(context);
        init(null, 0);
    }


    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) public CountDownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, 0);
    }

    private void onEnd() {
        if (listener != null) {
            listener.onEnd();
        }
    }

    public void setOnCountDownEndListener(OnCountDownEndListener listener) {
        this.listener = listener;
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CountDownView, defStyleAttr, 0);
        startNumber = new AtomicInteger(a.getInt(R.styleable.CountDownView_startNumber, DEFAULT_START_NUMBER));
        a.recycle();
        super.setText(String.valueOf(startNumber.get()));
    }

    public void setStartNumber(int number) {
        if (status == STARTED) {
            throw new IllegalStateException("You can't setText after count down start");
        } else if (number <= 0) {
            throw new IllegalArgumentException("You can only use number larger than 0");
        } else {
            status = NOT_START;
            startNumber.set(number);
            super.setText(String.valueOf(number));
        }
    }

    public void startCountDown() {
        if (status == STARTED) {
            LogUtil.w("CountDownView", "start count down but has been started, ignored!");
            return;
        } else if (status == END) {
            LogUtil.i("CountDownView", "start count down again.");
        } else {
            LogUtil.i("CountDownView", "start count down.");
        }
        status = STARTED;
        handler.postDelayed(runnable, COUNT_DOWN_DELAY);

    }

    public interface OnCountDownEndListener {
        void onEnd();
    }
}
