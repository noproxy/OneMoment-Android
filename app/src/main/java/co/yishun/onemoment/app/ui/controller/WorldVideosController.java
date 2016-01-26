package co.yishun.onemoment.app.ui.controller;

import android.content.Context;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.api.APIV4;
import co.yishun.onemoment.app.api.authentication.OneMomentV4;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.modelv4.ListWithErrorV4;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.WorldVideoAdapter;

/**
 * Created by Jinge on 2016/1/26.
 */
@EBean
public class WorldVideosController extends RecyclerController<Integer, SuperRecyclerView, WorldVideo, WorldVideoAdapter.SimpleViewHolder>
        implements OnMoreListener {
    public static final int COUNT_EVERY_PAGE = 10;
    private static final String TAG = "TagController";
    private String mWorldId;
    private APIV4 mApiV4 = OneMomentV4.createAdapter().create(APIV4.class);
    private Seed seed;
    private boolean mIsPrivate;

    protected WorldVideosController(Context context) {
        super(context);
    }

    public void setUp(AbstractRecyclerViewAdapter<WorldVideo, WorldVideoAdapter.SimpleViewHolder> adapter,
                      SuperRecyclerView recyclerView, String worldId) {
        mWorldId = worldId;
        super.setUp(adapter, recyclerView, 0);
        getRecyclerView().setOnMoreListener(this);
    }

    @Override
    protected ListWithErrorV4<WorldVideo> onLoad() {
        ListWithErrorV4<WorldVideo> list;
        list = mApiV4.getTodayVideos(mWorldId, 0, 6);
        setOffset(getOffset() + list.size());
        return list;
    }

    @Override
    @UiThread void onLoadEnd(ListWithError<WorldVideo> list) {
        if (list == null || !list.isSuccess()) {
            onLoadError();
            getRecyclerView().hideMoreProgress();
        } else {
            ((WorldVideoAdapter) getAdapter()).addItems(list, getOffset());
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
