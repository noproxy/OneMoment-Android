package co.yishun.onemoment.app.ui.common;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;

/**
 * Created by Jinge on 2016/1/21.
 */
@EFragment
public abstract class BaseWebFragment extends BaseFragment {
    private static final String TAG = "BaseWebFragment";
    @ViewById protected WebView webView;
    protected BaseActivity mActivity;

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) getActivity();
    }

    @Override public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @CallSuper protected void setUpWebView() {
        webView.loadUrl("http://www.baidu.com/");

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LogUtil.d(TAG, url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return false;
    }
}
