package co.yishun.onemoment.app;

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.Domain;
import co.yishun.onemoment.app.api.model.UploadToken;
import retrofit.RestAdapter;

/**
 * Created by Carlos on 2015/8/12.
 */
@RunWith(AndroidJUnit4.class)
public class MiscApiTest extends AndroidTestCase {
    public static final String DOMAIN_VIDEO_RESOURCE = "http://yishun.qiniudn.com/";
    private Misc mMisc;

    @Before
    public void createMiscService() {
        RestAdapter adapter = OneMomentV3.createAdapter();
        mMisc = adapter.create(Misc.class);
    }

    @Test
    public void testGetUploadToken() {
        UploadToken token = mMisc.getUploadToken("test");
        assertNotNull(token.token);
    }

    @Test
    public void testGetResourceDomain() {
        Domain domain = mMisc.getResourceDomain(Domain.Type.VIDEO);
        assertEquals(domain.domain, DOMAIN_VIDEO_RESOURCE);
    }
}
