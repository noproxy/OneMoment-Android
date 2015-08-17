package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;

import java.util.List;

import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.VideoLikeAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public class VotedUpController extends RecyclerController<Integer, SuperRecyclerView, Video, VideoLikeAdapter.SimpleViewHolder> implements SwipeRefreshLayout.OnRefreshListener {
    private World mWorld = OneMomentV3.createAdapter().create(World.class);

    protected VotedUpController(Context context) {
        super(context);
    }

    public void setUp(AbstractRecyclerViewAdapter<Video, VideoLikeAdapter.SimpleViewHolder> adapter, SuperRecyclerView recyclerView) {
        recyclerView.setRefreshListener(this);
        super.setUp(adapter, recyclerView, 0);
    }

    @Override
    protected synchronized List<Video> synchronizedLoad() {
        List<Video> list = mWorld.getLikedVideos(AccountHelper.getUserInfo(mContext)._id, getOffset(), 10);
        if (list.size() == 0) {
            //TODO loading error
            return null;
        }
        setOffset(getOffset() + 10);
        return list;
    }

    @Override
    public void onRefresh() {
        getAdapter().clear();
        setOffset(0);
        load();
    }
}