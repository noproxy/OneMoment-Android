package co.yishun.onemoment.app.ui.play;

import android.content.Context;
import android.view.WindowManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.library.OnemomentPlayerView;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created on 2015/12/20.
 */
@EFragment
public abstract class PlayFragment extends BaseFragment {

    protected Context mContext;
    @ViewById OnemomentPlayerView videoPlayView;

    private boolean isLoading;

    @AfterViews void setupVideoPlayView() {
        videoPlayView.showLoading();
        isLoading = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    protected void onLoad() {
        videoPlayView.hideLoading();
        isLoading = false;
    }

    protected void onLoadError(int resId) {
        videoPlayView.hideLoading();
        if (isLoading) {
            ((BaseActivity) getActivity()).showSnackMsg(resId);
        }
    }

    @Click(R.id.videoPlayView) void videoClick() {
        if (videoPlayView.isPlaying()) {
            videoPlayView.pause();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            videoPlayView.start();
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override public void onPause() {
        super.onPause();
        if (videoPlayView != null) {
            videoPlayView.pause();
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (videoPlayView != null) {
            videoPlayView.stop();
        }
    }

}