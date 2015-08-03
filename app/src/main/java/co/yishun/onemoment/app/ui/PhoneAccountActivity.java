package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.EActivity;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.account.PhoneLoginFragment;

/**
 * Created by yyz on 8/1/15.
 */

@EActivity(R.layout.activity_phone)
public class PhoneAccountActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().replace(R.id.fragment_container, new PhoneLoginFragment()).commit();
    }
}
