package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.squareup.picasso.Picasso;

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
    @ViewById CollapsingToolbarLayout collapsingToolbarLayout;
    @ViewById VideoView videoView;
    @ViewById ImageView videoImageView;
    @ViewById(R.id.ptr_layout) SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setViews() {
        Picasso.with(this).load(tag.domain + tag.thumbnail).into(videoImageView);
        collapsingToolbarLayout.setTitle(tag.name);
        collapsingToolbarLayout.setCollapsedTitleTextColor(getColor(R.color.textColorPrimary));
        collapsingToolbarLayout.setExpandedTitleColor(getColor(R.color.textColorPrimaryInverse));

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
