package es.usc.citius.servando.calendula.activities;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.fragments.RoutineCreateOrEditFragment;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


public class RoutinesActivityEditTest extends ActivityInstrumentationTestCase2<RoutinesActivity> {

    public static final String NAME_BEFORE_EDIT = "Breakfast";
    public static final String NAME_AFTER_EDIT = "Lunch";

    private RoutinesActivity mActivity;

    public RoutinesActivityEditTest() {
        super(RoutinesActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        DB.init(getInstrumentation().getContext());
        DB.dropAndCreateDatabase();

        // create medicine
        Routine created = new Routine(new LocalTime(10, 15), NAME_BEFORE_EDIT);
        created.save();

        // set edit intent
        Intent i = new Intent();
        i.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, created.getId());
        setActivityIntent(i);

        mActivity = getActivity();
    }

    @Test
    public void testActivityCreated() {
        assertNotNull(mActivity);
    }


    @Test
    public void testEditMedicine() {

        assertEquals(1, DB.routines().count());
        assertEquals(NAME_BEFORE_EDIT, DB.routines().findAll().get(0).name());

        // type name
        onView(withId(R.id.routine_edit_name)).perform(clearText()).perform(typeText(NAME_AFTER_EDIT));
        // close Soft Keyboard
        TestUtils.closeKeyboard();
        // set routine time (not possible vía UI)
        setTimepickerTime(20, 00);
        // open time picker
        onView(withId(R.id.button2)).perform(click());
        // check its open
        onView(withId(R.id.done_button)).check(matches(isDisplayed()));
        // press done
        onView(withId(R.id.done_button)).perform(click());
        // check button has the correct time
        onView(withId(R.id.button2)).check(matches(withText("20:00")));
        // click save
        onView(withId(R.id.add_button)).perform(click());

        // find edited routine and do assertions
        Routine r = DB.routines().findOneBy(Routine.COLUMN_NAME, NAME_AFTER_EDIT);
        assertEquals(1, DB.routines().count());
        assertNotNull(r);
        assertEquals(NAME_AFTER_EDIT, r.name());
        assertEquals(new LocalTime(20, 0), r.time());
    }


    private void setTimepickerTime(final int hour, final int minute) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RoutineCreateOrEditFragment f = (RoutineCreateOrEditFragment) mActivity.getViewPagerFragment(0);
                f.onDialogTimeSet(0, hour, minute);
            }
        });
    }


}