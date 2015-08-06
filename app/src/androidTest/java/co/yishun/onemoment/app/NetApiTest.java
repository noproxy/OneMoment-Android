package co.yishun.onemoment.app;

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.OneMomentV3;
import co.yishun.onemoment.app.model.User;
import retrofit.RestAdapter;

/**
 * Created by Carlos on 2015/8/5.
 */
@RunWith(AndroidJUnit4.class)
public class NetApiTest extends AndroidTestCase {
    public static final String TEST_ACCOUNT_ID = "54c7530f7d40b52e24107956";

    @Test
    public void testNet() {
        RestAdapter adapter = OneMomentV3.createAdapter();
        Account account = adapter.create(Account.class);
        User user = account.getUser(TEST_ACCOUNT_ID);

    }
}
