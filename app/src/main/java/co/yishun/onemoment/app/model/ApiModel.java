package co.yishun.onemoment.app.model;

import java.io.Serializable;

/**
 * Created by Carlos on 2015/8/8.
 */
public abstract class ApiModel implements Serializable {
    public int code;
    public int errorCode;
    public String msg;
}
