package es.usc.citius.servando.calendula.activities;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.TestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class MedicinesActivityCreateTest extends ActivityInstrumentationTestCase2<MedicinesActivity> {

    public static final String NAME = "Aspirin";

    private MedicinesActivity mActivity;

    public MedicinesActivityCreateTest() {
        super(MedicinesActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        CalendulaApp.disableReceivers = true;
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        DB.init(getInstrumentation().getContext());
        DB.dropAndCreateDatabase();

        mActivity = getActivity();
    }


    @Test
    public void testActivityCreated() {
        assertNotNull(mActivity);
    }

    @Test
    public void testCreateMedicine() {

        assertEquals(DB.medicines().count(), 0);

        // type name
        onView(withId(R.id.medicine_edit_name))
                .perform(typeText(NAME), ViewActions.closeSoftKeyboard());
        // close Soft Keyboard
        TestUtils.closeKeyboard();
        // select capsules presentation
        onView(withId(R.id.med_presentation_2))
                .perform(click());
        // click save
        onView(withId(R.id.add_button))
                .perform(click());

        Medicine m = DB.medicines().findOneBy(Medicine.COLUMN_NAME, NAME);
        assertNotNull(m);
        assertEquals(NAME, m.name());
        assertEquals(Presentation.CAPSULES, m.presentation());
    }


}