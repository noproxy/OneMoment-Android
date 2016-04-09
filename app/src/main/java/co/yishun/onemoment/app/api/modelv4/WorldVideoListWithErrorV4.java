package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

/**
 * Created by Jinge on 2016/1/27.
 */
public class WorldVideoListWithErrorV4<E> extends ListWithErrorV4<E> {
    public World world;

    public WorldVideoListWithErrorV4(List<E> mList) {
        super(mList);
    }
}
