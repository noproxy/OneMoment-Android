package co.yishun.onemoment.app.api.model;

import android.support.annotation.StringDef;

/**
 * Created by Carlos on 2015/8/17.
 */
public class VideoTag extends ApiModel implements co.yishun.library.tag.VideoTag {
    public String name;
    @Deprecated// remove in new version
    public
    @Type
    String type;
    public float x;
    public float y;

    @Override
    public String getText() {
        return name;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @StringDef({"location", "words", "time"})
    public @interface Type {
    }
}
