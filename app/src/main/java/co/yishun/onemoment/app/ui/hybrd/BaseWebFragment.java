package co.yishun.onemoment.app.ui.hybrd;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.authentication.OneMomentClientV4;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.ui.SplashActivity;
import co.yishun.onemoment.app.ui.WorldPickActivity;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.common.BaseFragment;
import co.yishun.onemoment.app.util.GsonFactory;


/**
 * Created by Jinge on 2016/1/21.
 */
@EFragment
public abstract class BaseWebFragment extends BaseFragment {
    public static final String TAG_WEB = "web";
    private static final String TAG = "BaseWebFragment";
    private static boolean needGlobalRefresh = false;
    @ViewById
    protected SwipeRefreshLayout swipeRefreshLayout;
    @ViewById
    protected WebView webView;
    protected BaseActivity mActivity;
    protected MaterialDialog dialog;
    protected File mHybrdDir;
    protected int posX;
    protected int posY;
    protected float touchX;
    protected float touchY;
    protected boolean mRefreshable;
    @FragmentArg
    protected String mUrl;
    @FragmentArg
    protected String mArg;
    private Context mApplicationContext;

    public static void invalidateWeb() {
        needGlobalRefresh = true;
    }

    @Override
    public Context getContext() {
        return super.getContext() == null ? mApplicationContext : super.getContext();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) getActivity();
        if (mApplicationContext == null) {
            mApplicationContext = mActivity.getApplicationContext();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.clearCache(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRefreshable && needGlobalRefresh) {
            LogUtil.i(TAG, "consume refresh: " + mUrl);
            needGlobalRefresh = false;
            reload();
        }
    }

    @AfterInject
    void setDefault() {
        mHybrdDir = FileUtil.getInternalFile(getContext(), Constants.HYBRD_UNZIP_DIR);
        if (TextUtils.isEmpty(mUrl)) {
            mUrl = Constants.FILE_URL_PREFIX +
                    new File(mHybrdDir, "build/pages/world/world.html").getPath();
            LogUtil.i(TAG, "url is null, load default");
        }

        int lastUpdateTime = getContext().getSharedPreferences(SplashActivity.RUNTIME_PREFERENCE,
                Context.MODE_PRIVATE).getInt(SplashActivity.PREFERENCE_HYBRID_UPDATE_TIME, 0);
        if (mUrl.startsWith(Constants.FILE_URL_PREFIX)) mUrl += "?time=" + lastUpdateTime;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @CallSuper
    protected void setUpWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webView.getSettings().setDatabasePath(FileUtil.getDatabasePath(getContext()));
        }

        webView.setWebViewClient(new BaseWebClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getContext().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
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

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (mRefreshable) reload();
        });
    }

    public void setRefreshable(boolean refreshable) {
        mRefreshable = refreshable;
    }

    public void reload() {
        if (webView != null)
            webView.reload();
    }

    private void loadOver() {
        swipeRefreshLayout.setRefreshing(false);
    }

    private void webGetEnv(List<String> args) {
        @SuppressWarnings("ConstantConditions") String env = Constants.SANDBOX ? "development" : "production";
        webView.loadUrl(String.format(toJs(env), HybrdUrlHandler.FUNC_GET_ENV));
    }

    private void webGetAccount(List<String> args) {
        webView.loadUrl(String.format(toJs(AccountManager.getUserInfo(getContext()), true, true),
                HybrdUrlHandler.FUNC_GET_ACCOUNT));
    }

    private void webGetAccountId(List<String> args) {
        webView.loadUrl(String.format(toJs(AccountManager.getUserInfo(getContext())._id),
                HybrdUrlHandler.FUNC_GET_ACCOUNT_ID));
    }

    private void webLog(List<String> args) {
        if (Constants.WEB_LOG_BLOCK) {
            return;
        }
        LogUtil.i(TAG, "js log : " + args.get(0));
    }

    private void webAlert(List<String> args) {
        String type = args.get(0);
        if (dialog != null && dialog.isShowing()) dialog.hide();
        if (TextUtils.equals(type, "alert")) {
            dialog = new MaterialDialog.Builder(getContext()).theme(Theme.LIGHT)
                    .content(args.get(1)).progress(true, 0).build();
            dialog.show();
        } else if (TextUtils.equals(type, "message") && mActivity != null) {
            mActivity.showSnackMsg(args.get(1));
        } else {
            LogUtil.e(TAG, "unhandled alert type");
        }
    }

    private void webCancelAlert(List<String> args) {
        if (dialog != null && dialog.isShowing()) {
            dialog.hide();
        }
    }

    private void webFinish(List<String> args) {
        String type = args.get(0);
        if (TextUtils.equals(type, "preview")) {
            Intent intent = new Intent();
            intent.putExtra(WorldPickActivity.KEY_NAME, args.get(1));
            intent.putExtra(WorldPickActivity.KEY_ID, args.get(2));
            if (mActivity != null)
                mActivity.setResult(WorldPickActivity.RESULT_OK, intent);
        } else {
            LogUtil.e(TAG, "unhandled finish type");
        }
        if (mActivity != null)
            mActivity.finish();
    }

    private void webLoad(List<String> args) {
        webView.loadUrl(String.format(toJs(mArg, false), HybrdUrlHandler.FUNC_LOAD));
    }

    private void webAuth(List<String> args) {
        webView.loadUrl(String.format(toJs(OneMomentClientV4.getAuthStr()),
                HybrdUrlHandler.FUNC_GET_BASIC_AUTH_HEADER));
    }

    private void webGetDiary(List<String> args) {
        String startDate = args.get(0);
        long numRequest = Long.parseLong(args.get(1));
        try {
            Dao<Moment, Integer> momentDao = OpenHelperManager.getHelper(getContext(),
                    MomentDatabaseHelper.class).getDao(Moment.class);
            //TODO cache this value but observe its changes
            long countAll = momentDao.queryBuilder().limit(numRequest).where()
                    .eq("owner", AccountManager.getUserInfo(getContext())._id).countOf();

            // use gt instead of ge according document required, By Carlos
            Where<Moment, Integer> where = momentDao.queryBuilder().limit(numRequest).where()
                    .eq("owner", AccountManager.getUserInfo(getContext())._id);
            // start date can be empty for getting all videos started at any day.
            if (!TextUtils.isEmpty(startDate))
                where.and().gt("time", startDate);
            List<Moment> moments = where.query();

            Gson gson = GsonFactory.newNormalGson();
            JsonArray jsonArray = new JsonArray();
            for (Moment m : moments) {
                JsonObject filename = new JsonObject();
                filename.addProperty("filename", m.getPath());
                jsonArray.add(filename);
            }

            JsonObject jsonObject = new JsonObject();
            // alert: this is the numbers of all the moments in database,
            // not the numbers of the videos
            jsonObject.addProperty("videos_num", countAll);
            jsonObject.add("videos", jsonArray);
            // here set encode false By Carlos
            webView.loadUrl(String.format(toJs(gson.toJson(jsonObject), false),
                    HybrdUrlHandler.FUNC_GET_DIARY));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendFinish() {
        String result = "javascript:ctx.finish()";
        LogUtil.d(TAG, "load js : " + result);
        webView.loadUrl(result);
    }

    public String toJs(Object o) {
        return toJs(o, true);
    }

    public String toJs(Object o, boolean encode, boolean naming) {
        String arg;
        Gson gson;
        if (naming)
            gson = GsonFactory.newNamingGson();
        else
            gson = GsonFactory.newNormalGson();
        arg = gson.toJson(o);
        if (encode) {
            arg = arg.replace("\"", "\\\"");
        } else {
            if (arg.startsWith("\""))
                arg = arg.substring(1);
            if (arg.endsWith("\""))
                arg = arg.substring(0, arg.length() - 1);
        }

        String result = "javascript:ctx.%sAndroidReturn('[" + arg + "]')";
        LogUtil.d(TAG, "load js : " + result);
        return result;
    }

    public String toJs(Object o, boolean encode) {
        return toJs(o, encode, false);
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

    public interface WebViewLoadListener {
        void loadOver();
    }

    private class BaseWebClient extends WebViewClient {

        private HybrdUrlHandler urlHandler = new HybrdUrlHandler() {
            @Override
            protected boolean handleInnerUrl(UrlModel urlModel) {
                if (TextUtils.equals(urlModel.call, FUNC_GET_ENV)) {
                    webGetEnv(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_GET_ACCOUNT)) {
                    webGetAccount(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_GET_ACCOUNT_ID)) {
                    webGetAccountId(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_LOG)) {
                    webLog(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_ALERT)) {
                    webAlert(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_CANCEL_AlERT)) {
                    webCancelAlert(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_FINISH)) {
                    webFinish(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_LOAD)) {
                    webLoad(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_GET_BASIC_AUTH_HEADER)) {
                    webAuth(urlModel.args);
                } else if (TextUtils.equals(urlModel.call, FUNC_GET_DIARY)) {
                    webGetDiary(urlModel.args);
                } else {
                    LogUtil.i(TAG, "unknown call type");
                }
                return true;
            }
        };

        @Override
        public void onPageFinished(WebView view, String url) {
            loadOver();
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            url = HybrdUrlHandler.urlDecode(url);
            LogUtil.d(TAG, url);
            return urlHandler.handleUrl(getContext(), url, (int) (touchX + posX), (int) (touchY + posY))
                    || super.shouldOverrideUrlLoading(view, url);
        }

        @SuppressWarnings("deprecation")
        @Override
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

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
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
