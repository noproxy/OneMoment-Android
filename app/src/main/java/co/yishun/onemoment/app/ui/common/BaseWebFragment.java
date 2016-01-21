package co.yishun.onemoment.app.ui.common;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
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
    public static final String URL_PREFIX = "ysjs://";
    public static final String FUNC_GET_ACCOUNT_ID = "get_account_id";
    public static final String FUNC_GET_ACCOUNT = "get_account";
    public static final String FUNC_JUMP = "jump";
    public static final String FUNC_LOG = "log";
    public static final String FUNC_ALERT = "alert";
    public static final String FUNC_CANCEL_AlERT = "cancel_alert";
    public static final String FUNC_FINISH = "finish";


    private static final String TAG = "BaseWebFragment";
    @ViewById protected WebView webView;
    protected BaseActivity mActivity;
    protected MaterialDialog dialog;
    @FragmentArg
    String mUrl = "file:///data/data/co.yishun.onemoment.app/files/build/pages/mine/mine.html";

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) getActivity();
    }

    @Override public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @CallSuper protected void setUpWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new BaseWebClient());
        webView.loadUrl(mUrl);
        webView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                webGetAccountId(null);
            }
        });
    }

    private boolean webGetAccount(List<String> args) {
        webView.loadUrl("javascript:ctx.androidreturn('[{'_id':'561866817d40b548c05e4c7f'}]')");
        return true;
    }

    private boolean webGetAccountId(List<String> args) {
        webView.loadUrl("ctx.iosjsbridgereturn('[{'_id':'561866817d40b548c05e4c7f'}]')");
        return true;
    }

    private boolean webJump(List<String> args) {
        String des = args.get(0);
        if (TextUtils.equals(des, "web")) {
            CommonWebActivity_.intent(mActivity).title(args.get(1)).url(args.get(2)).start();
            return true;
        }
        LogUtil.e(TAG, "unhandled jump type");
        return true;
    }

    private boolean webLog(List<String> args) {
        LogUtil.i(TAG, "webLog call");
        return true;
    }

    private boolean webAlert(List<String> args) {
        String type = args.get(0);
        if (dialog != null && dialog.isShowing()) dialog.hide();
        if (TextUtils.equals(type, "alert")) {
            dialog = new MaterialDialog.Builder(mActivity).theme(Theme.LIGHT)
                    .content(args.get(1)).progress(true, 0).build();
            dialog.show();
            return true;
        } else if (TextUtils.equals(type, "message")) {
            dialog = new MaterialDialog.Builder(mActivity).theme(Theme.LIGHT).content(args.get(1)).build();
            dialog.show();
            return true;
        }
        LogUtil.e(TAG, "unhandled alert type");
        return true;
    }

    private boolean webCancelAlert(List<String> args) {
        if (dialog.isShowing()) {
            dialog.hide();
        }
        return true;
    }

    private boolean webFinish(List<String> args) {
        String type = args.get(0);
        if (TextUtils.equals(type, "choose_world")){
            //TODO add result after choose a world
            return true;
        }
        LogUtil.e(TAG, "unhandled finish type");
        return true;
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
            if (url.startsWith(URL_PREFIX)) {
                String json = url.substring(URL_PREFIX.length());
                UrlModel urlModel = new Gson().fromJson(json, UrlModel.class);
                if (TextUtils.equals(urlModel.call, FUNC_GET_ACCOUNT)) {
                    return webGetAccount(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_GET_ACCOUNT_ID)) {
                    return webGetAccountId(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_JUMP)) {
                    return webJump(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_LOG)) {
                    return webLog(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_ALERT)) {
                    return webAlert(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_CANCEL_AlERT)) {
                    return webCancelAlert(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_FINISH)) {
                    return webFinish(urlModel.args);
                } else {
                    LogUtil.i(TAG, "unknown call type");
                }
                return true;
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

    public class JSInterface {

        private WebView mAppView;

        public JSInterface(WebView appView) {
            this.mAppView = appView;
        }

        public void doEchoTest(String echo) {
            Toast toast = Toast.makeText(mAppView.getContext(), echo, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
