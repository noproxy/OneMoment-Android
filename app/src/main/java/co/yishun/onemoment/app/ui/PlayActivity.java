package co.yishun.onemoment.app.ui;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import co.yishun.library.OnemomentPlayerView;
import co.yishun.library.resource.BaseVideoResource;
import co.yishun.library.resource.LocalVideo;
import co.yishun.library.resource.TaggedVideo;
import co.yishun.library.resource.VideoResource;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.play.PlayTagVideoFragment;
import co.yishun.onemoment.app.ui.play.PlayTagVideoFragment_;
import co.yishun.onemoment.app.ui.play.PlayWorldFragment;
import co.yishun.onemoment.app.ui.play.PlayWorldFragment_;

/**
 * Created on 2015/10/26.
 */
@EActivity(R.layout.activity_play)
public class PlayActivity extends BaseActivity {
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_WORLD = 2;
    private static final String TAG = "PlayActivity";

    @Extra
    int type;
    @Extra
    TagVideo oneVideo;
    @Extra
    WorldTag worldTag;
    @ViewById
    Toolbar toolbar;
    @ViewById
    ImageView addImageView;

    private FragmentManager fragmentManager;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setupView() {
        fragmentManager = getSupportFragmentManager();
        setupToolbar(this, toolbar);

        switch (type) {
            case TYPE_VIDEO:
                PlayTagVideoFragment playTagVideoFragment = PlayTagVideoFragment_.builder().oneVideo(oneVideo).build();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, playTagVideoFragment).commit();
                break;
            case TYPE_WORLD:
                PlayWorldFragment playWorldFragment = PlayWorldFragment_.builder().worldTag(worldTag).build();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, playWorldFragment).commit();
                break;
        }
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
        ab.setTitle(worldTag.name);
        String num = String.valueOf(worldTag.videosCount);
        SpannableString ss = new SpannableString(num + "人加入");
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, num.length()+ 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        toolbar.setSubtitle(ss);
        Log.i("setupToolbar", "set home as up true");
        return ab;
    }

    @Click(R.id.addImageView)
    void addVideo(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        ShootActivity_.intent(this).transitionX(location[0] + view.getWidth() / 2)
                .transitionY(location[1] + view.getHeight() / 2).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
