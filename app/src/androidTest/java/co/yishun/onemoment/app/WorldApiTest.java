package co.yishun.onemoment.app;

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.VideoTag;
import co.yishun.onemoment.app.api.model.WorldTag;
import retrofit.RestAdapter;

/**
 * Created by Carlos on 2015/8/9.
 */
@RunWith(AndroidJUnit4.class)
public class WorldApiTest extends AndroidTestCase {
    private World mWorld;

    @Before
    public void createWorldService() {
        RestAdapter adapter = OneMomentV3.createAdapter();
        mWorld = adapter.create(World.class);
    }

    @Test
    public void testGetBanners() {
        List<Banner> banners = mWorld.getBanners(3);
        assertEquals(banners.size(), 3);
    }

    @Test
    public void testGetTagList() {
        List<WorldTag> worldTags = mWorld.getWorldTagList(3, null, null);
        assertNotSame(worldTags.size(), 0);
    }

    @Test
    public void testGetLikeVideo() {
        List<Video> videos = mWorld.getLikedVideos(AccountApiTest.TEST_ACCOUNT_ID, 0, 10);
        assertNotNull(videos);
    }


    @Test
    public void testGetJoinedTags() {
        List<WorldTag> worldTags = mWorld.getJoinedWorldTags(AccountApiTest.TEST_ACCOUNT_ID, "public", 0, 10);
        assertNotNull(worldTags);
    }

    @Test
    public void testGetVideoOfTag() {
        List<TagVideo> videos = mWorld.getVideoOfTag("我的父母", 0, 6, null, null);
        assertNotNull(videos);
    }

    @Test
    public void testGetSuggestedTagName() {
        List<WorldTag> tags = mWorld.getSuggestedTagName("我的");
        assertNotNull(tags);
    }

    @Test
    public void testAddVideoToWorld() {
        List<VideoTag> list = new ArrayList<>();
        VideoTag tag = new VideoTag();
        tag.name = "test";
        tag.type = "words";
        tag.x = 0;
        tag.y = 0;
        list.add(tag);
        Video video = mWorld.addVideoToWorld(AccountApiTest.TEST_ACCOUNT_ID, Video.Type.PUBLIC, "temp.mp4", World.Util.getTagsJson(list));

        assertNotNull(video);
    }
}
