package co.yishun.onemoment.app.api.model;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Carlos on 2015/8/17.
 */
public class TagVideo extends ApiModel implements NameProvider {
    public String _id;
    public boolean available;
    public boolean liked;
    public String account_id;
    public List<VideoTag> tags;
    public long timestamp;
    @SerializedName("filename")
    public String fileName;
    public boolean visible;
    public long createTime;
    public int likeNum;
    public String avatar;
    public @Type String type;
    public String worldId;
    public String nickname;

    public Domain domain;
    public Seed seed;

    @Override
    public String getName() {
        return fileName;
    }

    @StringDef({"public", "private"})
    public @interface Type {

    }
}
