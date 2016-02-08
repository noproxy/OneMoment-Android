package co.yishun.onemoment.app.api.model;

import java.io.Serializable;

import co.yishun.library.datacenter.Updatable;
import co.yishun.onemoment.app.config.Constants;

/**
 * Created by Carlos on 2015/8/8.
 */
public class ApiModel implements Serializable, Updatable {
    public int code;
    public int errorCode = 1;
    public String msg;
    public CacheType cacheType = CacheType.NETWORK_ONLY;

    public ApiModel() {

    }

    public ApiModel(int code, int errorCode, String msg) {
        this.code = code;
        this.errorCode = errorCode;
        this.msg = msg;
    }

    @Deprecated
    public ApiModel(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public boolean updateThan(Updatable another) {
        return another instanceof ApiModel && this.cacheType.ordinal() > ((ApiModel) another).cacheType.ordinal();
    }

    @Override
    public int compareTo(Object another) {
        throw new UnsupportedOperationException("not implement comparable, please override it");
    }

    public boolean isSuccess() {
        return code == Constants.CODE_SUCCESS;
    }

    public enum CacheType {
        CACHE_ONLY, NORMAL, NETWORK_ONLY
    }
}
