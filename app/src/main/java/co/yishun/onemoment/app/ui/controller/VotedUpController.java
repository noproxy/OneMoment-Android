package co.yishun.onemoment.app.ui.controller;

import android.content.Context;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ListWithError;
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
    protected ListWithError<TagVideo> onLoad() {
        ListWithError<TagVideo> list = mWorld.getLikedVideos(AccountManager.getUserInfo(mContext)._id, getOffset(), 10);
        setOffset(getOffset() + list.size());
        return list;
    }

    @Override
    @UiThread void onLoadEnd(ListWithError<TagVideo> list) {
        if (list == null || !list.isSuccess()) {
            onLoadError();
            getRecyclerView().hideMoreProgress();
        } else {
            ((TagAdapter) getAdapter()).addItems(list, getOffset());
        }
        getRecyclerView().getSwipeToRefresh().setRefreshing(false);
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        if (overallItemsCount == itemsBeforeMore) {
            getRecyclerView().hideMoreProgress();
        }
        load();
    }
}