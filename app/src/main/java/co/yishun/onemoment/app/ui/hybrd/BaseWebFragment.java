package co.yishun.onemoment.app.ui.hybrd;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;

import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.SplashActivity;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.common.BaseFragment;


/**
 * Created by Jinge on 2016/1/21.
 */
@EFragment
public abstract class BaseWebFragment extends BaseFragment {
    public static final String TAG_WEB = "web";

    private static final String TAG = "BaseWebFragment";

    @ViewById protected WebView webView;

    protected BaseActivity mActivity;
    protected MaterialDialog dialog;
    protected File mHybrdDir;
    protected int posX;
    protected int posY;
    protected float touchX;
    protected float touchY;

    @FragmentArg protected String mUrl;

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) getActivity();
    }

    @Override public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @AfterInject void setDefault() {
        mHybrdDir = FileUtil.getInternalFile(mActivity, Constants.HYBRD_UNZIP_DIR);
        if (TextUtils.isEmpty(mUrl)) {
            mUrl = Constants.FILE_URL_PREFIX + new File(mHybrdDir, "build/pages/world/world.html").getPath();
            LogUtil.i(TAG, "url is null, load default");
        }

        int lastUpdateTime = mActivity.getSharedPreferences(SplashActivity.RUNTIME_PREFERENCE, Context.MODE_PRIVATE)
                .getInt(SplashActivity.PREFERENCE_HYBRD_UPDATE_TIME, 0);
        if (mUrl.startsWith(Constants.FILE_URL_PREFIX)) mUrl += "?time=" + lastUpdateTime;
    }

    @SuppressLint("SetJavaScriptEnabled") @CallSuper protected void setUpWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.setWebViewClient(new BaseWebClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (mActivity.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
        webView.setOnTouchListener((v, event) -> {
            touchX = event.getX();
            touchY = event.getY();
            return false;
        });
        webView.post(() -> {
            int location[] = new int[2];
            webView.getLocationOnScreen(location);
            posX = location[0];
            posY = location[1];
        });
        webView.loadUrl(mUrl);
    }

    private boolean webGetEnv(List<String> args) {
        String env = BuildConfig.DEBUG ? "development" : "production";
        webView.loadUrl(toJs(env));
        return true;
    }

    private boolean webGetAccount(List<String> args) {
        webView.loadUrl(toJs(AccountManager.getUserInfo(mActivity)));
        return true;
    }

    private boolean webGetAccountId(List<String> args) {
        webView.loadUrl(toJs(AccountManager.getUserInfo(mActivity)._id));
        return true;
    }

    private boolean webLog(List<String> args) {
        LogUtil.i(TAG, "js log : " + args.get(0));
        return true;
    }

    private boolean webAlert(List<String> args) {
        String type = args.get(0);
        if (dialog != null && dialog.isShowing()) dialog.hide();
        if (TextUtils.equals(type, "alert")) {
            dialog = new MaterialDialog.Builder(mActivity).theme(Theme.LIGHT)
                    .content(args.get(1)).progress(true, 0).build();
            dialog.show();
        } else if (TextUtils.equals(type, "message")) {
            mActivity.showSnackMsg(args.get(1));
        } else {
            LogUtil.e(TAG, "unhandled alert type");
        }
        return true;
    }

    private boolean webCancelAlert(List<String> args) {
        if (dialog != null && dialog.isShowing()) {
            dialog.hide();
        }
        return true;
    }

    private boolean webFinish(List<String> args) {
        String type = args.get(0);
        if (TextUtils.equals(type, "choose_world")) {
            //TODO add result after choose a world
        } else {
            LogUtil.e(TAG, "unhandled finish type");
        }
        mActivity.finish();
        return true;
    }

    public void sendFinish() {
        String result = "javascript:ctx.finish()";
        LogUtil.d(TAG, "load js : " + result);
        webView.loadUrl(result);
    }

    public String toJs(Object o) {
        String arg;
        arg = new Gson().toJson(o);
        arg = arg.replace("\"", "\\\"");

        String result = "javascript:ctx.androidreturn('[" + arg + "]')";
        LogUtil.d(TAG, "load js : " + result);
        return result;
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

    public boolean canGoBack() {
        return webView.canGoBack();
    }

    public void goBack() {
        webView.goBack();
    }

    public File getHybrdDir() {
        return mHybrdDir;
    }

    private class BaseWebClient extends WebViewClient {

        private HybrdUrlHandler urlHandler = new HybrdUrlHandler() {
            @Override protected boolean handleInnerUrl(UrlModel urlModel) {
                if (TextUtils.equals(urlModel.call, FUNC_GET_ENV)) {
                    return webGetEnv(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_GET_ACCOUNT)) {
                    return webGetAccount(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_GET_ACCOUNT_ID)) {
                    return webGetAccountId(urlModel.args);
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
        };

        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            url = HybrdUrlHandler.urlDecode(url);
            LogUtil.d(TAG, url);
            return urlHandler.handleUrl(mActivity, url, (int) (touchX + posX), (int) (touchY + posY))
                    || super.shouldOverrideUrlLoading(view, url);
        }

        @SuppressWarnings("deprecation") @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            LogUtil.d(TAG, description + "  " + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @TargetApi(23)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            LogUtil.d(TAG, "error : " + request.getUrl() + error.toString());
            super.onReceivedError(view, request, error);
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
}
