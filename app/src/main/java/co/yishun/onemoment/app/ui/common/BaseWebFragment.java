package co.yishun.onemoment.app.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.view.KeyEvent;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created by Jinge on 2016/1/21.
 */
@EFragment
public abstract class BaseWebFragment extends BaseFragment {
    public static final String URL_PREFIX = Constants.APP_URL_PREFIX;
    public static final String FUNC_GET_ACCOUNT_ID = "get_account_id";
    public static final String FUNC_GET_ACCOUNT = "get_account";
    public static final String FUNC_JUMP = "jump";
    public static final String FUNC_LOG = "log";
    public static final String FUNC_ALERT = "alert";
    public static final String FUNC_CANCEL_AlERT = "cancel_alert";
    public static final String FUNC_FINISH = "finish";
    public static final String FUNC_GET_ENV = "getEnv";


    private static final String TAG = "BaseWebFragment";

    @ViewById protected WebView webView;

    protected BaseActivity mActivity;
    protected MaterialDialog dialog;
    protected File mHybrdDir;

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
    }

    @SuppressLint("SetJavaScriptEnabled") @CallSuper protected void setUpWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new BaseWebClient());
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
        if (TextUtils.equals(type, "choose_world")) {
            //TODO add result after choose a world
            return true;
        } else {
            mActivity.finish();
        }
        LogUtil.e(TAG, "unhandled finish type");
        return true;
    }

    public String decode(String raw) {
        String result;
        try {
            result = URLDecoder.decode(raw, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = "decoder error";
            LogUtil.e(TAG, "decoder error : " + raw);
        }
        return result;
    }

    public List<String> decode(List<String> rawStrings) {
        List<String> results = new ArrayList<>();
        for (String s : rawStrings) results.add(decode(s));
        return results;
    }

    public String toJs(Object o) {
        String arg;
        if (o instanceof String) {
            arg = (String) o;
        } else {
            arg = new Gson().toJson(o);
            arg = arg.replace("\"", "\\\"");
        }
        String result = "ctx.androidreturn('" + arg + "')";
        LogUtil.d(TAG, "decode : " + result);
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

    public File getHybrdDir() {
        return mHybrdDir;
    }

    private class BaseWebClient extends WebViewClient {
        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtil.d(TAG, url);
            if (url.startsWith(URL_PREFIX)) {
                String json = url.substring(URL_PREFIX.length());
                UrlModel urlModel = new Gson().fromJson(json, UrlModel.class);
                urlModel.args = decode(urlModel.args);

                if (TextUtils.equals(urlModel.call, FUNC_GET_ENV)) {
                    return webGetEnv(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_GET_ACCOUNT)) {
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
}
