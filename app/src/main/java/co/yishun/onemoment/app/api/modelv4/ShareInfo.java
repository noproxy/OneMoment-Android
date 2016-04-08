package co.yishun.onemoment.app.api.modelv4;

/**
 * Created by Jinge on 2015/12/11.
 */
public class ShareInfo extends ApiModel implements ShareInfoProvider {
    public String imageUrl;
    public String link;
    public String title;

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
