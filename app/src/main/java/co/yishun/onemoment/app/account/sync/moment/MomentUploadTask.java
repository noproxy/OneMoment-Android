package co.yishun.onemoment.app.account.sync.moment;

import android.support.annotation.NonNull;
import android.util.Log;

import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.function.Consumer;

/**
 * Task to upload a moment to server.
 * Created by Carlos on 2015/12/20.
 */
public class MomentUploadTask implements Runnable {
    private final static Misc mMiscService = OneMomentV3.createAdapter().create(Misc.class);
    private static final String TAG = "MomentUploadTask";
    private final static UploadManager mUploadManager = new UploadManager();
    private final Moment mMoment;
    private final Consumer<Moment> mOnFail;
    private final Consumer<Moment> mOnSuccess;

    public MomentUploadTask(@NonNull Moment moment, @NonNull Consumer<Moment> onFail, @NonNull Consumer<Moment> onSuccess) {
        mMoment = moment;
        this.mOnFail = onFail;
        this.mOnSuccess = onSuccess;
    }


    @Override
    public void run() {
        LogUtil.i(TAG, "upload a moment: " + mMoment);
        String qiNiuKey = mMoment.getKey();
        UploadToken token = mMiscService.getUploadToken(mMoment.getKey());
        if (!token.isSuccess()) {
            LogUtil.e(TAG, "upload failed when get token");
            mOnFail.accept(mMoment);
        } else {
            mUploadManager.put(mMoment.getPath(), qiNiuKey, token.token, newHandler(), newOptions());
        }
    }

    private UploadOptions newOptions() {
        return new UploadOptions(null, Constants.MIME_TYPE, true, null, null);
    }

    private UpCompletionHandler newHandler() {
        return (key, responseInfo, response) -> {
            LogUtil.i(TAG, responseInfo.toString());
            (responseInfo.isOK() ? mOnSuccess : mOnFail).call();
        };
    }

}
