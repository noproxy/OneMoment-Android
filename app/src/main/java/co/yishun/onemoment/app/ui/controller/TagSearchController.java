package co.yishun.onemoment.app.ui.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagSearchAdapter;

/**
 * Created by Jinge on 2015/11/2.
 */
@EBean
public class TagSearchController extends RecyclerController<Integer, RecyclerView, String, TagSearchAdapter.TagSearchViewHolder> {
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private String mWords;

    protected TagSearchController(Context context) {
        super(context);
    }

    public void setUp(AbstractRecyclerViewAdapter<String, TagSearchAdapter.TagSearchViewHolder> adapter,
                      RecyclerView recyclerView, String words) {
        mWords = words;
        super.setUp(adapter, recyclerView, 0);
    }

    @Override
    protected ListWithError<String> onLoad() {
        ListWithError<WorldTag> worldTags = mWorld.getSuggestedTagName(mWords);
        List<String> tagNames = new ArrayList<>(worldTags.size());
        for (WorldTag worldTag : worldTags) {
            tagNames.add(worldTag.name);
        }
        ListWithError<String> result = new ListWithError<>(tagNames);
        result.code = worldTags.code;
        result.errorCode = worldTags.errorCode;
        result.msg = worldTags.msg;
        return result;
    }
}
