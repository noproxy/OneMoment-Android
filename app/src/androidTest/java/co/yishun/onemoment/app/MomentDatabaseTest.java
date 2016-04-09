package co.yishun.onemoment.app;

import android.test.AndroidTestCase;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.SQLException;

import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;

/**
 * Created by Carlos on 12/7/15.
 */
@RunWith(JUnit4.class)
public class MomentDatabaseTest extends AndroidTestCase {

    Dao<Moment, Integer> dao;

    @Before public void setDao() throws SQLException {
        dao = OpenHelperManager.getHelper(getContext(), MomentDatabaseHelper.class).getDao(Moment.class);
    }


}
