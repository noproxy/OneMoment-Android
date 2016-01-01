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
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public class TagController extends RecyclerController<Integer, SuperRecyclerView, TagVideo, TagAdapter.SimpleViewHolder>
        implements OnMoreListener {
    public static final int COUNT_EVERY_PAGE = 10;
    private static final String TAG = "TagController";
    private WorldTag mTag;
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private Seed seed;
    private boolean mIsPrivate;

    protected TagController(Context context) {
        super(context);
    }

    public void setUp(AbstractRecyclerViewAdapter<TagVideo, TagAdapter.SimpleViewHolder> adapter,
                      SuperRecyclerView recyclerView, WorldTag tag, boolean isPrivate) {
        mTag = tag;
        mIsPrivate = isPrivate;
        super.setUp(adapter, recyclerView, 0);
        getRecyclerView().setOnMoreListener(this);
    }

    @Override
    protected ListWithError<TagVideo> onLoad() {
        ListWithError<TagVideo> list;
        if (mIsPrivate)
            list = mWorld.getPrivateVideoOfTag(mTag.name, getOffset(), COUNT_EVERY_PAGE, AccountManager.getUserInfo(mContext)._id);
        else
            list = mWorld.getVideoOfTag(mTag.name, getOffset(), COUNT_EVERY_PAGE, AccountManager.getUserInfo(mContext)._id, seed);

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
