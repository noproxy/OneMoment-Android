package co.yishun.onemoment.app.ui.home;

import android.widget.FrameLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.common.CommonWebFragment;
import co.yishun.onemoment.app.ui.common.CommonWebFragment_;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;

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
        getFragmentManager().beginTransaction().replace(R.id.containerFrameLayout, meWebFragment).commit();
    }


    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_me_title;
    }
    @Override
    public void setPageInfo() {
        mPageName = "MeFragment";
    }

}
