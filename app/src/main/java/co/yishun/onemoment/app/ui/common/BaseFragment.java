package co.yishun.onemoment.app.ui.common;

import android.support.v4.app.Fragment;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by yyz on 8/3/15.
 */
public abstract class BaseFragment extends Fragment {
    //set it true and give a page name in setPageInfo(), if we take this fragment into count.
    protected boolean mIsPage = true;
    protected String mPageName = "BaseFragment";

    public abstract void setPageInfo();

    @Override
    public void onResume() {
        super.onResume();
        setPageInfo();
        if (mIsPage) {
            MobclickAgent.onPageStart(mPageName);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsPage) {
            MobclickAgent.onPageEnd(mPageName);
        }
    }
}
