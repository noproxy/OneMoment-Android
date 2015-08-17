package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.TagController_;

/**
 * Created by Carlos on 2015/8/17.
 */

@EActivity(R.layout.activity_tag)
public class TagActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<TagVideo> {
    @Extra
    WorldTag tag;
    @ViewById
    SuperRecyclerView recyclerView;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setViews() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        TagAdapter adapter = new TagAdapter(this, this);
        recyclerView.setAdapter(adapter);
        TagController_.getInstance_(this).setUp(adapter, recyclerView, tag);
    }

    @Override
    public void onClick(View view, TagVideo item) {

    }
}
