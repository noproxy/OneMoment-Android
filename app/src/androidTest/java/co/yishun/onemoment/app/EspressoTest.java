package co.yishun.onemoment.app;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.yishun.onemoment.app.ui.EspressoTestActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by yyz on 7/12/15.
 */
@RunWith(AndroidJUnit4.class)
public class EspressoTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mActivity;

//    public EspressoTest(String pkg, Class<MainActivity> activityClass, MainActivity mActivity) {
//        super(pkg, activityClass);
//        this.mActivity = mActivity;
//    }

    //    public EspressoTest(Class<MainActivity> activityClass) {
//        super(activityClass);
//    }
//
    public EspressoTest() {
        super("co.yishun.onemoment.app", MainActivity.class);

    }

    @Override @Before public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    @Test
    public void testChangeText() {
        onView(withId(R.id.nameEditText)).perform(typeText("Test"), ViewActions
                .closeSoftKeyboard
                        ());
        onView(withId(R.id.changeTextBtn)).perform(click());
        onView(withId(R.id.nameEditText)).check(matches(withText(EspressoTestActivity.TEXT_CHANGE_TO)));
//        onView(allOf(withId(R.id.nameEditText), not(instanceOf(EditText.class)))).perform(click());

    }
}
