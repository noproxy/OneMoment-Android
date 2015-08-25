package co.yishun.onemoment.app.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
    @ViewById Toolbar toolbar;
    @Extra
    WorldTag tag;
    @ViewById
    SuperRecyclerView recyclerView;
    @ViewById CollapsingToolbarLayout collapsingToolbarLayout;
    @ViewById ImageView videoImageView;
    @ViewById(R.id.ptr_layout) SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }


    @AfterViews
    void setViews() {
        videoImageView.setBackgroundColor(tag.color);

        setupToolbar(this, toolbar);
        Picasso.with(this).load(tag.domain + tag.thumbnail).into(videoImageView);
        collapsingToolbarLayout.setTitle(tag.name);
        collapsingToolbarLayout.setCollapsedTitleTextColor(getColor(R.color.textColorPrimary));
        collapsingToolbarLayout.setExpandedTitleColor(getColor(R.color.textColorPrimaryInverse));

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        TagAdapter adapter = new TagAdapter(this, this);
        recyclerView.setAdapter(adapter);
        TagController_.getInstance_(this).setUp(adapter, recyclerView, tag);
    }

    @CallSuper
    protected ActionBar setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);

        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        Log.i("setupToolbar", "set home as up true");
        return ab;
    }

    @Override
    public void onClick(View view, TagVideo item) {

    }
}
