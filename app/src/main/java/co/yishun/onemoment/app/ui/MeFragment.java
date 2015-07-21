package co.yishun.onemoment.app.ui;

import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Created by yyz on 7/21/15.
 */
@EFragment(R.layout.fragment_me)
public class MeFragment extends BaseFragment {
    @Override protected int getTitle() {
        return R.string.me_title;
    }
}
