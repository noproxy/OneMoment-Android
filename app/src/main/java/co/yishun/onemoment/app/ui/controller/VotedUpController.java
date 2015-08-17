package co.yishun.onemoment.app.ui.controller;

import android.content.Context;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;

import java.util.List;

import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.ui.adapter.VideoLikeAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public class VotedUpController extends IntOffsetRefreshableRecyclerController<SuperRecyclerView, Video, VideoLikeAdapter.SimpleViewHolder> {
    private World mWorld = OneMomentV3.createAdapter().create(World.class);

    protected VotedUpController(Context context) {
        super(context);
    }

    @Override
    protected List<Video> onLoad() {
        List<Video> list = mWorld.getLikedVideos(AccountHelper.getUserInfo(mContext)._id, getOffset(), 10);
        if (list.size() == 0) {
            //TODO loading error
            return null;
        }
        setOffset(getOffset() + 10);
        return list;
    }
}