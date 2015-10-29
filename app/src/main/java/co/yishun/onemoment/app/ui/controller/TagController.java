package co.yishun.onemoment.app.ui.controller;

import android.content.Context;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;

import java.util.List;

import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagAdapter;

/**
 * Created by Carlos on 2015/8/17.
 */
@EBean
public class TagController extends RecyclerController<Integer, SuperRecyclerView, TagVideo, TagAdapter.SimpleViewHolder> {
    public static final int COUNT_EVERY_PAGE = 10;
    private WorldTag mTag;
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private Seed seed;

    protected TagController(Context context) {
        super(context);
    }

    public void setUp(AbstractRecyclerViewAdapter<TagVideo, TagAdapter.SimpleViewHolder> adapter, SuperRecyclerView recyclerView, WorldTag tag) {
        mTag = tag;
        super.setUp(adapter, recyclerView, 0);
    }

    @Override
    protected List<TagVideo> onLoad() {
        List<TagVideo> list = mWorld.getVideoOfTag(mTag.name, getOffset(), COUNT_EVERY_PAGE, AccountHelper.getUserInfo(mContext)._id, seed);
        if (list.size() == 0) {
            //TODO loading error
            return null;
        }
        setOffset(getOffset() + COUNT_EVERY_PAGE);
        return list;
    }
}
