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
public class EspressoTest extends ActivityInstrumentationTestCase2<EspressoTestActivity> {
    public static final String TEXT_CHANGE_TO = "Text changed!";
    private EspressoTestActivity mActivity;

    public EspressoTest() {
        super(EspressoTestActivity.class);
    }

    @Override @Before public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    @Test
    public void testChangeText() {
        onView(withId(R.id.editText)).perform(typeText(TEXT_CHANGE_TO), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.changeTextBtn)).perform(click());
        onView(withId(R.id.showTextView)).check(matches(withText(TEXT_CHANGE_TO)));
//        onView(allOf(withId(R.id.nameEditText), not(instanceOf(EditText.class)))).perform(click());

    }
}
