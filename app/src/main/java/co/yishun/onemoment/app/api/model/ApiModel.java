package co.yishun.onemoment.app.api.model;

import java.io.Serializable;

import co.yishun.onemoment.app.config.Constants;

/**
 * Created by Carlos on 2015/8/8.
 */
public class ApiModel implements Serializable {
    public int code;
    public int errorCode = 1;
    public String msg;

    public ApiModel() {

    }

    public ApiModel(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return code == Constants.CODE_SUCCESS;
    }
}
