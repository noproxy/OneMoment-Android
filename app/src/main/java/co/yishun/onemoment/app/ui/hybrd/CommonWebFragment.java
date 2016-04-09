package co.yishun.onemoment.app.ui.hybrd;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Created by Jinge on 2016/1/21.
 */
@EFragment(R.layout.fragment_web_view)
public class CommonWebFragment extends BaseWebFragment {

    @AfterViews
    void setUpViews() {
        setUpWebView();
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }
}
