package co.yishun.onemoment.app.api.modelv4;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by Jinge on 2016/1/21.
 */
public class ApiModel implements Serializable {
    public String error;
    public String msg;

    public boolean isSuccess() {
        return TextUtils.equals(error, "Ok");
    }

    @Override
    public String toString() {
        return "ApiModel{" +
                "error='" + error + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
