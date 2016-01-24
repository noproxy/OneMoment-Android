package co.yishun.onemoment.app.ui.home;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.UserInfoActivity_;
import co.yishun.onemoment.app.ui.hybrd.BaseWebFragment;
import co.yishun.onemoment.app.ui.hybrd.CommonWebFragment;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;
import co.yishun.onemoment.app.ui.hybrd.CommonWebFragment_;

/**
 * Created by yyz on 7/21/15.
 */
@EFragment(R.layout.fragment_me)
public class MeFragment extends ToolbarFragment {
    CommonWebFragment meWebFragment;
    @ViewById FrameLayout containerFrameLayout;

    @AfterViews void setUpViews() {
        File hybrdDir = FileUtil.getInternalFile(getActivity(), Constants.HYBRD_UNZIP_DIR);
        String url = Constants.FILE_URL_PREFIX + new File(hybrdDir, "build/pages/mine/mine.html").getPath();
        meWebFragment = CommonWebFragment_.builder().mUrl(url).build();
        getFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, meWebFragment, BaseWebFragment.TAG_WEB).commit();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_me, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.fragment_me_action_modify_info) {
            UserInfoActivity_.intent(getContext()).start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_me_title;
    }
    @Override
    public void setPageInfo() {
        mPageName = "MeFragment";
    }

}
