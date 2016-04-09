package co.yishun.library.momentcalendar;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.Calendar;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
        assertEquals(Calendar.getInstance().get(Calendar.DAY_OF_MONTH), 27);
    }
}