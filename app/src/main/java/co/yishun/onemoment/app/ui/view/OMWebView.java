package co.yishun.onemoment.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.LogUtil;

/**
 * Created by Carlos on 2016/3/18.
 */
public class OMWebView extends WebView {
    private static final String TAG = "OMWebView";
    private static WeakReference<OnBlockKeyListener> mListener = new WeakReference<>(null);

    public OMWebView(Context context) {
        super(context);
    }

    public OMWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OMWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OMWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public OMWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
    }

    public static void setListener(OnBlockKeyListener listener) {
        OMWebView.mListener = new WeakReference<>(listener);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new BaseInputConnection(this, false) {
            @Override
            public boolean performEditorAction(int actionCode) {
                if (actionCode == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    LogUtil.i(TAG, "action go");
                    onBlockKey();
                    return true;
                }
                return super.performEditorAction(actionCode);
            }
        };
//        return super.onCreateInputConnection(outAttrs);
    }

    private void onBlockKey() {
        OnBlockKeyListener listener = mListener.get();
        if (listener != null) {
            listener.onBlockKey();
        }
    }

    public interface OnBlockKeyListener {
        void onBlockKey();
    }
}
