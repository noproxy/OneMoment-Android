package co.yishun.onemoment.app;

import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Link;
import co.yishun.onemoment.app.api.model.Moment;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.config.Constants;
import retrofit.RestAdapter;

/**
 * Created by Carlos on 2015/8/5.
 */
@RunWith(AndroidJUnit4.class)
public class AccountApiTest extends AndroidTestCase {
    public static final String TEST_ACCOUNT_ID = "54c7530f7d40b52e24107956";
    public static final String TEST_NOT_EXIST_NICKNAME = "MUSVBIYLTC3HW79J0O4Q5KNDRF2EZ8APG6X1";
    public static final String TEST_EXIST_NICKNAME = "一瞬App";
    public static final String TEST_PHONE = "18571471735";

    private Account mAccount;


    @Before
    public void createAccountService() {
        RestAdapter adapter = OneMomentV3.createAdapter();
        mAccount = adapter.create(Account.class);
    }

    @Test
    public void testGetUserInfo() {
        User user = mAccount.getUserInfo(TEST_ACCOUNT_ID);
        assertNotNull(user);
        assertEquals(user.signUpIP, "223.167.118.79");
        assertEquals(user.signUpTime, "1422349071");
        assertEquals(user.signUpUserAgent, "onemoment/1.3.1 (iPhone; iOS 8.1.2; Scale/2.00)");
    }

    @Test
    public void testNicknameExist() {
        assertEquals(mAccount.isNicknameExist(TEST_NOT_EXIST_NICKNAME).code, Constants.CODE_SUCCESS);
        assertNotSame(mAccount.isNicknameExist(TEST_EXIST_NICKNAME).code, Constants.CODE_SUCCESS);
    }

    @Test
    public void testUpdateInfo() {
        mAccount.updateInfo(TEST_ACCOUNT_ID, null, Account.Gender.MALE, null, null);
        assertEquals(mAccount.getUserInfo(TEST_ACCOUNT_ID).gender, Account.Gender.MALE);
        mAccount.updateInfo(TEST_ACCOUNT_ID, null, Account.Gender.FEMALE, null, null);
        assertEquals(mAccount.getUserInfo(TEST_ACCOUNT_ID).gender, Account.Gender.FEMALE);
    }

    @Test
    public void testGetTagUrl() {
        Link link = mAccount.getTagUrl(TEST_ACCOUNT_ID);
        assertNull(link.link);
    }


    @Test
    public void testSendVerifySms() {
        ApiModel model = mAccount.sendVerifySms(TEST_PHONE, null);
//        exist
        assertEquals(model.code, Constants.CODE_PARAMETER_INVALID);
    }

    @Test
    public void testSignUpByPhone() {

    }

    @Test
    public void testResetPassword() {
        ApiModel model = mAccount.resetPassword(TEST_PHONE, "qaz123");
        assertNotSame(model, Constants.CODE_SUCCESS);
    }

    @Test
    public void testGetVideoList() {
        List<Moment> moments = mAccount.getVideoList(TEST_ACCOUNT_ID);
        assertNotNull(moments);
        assertEquals(moments.size(), 73);
    }
}
