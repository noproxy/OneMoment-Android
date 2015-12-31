package co.yishun.onemoment.app.ui.controller;

import android.content.Context;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.EBean;

import java.util.List;

import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.SearchAdapter;

/**
 * Created on 2015/10/19.
 */
@EBean
public class SearchController extends RecyclerController<Integer, SuperRecyclerView, WorldTag, SearchAdapter.SimpleViewHolder> {
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private String mWords;

    protected SearchController(Context context) {
        super(context);
    }

    public void setUp(AbstractRecyclerViewAdapter<WorldTag, SearchAdapter.SimpleViewHolder> adapter,
                      SuperRecyclerView recyclerView, String words) {
        mWords = words;
        super.setUp(adapter, recyclerView, 0);
    }

    @Override
    protected List<WorldTag> onLoad() {
        ListWithError<WorldTag> worldTags = mWorld.getSuggestedTagName(mWords);
        if (worldTags.isSuccess()) {
            return worldTags;
        } else return null;
    }

    @Override protected void onLoadError() {
        //TODO　load error
    }
}
