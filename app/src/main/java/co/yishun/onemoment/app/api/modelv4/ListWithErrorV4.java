package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

import co.yishun.onemoment.app.api.model.ListWithError;

/**
 * Created by Jinge on 2016/1/23.
 */
public class ListWithErrorV4<E> extends ListWithError<E> {
    public String error;
    public ListWithErrorV4(List<E> mList) {
        super(mList);
    }
}
