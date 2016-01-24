package co.yishun.onemoment.app.ui;

import android.view.Menu;
import android.view.MenuItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.hybrd.BaseWebActivity;

/**
 * Created by Jinge on 2016/1/22.
 */
@EActivity(R.layout.activity_tool_fragment)
public class PersonalWorldActivity extends BaseWebActivity {

    public static final String KEY_ID = "world_id";
    public static final String KEY_NAME = "world_name";

    @AfterInject void setDefault() {
        title = getString(R.string.activity_personal_world_title);
        File hybrdDir = FileUtil.getInternalFile(this, Constants.HYBRD_UNZIP_DIR);
        url = Constants.FILE_URL_PREFIX + new File(hybrdDir, "build/pages/add_to_world/add_to_world.html").getPath();
    }

    @AfterViews void setupViews() {
        setupToolbar();
        setupFragment();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_personal_world, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.activity_personal_world_add) {
            CreateWorldActivity_.intent(this).start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void setPageInfo() {
        mPageName = "PersonalWorldActivity";
    }
}
