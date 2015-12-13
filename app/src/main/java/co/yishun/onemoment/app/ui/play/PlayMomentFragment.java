package co.yishun.onemoment.app.ui.play;


import android.view.WindowManager;

import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import co.yishun.library.OnemomentPlayerView;
import co.yishun.library.resource.NetworkVideo;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * A simple {@link BaseFragment} subclass.
 */
@EFragment(R.layout.fragment_play_moment)
public class PlayMomentFragment extends BaseFragment
        implements OnemomentPlayerView.OnVideoChangeListener {

    @FragmentArg String startDate;
    @FragmentArg String endDate;

    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;

    @ViewById OnemomentPlayerView videoPlayView;

    @AfterViews void setUpViews() {
        videoPlayView.setWithAvatar(false);
        videoPlayView.setVideoChangeListener(this);
        try {
            List<Moment> momentList = momentDao.queryBuilder().where().between("time", startDate, endDate).query();
            videoPlayView.setPreview(new File(momentList.get(0).getLargeThumbPath()));
            for (Moment moment : momentList) {
                List<OMLocalVideoTag> omLocalVideoTags = RealmHelper.getTags(moment.getTime());
                List<VideoTag> videoTags = new ArrayList<>();
                for (OMLocalVideoTag omlTag : omLocalVideoTags) {
                    String[] position = omlTag.getTagPosition().split(" ");
                    videoTags.add(new BaseVideoTag(omlTag.getTagText(), Float.parseFloat(position[0]),
                            Float.parseFloat(position[1])));
                }
                NetworkVideo videoResource = new NetworkVideo(videoTags, moment.getPath());
                videoPlayView.addVideoResource(videoResource);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    @Override public void setPageInfo() {
        mIsPage = false;
    }

    @Override public void videoChangeTo(int index) {

    }
}