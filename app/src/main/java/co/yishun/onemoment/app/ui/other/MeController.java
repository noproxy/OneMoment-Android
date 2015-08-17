package co.yishun.onemoment.app.ui.other;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;

import java.util.List;

import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.WorldAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public class MeController extends RecyclerController<Integer, SuperRecyclerView, WorldTag, WorldAdapter.SimpleViewHolder> implements SwipeRefreshLayout.OnRefreshListener {
    public static final int COUNT_EVERY_PAGE = 5;
    private World mWorld = OneMomentV3.createAdapter().create(World.class);

    protected MeController(Context context) {
        super(context);
    }

    @Override
    protected List<WorldTag> synchronizedLoad() {
        List<WorldTag> list = mWorld.getJoinedWorldTags(AccountHelper.getUserInfo(mContext)._id, World.TYPE_PUBLIC, getOffset(), COUNT_EVERY_PAGE);
        if (list.size() == 0) {
            //TODO loading error
            return null;
        }
        setOffset(getOffset() + COUNT_EVERY_PAGE);
        return list;
    }


    public void setUp(AbstractRecyclerViewAdapter<WorldTag, WorldAdapter.SimpleViewHolder> adapter, SuperRecyclerView recyclerView) {
        super.setUp(adapter, recyclerView, 0);
    }

    @Override
    public void onRefresh() {
        getAdapter().clear();
        setOffset(0);
        load();
    }
}
