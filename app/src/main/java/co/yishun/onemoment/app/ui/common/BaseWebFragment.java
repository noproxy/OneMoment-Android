package co.yishun.onemoment.app.ui.common;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import co.yishun.onemoment.app.LogUtil;

/**
 * Created by Jinge on 2016/1/21.
 */
@EFragment
public abstract class BaseWebFragment extends BaseFragment {
    private static final String TAG = "BaseWebFragment";

    @FragmentArg String mUrl = "file:///data/data/co.yishun.onemoment.app/files/build/pages/pages_list/pages_list.html";

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
        webView.loadUrl(mUrl);
        webView.setWebViewClient(new BaseWebClient());
    }

    protected void startNativeActivity() {

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

    private class BaseWebClient extends WebViewClient {
        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtil.d(TAG, url);
            if (url.startsWith("ysjs://")){
                String json = url.substring(7);
                UrlModel urlModel = new Gson().fromJson(json, UrlModel.class);
                if (TextUtils.equals(urlModel.call, "")) {

                }
                return false;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            LogUtil.d(TAG, event.getKeyCode() + " " + event.getAction());
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                return true;
            }
            return super.shouldOverrideKeyEvent(view, event);
        }
    }

    class UrlModel {
        String call;
        List<String> args;
    }
}
