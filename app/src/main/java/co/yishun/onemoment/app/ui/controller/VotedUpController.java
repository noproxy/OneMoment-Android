package co.yishun.onemoment.app.ui.controller;

import android.content.Context;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public class VotedUpController extends IntOffsetRefreshableRecyclerController<SuperRecyclerView, TagVideo, TagAdapter.SimpleViewHolder>
        implements OnMoreListener {
    private World mWorld = OneMomentV3.createAdapter().create(World.class);

    protected VotedUpController(Context context) {
        super(context);
    }

    public void setUp(AbstractRecyclerViewAdapter<TagVideo, TagAdapter.SimpleViewHolder> adapter, SuperRecyclerView recyclerView) {
        super.setUp(adapter, recyclerView, 0);
        getRecyclerView().setOnMoreListener(this);
    }

    @Override
    protected List<TagVideo> onLoad() {
        List<TagVideo> list = mWorld.getLikedVideos(AccountHelper.getUserInfo(mContext)._id, getOffset(), 10);
        if (list.size() == 0) {
            //TODO loading error
            return null;
        }
        setOffset(getOffset() + list.size());
        return list;
    }

    @Override
    @UiThread void onLoadEnd(List<TagVideo> list) {
        if (list != null) {
            ((TagAdapter) getAdapter()).addItems(list, getOffset());
        } else {
            getRecyclerView().hideMoreProgress();
        }
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (overallItemsCount == itemsBeforeMore) {
            getRecyclerView().hideMoreProgress();
        }
        load();
    }
}