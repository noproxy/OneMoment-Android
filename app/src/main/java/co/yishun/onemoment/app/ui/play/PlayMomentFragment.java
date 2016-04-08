package co.yishun.onemoment.app.ui.play;


import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OrmLiteDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.yishun.library.VideoPlayerView;
import co.yishun.library.resource.NetworkVideo;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import co.yishun.onemoment.app.data.realm.RealmHelper;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * A simple {@link BaseFragment} subclass.
 */
@EFragment(R.layout.fragment_play_moment)
public class PlayMomentFragment extends PlayFragment
        implements VideoPlayerView.VideoPlayViewListener {

    private static final String TAG = "PlayMomentFragment";
    @FragmentArg
    String startDate;
    @FragmentArg
    String endDate;

    @OrmLiteDao(helper = MomentDatabaseHelper.class)
    Dao<Moment, Integer> momentDao;
    private List<Moment> momentList;

    @AfterViews
    void setUpViews() {
        videoPlayView.setWithAvatar(false);
        videoPlayView.setVideoPlayListener(this);
        try {
            momentList = momentDao.queryBuilder().where()
                    .eq("owner", AccountManager.getAccountId(getContext())).and()
                    .between("time", startDate, endDate).query();

            //noinspection unchecked
            Collections.sort(momentList);
            for (int i = 0; i < momentList.size(); ) {
                if (momentList.get(i).getFile().length() == 0) {
                    momentList.remove(i);
                } else {
                    i++;
                }
            }
            if (momentList.size() > 0) {
                cacheVideoToPlayView(momentList.get(0));
                onLoad();
            }

            onLoadError(R.string.fragment_play_moment_not_found);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    void cacheVideoToPlayView(Moment moment) {
        List<OMLocalVideoTag> omLocalVideoTags = RealmHelper.getTags(moment.getTime());
        List<VideoTag> videoTags = new ArrayList<>();
        for (OMLocalVideoTag omlTag : omLocalVideoTags) {
            String[] position = omlTag.getTagPosition().split(" ");
            videoTags.add(new BaseVideoTag(omlTag.getTagText(), Float.parseFloat(position[0]) * 100f,
                    Float.parseFloat(position[1]) * 100f));
        }
        NetworkVideo videoResource = new NetworkVideo(videoTags, moment.getPath());
        videoPlayView.addVideoResource(videoResource);
    }

    public void pause() {
        videoPlayView.pause();
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }

    @Override
    public void videoChangeTo(int index) {

    }

    @Override
    public boolean loadMore(int index) {
        if (index >= momentList.size()) {
            return false;
        } else {
            cacheVideoToPlayView(momentList.get(index));
            return true;
        }
    }
}
