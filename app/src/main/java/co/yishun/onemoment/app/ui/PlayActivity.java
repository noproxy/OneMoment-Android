package co.yishun.onemoment.app.ui;

import android.media.Image;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.w3c.dom.Text;

import java.io.IOException;
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
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.common.BaseActivity;

/**
 * Created on 2015/10/26.
 */
@EActivity(R.layout.activity_play)
public class PlayActivity extends BaseActivity {

    @Extra
    TagVideo tagVideo;
    @Extra
    WorldTag worldTag;
    @ViewById
    ImageView avatar;
    @ViewById
    TextView usernameTextView;
    @ViewById
    OnemomentPlayerView videoPlayView;
    @ViewById
    TextView voteCountTextView;
    @ViewById
    Toolbar toolbar;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @AfterViews
    void setup() {
        setupToolbar(this, toolbar);

        Picasso.with(this).load(tagVideo.avatar).into(avatar);

        usernameTextView.setText(tagVideo.nickname);

        VideoResource vr1 = new LocalVideo(new BaseVideoResource(), FileUtil.getWorldVideoStoreFile(this, tagVideo).getPath());
        List<VideoTag> tags = new LinkedList<VideoTag>();
        for(int i = 0; i < tagVideo.tags.size(); i++) {
            tags.add(new BaseVideoTag(tagVideo.tags.get(i).name, tagVideo.tags.get(i).x / 100f, tagVideo.tags.get(i).y / 100f));
        }
        vr1 = new TaggedVideo(vr1, tags);
        videoPlayView.addVideoResource(vr1);

        try {
            videoPlayView.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        voteCountTextView.setText(tagVideo.likeNum + "");
//        voteCountTextView.setTextAppearance(this, R.style.TextAppearance.AppCompat.Small);
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
        ab.setTitle(tagVideo.type);
        Log.i("setupToolbar", "set home as up true");
        return ab;
    }

    @Click(R.id.videoPlayView)
    void videoClick() {
        if(videoPlayView.isPlaying()){
            videoPlayView.pause();
        } else {
            videoPlayView.start();
        }
    }
}
