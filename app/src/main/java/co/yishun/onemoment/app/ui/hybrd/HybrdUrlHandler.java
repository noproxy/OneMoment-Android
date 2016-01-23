package co.yishun.onemoment.app.ui.hybrd;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.ui.CreateWorldActivity_;
import co.yishun.onemoment.app.ui.SettingsActivity_;
import co.yishun.onemoment.app.ui.ShootActivity_;
import co.yishun.onemoment.app.ui.UserInfoActivity_;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created by Jinge on 2016/1/23.
 */
public abstract class HybrdUrlHandler {

    public static final String URL_PREFIX = Constants.APP_URL_PREFIX;
    public static final String FUNC_GET_ACCOUNT_ID = "getAccountId";
    public static final String FUNC_GET_ACCOUNT = "getAccount";
    public static final String FUNC_JUMP = "jump";
    public static final String FUNC_LOG = "log";
    public static final String FUNC_ALERT = "alert";
    public static final String FUNC_CANCEL_AlERT = "cancelAlert";
    public static final String FUNC_FINISH = "finish";
    public static final String FUNC_GET_ENV = "getEnv";

    private static final String TAG = "HybrdUrlHandler";

    protected abstract boolean handleInnerUrl(UrlModel urlModel);

    public boolean handleUrl(BaseActivity activity, String url) {
        return handleUrl(activity, url, 0, 0);
    }

    public boolean handleUrl(BaseActivity activity, String url, int posX, int posY) {
        if (url.startsWith(URL_PREFIX)) {
            String json = url.substring(URL_PREFIX.length());
            UrlModel urlModel = new Gson().fromJson(json, UrlModel.class);
            urlModel.args = decode(urlModel.args);
            if (TextUtils.equals(urlModel.call, FUNC_JUMP)) {
                return webJumpWithPosition(activity, urlModel.args, posX, posY);
            }
            return handleInnerUrl(urlModel);
        }
        return false;
    }

    private boolean webJumpWithPosition(BaseActivity activity, List<String> args, int posX, int posY) {
        String des = args.get(0);
        if (TextUtils.equals(des, "web")) {
            CommonWebActivity_.intent(activity).title(args.get(1)).url(args.get(2)).start();
        } else if (TextUtils.equals(des, "camera")) {
            ShootActivity_.intent(activity).transitionX(posX).transitionY(posY).start();
        } else if (TextUtils.equals(des, "setting")) {
            SettingsActivity_.intent(activity).start();
        } else if (TextUtils.equals(des, "create_world")) {
            CreateWorldActivity_.intent(activity).start();
        } else if (TextUtils.equals(des, "edit")) {
            UserInfoActivity_.intent(activity).start();
        } else if (TextUtils.equals(des, "world_square")) {
            //TODO jump to world detail activity
//            TagActivity_.intent(activity).start();
        } else {
            LogUtil.e(TAG, "unhandled jump type");
        }
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

    public static class JumpUrlHandler extends HybrdUrlHandler {

        @Override protected boolean handleInnerUrl(UrlModel urlModel) {
            LogUtil.i(TAG, "unknown call type");
            return false;
        }
    }

    class UrlModel {
        String call;
        List<String> args;
    }

}
