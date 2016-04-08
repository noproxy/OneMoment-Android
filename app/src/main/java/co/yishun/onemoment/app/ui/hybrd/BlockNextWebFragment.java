package co.yishun.onemoment.app.ui.hybrd;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import co.yishun.onemoment.app.R;

/**
 * Deprecated because of no use
 */
@Deprecated
@EFragment(R.layout.fragment_web_view_block_next)
public class BlockNextWebFragment extends BaseWebFragment {

    @AfterViews
    void setUpViews() {
        setUpWebView();
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }
}
