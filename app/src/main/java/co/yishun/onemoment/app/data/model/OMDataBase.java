package co.yishun.onemoment.app.data.model;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created on 2015/10/27.
 */
public class OMDataBase extends RealmObject {
    @Required private String createTime;
    @Required private String updateTime;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
