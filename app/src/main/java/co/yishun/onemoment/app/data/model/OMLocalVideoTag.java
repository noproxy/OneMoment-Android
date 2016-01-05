package co.yishun.onemoment.app.data.model;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created on 2015/10/27.
 */
public class OMLocalVideoTag extends RealmObject {

    @Required private String tagDate;
    @Required private String tagText;
    @Required private String tagPosition;

    public String getTagDate() {
        return tagDate;
    }

    public void setTagDate(String tagDate) {
        this.tagDate = tagDate;
    }

    public String getTagText() {
        return tagText;
    }

    public void setTagText(String tagText) {
        this.tagText = tagText;
    }

    public String getTagPosition() {
        return tagPosition;
    }

    public void setTagPosition(String tagPosition) {
        this.tagPosition = tagPosition;
    }

}
