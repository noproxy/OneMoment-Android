package co.yishun.onemoment.app.api.model;

import android.support.annotation.StringDef;

/**
 * Created by Carlos on 2015/8/17.
 */
public class VideoTag extends ApiModel {
    public String name;
    public @Type String type;
    public float x;
    public float y;

    @StringDef({"location", "words", "time"})
    public @interface Type {
    }
}
