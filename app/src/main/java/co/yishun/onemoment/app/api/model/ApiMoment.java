package co.yishun.onemoment.app.api.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.sina.weibo.sdk.utils.LogUtil;

import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.model.Moment;

/**
 * This is a model represent CompatMoment from Server API 2.0
 * Created by Carlos on 2015/8/8.
 */
public class ApiMoment extends ApiModel implements Comparable<ApiMoment>, QiniuKeyProvider, Moment.MomentProvider {
    private static final String TAG = "ApiMoment";
    public String mimeType = "video/mp4";
    public String hash;
    @SerializedName("fsize")
    public long fileSize;
    public long putTime;
    private String key;

    //<userID>-<time>-<timestamp>.mp4
    // When be created by Gson into a List, auto set code 1
    public ApiMoment() {
        this.code = 1;
        this.msg = "";
    }

    @Override
    public String getPath() {
        return key;
    }

    @Override
    public String getTime() {
        return this.key.substring(key.indexOf(Constants.URL_HYPHEN) + 1, key.lastIndexOf(Constants.URL_HYPHEN));
    }

    @Override
    public String getOwnerID() {
        String id = this.key.substring(0, key.indexOf(Constants.URL_HYPHEN));
        LogUtil.v(TAG, "getOwnerID: " + id);
        return id;
    }

    @Override
    public String getUnixTimeStamp() {
        return key.substring(key.lastIndexOf(Constants.URL_HYPHEN) + 1, key.lastIndexOf("."));
    }

    @Override
    public String toString() {
        return "Data{" +
                "key='" + key + '\'' +
                ", putTime='" + putTime + '\'' +
                '}';
    }

    @Override
    public int compareTo(final @NonNull ApiMoment apiMoment) {
        return (int) (putTime - apiMoment.putTime);
    }

    @Override public String getKey() {
//        if (BuildConfig.DEBUG && !key.equals(getOwnerID() + Constants.URL_HYPHEN + this.getTime() + Constants.URL_HYPHEN + this.getUnixTimeStamp() + Constants.VIDEO_FILE_SUFFIX))
//            throw new AssertionError("qiniu key is not corroding to server's");
        // because of history, there are some key not accorded with standard.
        return key;
    }
}
