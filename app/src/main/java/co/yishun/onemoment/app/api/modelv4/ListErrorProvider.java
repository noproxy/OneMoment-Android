package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

/**
 * Created by Jinge on 2016/1/28.
 */
public interface ListErrorProvider<E> extends List<E> {
    boolean isSuccess();
}
