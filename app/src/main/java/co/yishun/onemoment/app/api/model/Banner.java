package co.yishun.onemoment.app.api.model;

/**
 * Created by Carlos on 2015/8/8.
 */
public class Banner extends ApiModel {
    public String iamgeUrl;
    public String href;
    public String createTime;
    public String title;

    // When be created by Gson into a List, auto set code 1
    public Banner() {
        code = 1;
    }
}
