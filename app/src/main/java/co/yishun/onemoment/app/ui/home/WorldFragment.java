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
 * Created by yyz on 7/13/15.
 */
@EFragment(R.layout.fragment_world)
public class WorldFragment extends ToolbarFragment {
    CommonWebFragment worldWebFragment;
    @ViewById FrameLayout containerFrameLayout;

    @AfterViews void setUpViews() {
        File hybrdFile = FileUtil.getInternalFile(getActivity(), Constants.HYBRD_UNZIP_DIR);
        String url = Constants.FILE_URL_PREFIX + new File(hybrdFile, "build/pages/world/world.html").getPath();
        worldWebFragment = CommonWebFragment_.builder().mUrl(url).build();
        getFragmentManager().beginTransaction().replace(R.id.containerFrameLayout, worldWebFragment).commit();
    }

    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_world_title;
    }

    @Override
    public void setPageInfo() {
        mPageName = "WorldFragment";
    }
}