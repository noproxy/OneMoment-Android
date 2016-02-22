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
import co.yishun.onemoment.app.ui.CreateWorldActivity_;
import co.yishun.onemoment.app.ui.common.ToolbarFragment;
import co.yishun.onemoment.app.ui.hybrd.BaseWebFragment;
import co.yishun.onemoment.app.ui.hybrd.CommonWebFragment;
import co.yishun.onemoment.app.ui.hybrd.CommonWebFragment_;

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
        getFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, worldWebFragment, BaseWebFragment.TAG_WEB).commit();

        worldWebFragment.setRefreshable(true);
    }

    @Override protected int getTitleDrawableRes() {
        return R.drawable.pic_world_title;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_world, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.fragment_world_action_add) {
            CreateWorldActivity_.intent(getContext()).start();//TODO may ask for result, and remove refresh every time MainActivity onResume
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setPageInfo() {
        mPageName = "WorldFragment";
    }
}