package co.yishun.onemoment.app.api.model;

import java.io.Serializable;

/**
 * Interface to provide standard api naming. It must obey those naming rules: <ul> <li>
 * <strong>short video: </strong> {userID}-{time}-{timestamp}.mp4 </li> <li> <strong>long video:
 * </strong> long-{userID}-{video_num}-{timestamp}.mp4 <li/> </ul>
 *
 *
 *
 * Created by Carlos on 2015/8/24.
 */
public interface QiniuKeyProvider extends Serializable {
    String getKey();
}
