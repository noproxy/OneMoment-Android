package co.yishun.onemoment.app.ui.controller;

import android.content.Context;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;

import java.util.List;

import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.SearchAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public class MeController extends IntOffsetRefreshableRecyclerController<SuperRecyclerView, WorldTag, SearchAdapter.SimpleViewHolder> {
    public static final int COUNT_EVERY_PAGE = 5;
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private boolean isPublic = true;

    protected MeController(Context context) {
        super(context);
    }

    @Override
    protected List<WorldTag> onLoad() {
        ListWithError<WorldTag> list = mWorld.getJoinedWorldTags(AccountManager.getUserInfo(mContext)._id, isPublic ? "public" : "private", getOffset(), COUNT_EVERY_PAGE);
        if (list.isSuccess()) {
            setOffset(getOffset() + COUNT_EVERY_PAGE);
            return list;
        } else return null;
    }

    public void setUp(AbstractRecyclerViewAdapter<WorldTag, SearchAdapter.SimpleViewHolder> adapter, SuperRecyclerView recyclerView, boolean isPublic) {
        this.isPublic = isPublic;// must first!
        super.setUp(adapter, recyclerView);
    }
}
