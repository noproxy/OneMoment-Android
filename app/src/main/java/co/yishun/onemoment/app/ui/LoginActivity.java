package co.yishun.onemoment.app.ui;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/24/15.
 */

@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity {
    public static final int REQUEST_WEIBO_LOGIN = 0;

//    @ViewById
//    Toolbar toolbar;
//
//    @AfterViews void setToolbar() {
//        setSupportActionBar(toolbar);
//        final ActionBar ab = getSupportActionBar();
//        assert ab != null;
//
//        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setDisplayShowTitleEnabled(false);
//        Log.i("setupToolbar", "set home as up true");
//    }


    @Click void loginByPhoneClicked(final View view) {
        PhoneAccountActivity_.intent(this).start();
    }


}
