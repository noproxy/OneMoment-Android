package co.yishun.onemoment.app;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import java.util.Queue;

import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.util.CircularFifoQueue;


/**
 * Created by Carlos on 2015/12/24.
 */
public class LogUtil {
    public static final int MAX_LOG = 100;
    private static Context mContext;
    private static Queue<String> logQueue = new CircularFifoQueue<>(MAX_LOG);

    static void setUp(Context context) {
        mContext = context.getApplicationContext();
    }

    public static int v(String tag, String msg) {
        return log(Log.VERBOSE, tag, msg, null);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return log(Log.VERBOSE, tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return log(Log.DEBUG, tag, msg, null);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return log(Log.DEBUG, tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return log(Log.INFO, tag, msg, null);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return log(Log.INFO, tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return log(Log.WARN, tag, msg, null);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return log(Log.WARN, tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return log(Log.ERROR, tag, msg, null);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return log(Log.ERROR, tag, msg, tr);
    }

    private static int log(int priority, String tag, String msg, Throwable tr) {
        //noinspection PointlessBooleanExpression
        if (BuildConfig.DEBUG || Constants.LOG_ENABLE) {
            return systemLog(priority, tag, msg, tr);
        } else
            switch (priority) {
                case Log.VERBOSE:
                case Log.DEBUG:
                    return 0;
                case Log.ERROR:
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = logQueue.poll()) != null) {
                        builder.append(line);
                        builder.append('\n');
                    }
                    String result = builder.toString();
                    if (!TextUtils.isEmpty(result))
                        MobclickAgent.reportError(mContext, result);
                    return 0;
                default:
                    String report = (priority == Log.WARN ? "WARN" : "INFO") + "/TAG: " + tag + ", MSG: " + msg;
                    logQueue.add(report);
                    return 0;
            }
    }

    private static int systemLog(int priority, String tag, String msg, Throwable tr) {
        switch (priority) {
            case Log.VERBOSE:
                return Log.v(tag, msg, tr);
            case Log.DEBUG:
                return Log.d(tag, msg, tr);
            case Log.INFO:
                return Log.i(tag, msg, tr);
            case Log.WARN:
                return Log.w(tag, msg, tr);
            default:
                return Log.e(tag, msg, tr);
        }
    }
}
