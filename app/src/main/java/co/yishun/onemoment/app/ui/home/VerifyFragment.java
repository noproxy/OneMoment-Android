package co.yishun.onemoment.app.ui.home;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.view.CountDownView;

/**
 * Created by yyz on 7/25/15.
 */

@EFragment(R.layout.layout_countdown)
public class VerifyFragment extends Fragment {

    @ViewById CountDownView countDownView;

    @Click void btnClicked(View view) {
        countDownView.setStartNumber(10);
        countDownView.setOnCountDownEndListener(() -> {
            Toast.makeText(view.getContext(), "End!", Toast.LENGTH_LONG).show();
        });
        countDownView.startCountDown();
    }


}
