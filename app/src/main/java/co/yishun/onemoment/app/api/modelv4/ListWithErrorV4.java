package co.yishun.onemoment.app.api.modelv4;

import android.text.TextUtils;

import java.util.List;

import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.config.Constants;

/**
 * Created by Jinge on 2016/1/23.
 */
public class ListWithErrorV4<E> extends ListWithError<E> {
    public String error;
    public ListWithErrorV4(List<E> mList) {
        super(mList);
    }

    @Override public boolean isSuccess() {
        return TextUtils.equals(error, Constants.ErrorStr.SUCCESS);
    }
}
