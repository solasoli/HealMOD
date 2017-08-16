/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import org.joda.time.DateTime;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import es.usc.citius.servando.calendula.activities.CalendarActivity;
import es.usc.citius.servando.calendula.activities.ConfirmActivity;
import es.usc.citius.servando.calendula.activities.LeftDrawerMgr;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.activities.SchedulesHelpActivity;
import es.usc.citius.servando.calendula.adapters.HomePageAdapter;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.events.PersistenceEvents;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.HomeProfileMgr;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.scheduling.DailyAgenda;
import es.usc.citius.servando.calendula.services.PopulatePrescriptionDBService;
import es.usc.citius.servando.calendula.util.FragmentUtils;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.Snack;

public class HomePagerActivity extends CalendulaActivity implements
        RoutinesListFragment.OnRoutineSelectedListener,
        MedicinesListFragment.OnMedicineSelectedListener,
        ScheduleListFragment.OnScheduleSelectedListener {

    private static final String TAG = "HomePagerActivity";
    AppBarLayout appBarLayout;
    CollapsingToolbarLayout toolbarLayout;
    HomeProfileMgr homeProfileMgr;
    View userInfoFragment;
    FloatingActionsMenu addButton;
    FabMenuMgr fabMgr;
    TextView toolbarTitle;
    MenuItem expandItem;
    MenuItem helpItem;
    Drawable icAgendaMore;
    Drawable icAgendaLess;
    boolean appBarLayoutExpanded = true;
    boolean active = false;
    private HomePageAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private LeftDrawerMgr drawerMgr;
    private FloatingActionButton fab;
    private Patient activePatient;
    private int pendingRefresh = -2;
    private Queue<Object> pendingEvents = new LinkedList<>();
    private Handler handler;

    public void showPagerItem(int position) {
        showPagerItem(position, true);
    }

    public void showPagerItem(int position, boolean updateDrawer) {
        if (position >= 0 && position < mViewPager.getChildCount()) {
            mViewPager.setCurrentItem(position);
            if (updateDrawer) {
                drawerMgr.onPagerPositionChange(position);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        expandItem = menu.findItem(R.id.action_expand);
        helpItem = menu.findItem(R.id.action_schedules_help);
        helpItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        int pageNum = mViewPager.getCurrentItem();

        if (pageNum == 0) {
            boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(0)).isExpanded();
            menu.findItem(R.id.action_expand).setVisible(true);
            menu.findItem(R.id.action_expand).setIcon(!expanded ? icAgendaMore : icAgendaLess);
        } else {
            menu.findItem(R.id.action_expand).setVisible(false);
        }

        if (pageNum == 2 && CalendulaApp.isPharmaModeEnabled(this)) {
            menu.findItem(R.id.action_calendar).setVisible(true);
        } else {
            menu.findItem(R.id.action_calendar).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_calendar:
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            case R.id.action_expand:

                final boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(0)).isExpanded();
                appBarLayout.setExpanded(expanded);


                boolean delay = appBarLayoutExpanded && !expanded || !appBarLayoutExpanded && expanded;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((DailyAgendaFragment) getViewPagerFragment(0)).toggleViewMode();
                    }
                }, delay ? 500 : 0);

                item.setIcon(expanded ? icAgendaMore : icAgendaLess);
                return true;
            case R.id.action_schedules_help:
                launchActivity(new Intent(this, SchedulesHelpActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void enableOrDisablePharmacyMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (CalendulaApp.isPharmaModeEnabled(getApplicationContext())) {
            prefs.edit().putBoolean(CalendulaApp.PHARMACY_MODE_ENABLED, false).commit();
            Snack.show("Acabas de deshabilitar el modo farmacia!", HomePagerActivity.this);
            fabMgr.onPharmacyModeChanged(false);
            drawerMgr.onPharmacyModeChanged(false);
        } else {
            prefs.edit().putBoolean(CalendulaApp.PHARMACY_MODE_ENABLED, true)
                    .putBoolean("enable_prescriptions_db", true)
                    .commit();
            try {
                DB.prescriptions().executeRaw("DELETE FROM Prescriptions;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            new PopulatePrescriptionDatabaseTask().execute("");
        }
    }

    public void launchActivityDelayed(final Class<?> activityClazz, int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(HomePagerActivity.this, activityClazz));
                overridePendingTransition(0, 0);
            }
        }, delay);

    }

    @Override
    public void onRoutineSelected(Routine r) {
        Intent i = new Intent(this, RoutinesActivity.class);
        i.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, r.getId());
        launchActivity(i);
    }

    @Override
    public void onCreateRoutine() {
        //do nothing
    }

    @Override
    public void onMedicineSelected(Medicine m) {
        Intent i = new Intent(this, MedicinesActivity.class);
        i.putExtra(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, m.getId());
        launchActivity(i);
    }

    @Override
    public void onCreateMedicine() {

        //do nothing
    }

    @Override
    public void onScheduleSelected(Schedule r) {
        Intent i = new Intent(this, ScheduleCreationActivity.class);
        i.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, r.getId());
        launchActivity(i);
    }

    @Override
    public void onCreateSchedule() {

    }

    // Method called from the event bus
    @SuppressWarnings("unused")
    public void onEvent(final Object evt) {
        if (active) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (evt instanceof PersistenceEvents.ModelCreateOrUpdateEvent) {
                        PersistenceEvents.ModelCreateOrUpdateEvent event = (PersistenceEvents.ModelCreateOrUpdateEvent) evt;
                        Log.d(TAG, "onEvent: " + event.clazz.getName());
                        ((DailyAgendaFragment) getViewPagerFragment(0)).notifyDataChange();
                        ((RoutinesListFragment) getViewPagerFragment(1)).notifyDataChange();
                        ((MedicinesListFragment) getViewPagerFragment(2)).notifyDataChange();
                        ((ScheduleListFragment) getViewPagerFragment(3)).notifyDataChange();
                    } else if (evt instanceof PersistenceEvents.ActiveUserChangeEvent) {
                        activePatient = ((PersistenceEvents.ActiveUserChangeEvent) evt).patient;
                        updateTitle(mViewPager.getCurrentItem());
                        toolbarLayout.setContentScrimColor(activePatient.color());
                        fabMgr.onPatientUpdate(activePatient);
                    } else if (evt instanceof PersistenceEvents.UserUpdateEvent) {
                        Patient p = ((PersistenceEvents.UserUpdateEvent) evt).patient;
                        ((DailyAgendaFragment) getViewPagerFragment(0)).onUserUpdate();
                        drawerMgr.onPatientUpdated(p);
                        if (DB.patients().isActive(p, HomePagerActivity.this)) {
                            activePatient = p;
                            updateTitle(mViewPager.getCurrentItem());
                            toolbarLayout.setContentScrimColor(activePatient.color());
                            fabMgr.onPatientUpdate(activePatient);
                        }
                    } else if (evt instanceof PersistenceEvents.UserCreateEvent) {
                        Patient created = ((PersistenceEvents.UserCreateEvent) evt).patient;
                        drawerMgr.onPatientCreated(created);
                    } else if (evt instanceof HomeProfileMgr.BackgroundUpdatedEvent) {
                        ((DailyAgendaFragment) getViewPagerFragment(0)).refresh();
                    } else if (evt instanceof ConfirmActivity.ConfirmStateChangeEvent) {
                        pendingRefresh = ((ConfirmActivity.ConfirmStateChangeEvent) evt).position;
                        ((DailyAgendaFragment) getViewPagerFragment(0)).refreshPosition(pendingRefresh);
                    } else if (evt instanceof DailyAgenda.AgendaUpdatedEvent) {
                        ((DailyAgendaFragment) getViewPagerFragment(0)).notifyDataChange();
                        homeProfileMgr.updateDate();
                    }
                }
            });
        } else {
            pendingEvents.add(evt);
        }
    }

    void showMessage(String text) {
        Snackbar.make(appBarLayout, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    void updateAempsIfNeeded() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int msgRes = R.string.updating_prescriptions_db_msg;
        boolean dbEnabled = prefs.getBoolean("enable_prescriptions_db", false);
        if (dbEnabled && PopulatePrescriptionDBService.isDbOutdated(this)) {
            new PopulatePrescriptionDatabaseTask(msgRes).execute("");
        }
    }

    Fragment getViewPagerFragment(int position) {
        String tag = FragmentUtils.makeViewPagerFragmentName(R.id.container, position);
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar(null, Color.TRANSPARENT);
        initializeDrawer(savedInstanceState);
        setupStatusBar(Color.TRANSPARENT);
        subscribeToEvents();
        handler = new Handler();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new HomePageAdapter(getSupportFragmentManager(), this, this);
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(getPageChangeListener());
        mViewPager.setOffscreenPageLimit(5);

        // Set up home profile
        homeProfileMgr = new HomeProfileMgr();
        userInfoFragment = findViewById(R.id.user_info_fragment);
        homeProfileMgr.init(userInfoFragment, this);

        activePatient = DB.patients().getActive(this);
        toolbarLayout.setContentScrimColor(activePatient.color());


        // Setup fab
        addButton = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        fab = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.add_button);
        fabMgr = new FabMenuMgr(fab, addButton, drawerMgr, this);
        fabMgr.init();

        fabMgr.onPatientUpdate(activePatient);


        // Setup the tabLayout
        setupTabLayout();

        AppBarLayout.OnOffsetChangedListener mListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                //Log.d(TAG, "Values: (" + toolbarLayout.getHeight()+ " + " +verticalOffset + ") < (2 * " + ViewCompat.getMinimumHeight(toolbarLayout) + ")");

                if ((toolbarLayout.getHeight() + verticalOffset) < (1.8 * ViewCompat.getMinimumHeight(toolbarLayout))) {
                    homeProfileMgr.onCollapse();
                    toolbarTitle.animate().alpha(1);
                    appBarLayoutExpanded = false;
                    Log.d(TAG, "OnCollapse");
                } else {
                    appBarLayoutExpanded = true;
                    if (mViewPager.getCurrentItem() == 0) {
                        toolbarTitle.animate().alpha(0);
                    }
                    homeProfileMgr.onExpand();
                    Log.d(TAG, "OnExpand");
                }


            }
        };
        appBarLayout.addOnOffsetChangedListener(mListener);

        icAgendaLess = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_unfold_less)
                .color(Color.WHITE)
                .sizeDp(24);

        icAgendaMore = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_unfold_more)
                .color(Color.WHITE)
                .sizeDp(24);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateAempsIfNeeded();
            }
        }, 1500);




        if (getIntent() != null && getIntent().getBooleanExtra("invalid_notification_error", false)) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showInvalidNotificationError();
                }
            }, 500);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Patient p = DB.patients().getActive(this);
        drawerMgr.onActivityResume(p);
        active = true;

        // process pending events
        while (!pendingEvents.isEmpty()) {
            Log.d(TAG, "Processing pending event...");
            onEvent(pendingEvents.poll());
        }
    }

    @Override
    protected void onPause() {
        active = false;
        super.onPause();
    }


    // Interface implementations

    private void showInvalidNotificationError() {

        final boolean expanded = ((DailyAgendaFragment) getViewPagerFragment(0)).isExpanded();

        new AlertDialog.Builder(this)
                .setTitle(R.string.notification_error_title)
                .setMessage(R.string.notification_error_msg)
                .setCancelable(true)
                .setIcon(IconUtils.icon(this, CommunityMaterial.Icon.cmd_bug, R.color.black))
                .setPositiveButton(R.string.tutorial_understood, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (!expanded) {
                            appBarLayout.setExpanded(expanded);
                            expandItem.setIcon(expanded ? icAgendaMore : icAgendaLess);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((DailyAgendaFragment) getViewPagerFragment(0)).toggleViewMode();
                                }
                            }, 200);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((DailyAgendaFragment) getViewPagerFragment(0)).scrollTo(DateTime.now());
                                }
                            }, 600);
                        }
                    }
                }).create().show();
    }

    private void setupTabLayout() {

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        IIcon[] icons = new IIcon[]{
                GoogleMaterial.Icon.gmd_home,
                GoogleMaterial.Icon.gmd_alarm,
                CommunityMaterial.Icon.cmd_pill,
                GoogleMaterial.Icon.gmd_calendar,
        };

        for (int i = 0; i < tabLayout.getTabCount(); i++) {

            Drawable icon = new IconicsDrawable(this)
                    .icon(icons[i])
                    .alpha(80)
                    .paddingDp(2)
                    .color(Color.WHITE)
                    .sizeDp(24);

            tabLayout.getTabAt(i).setIcon(icon);
        }
    }

    private ViewPager.OnPageChangeListener getPageChangeListener() {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateTitle(position);
                fabMgr.onViewPagerItemChange(position);
                if (position == 0) {
                    appBarLayout.setExpanded(true);
                } else {
                    appBarLayout.setExpanded(false);
                }

                if (expandItem != null) {
                    expandItem.setVisible(position == 0);
                }
                if (helpItem != null) {
                    helpItem.setVisible(position == 3);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }

    private void updateTitle(int page) {
        String title;

        switch (page) {
            case 1:
                title = getString(R.string.title_activity_routines) + " de " + activePatient.name();
                break;
            case 2:
                title = getString(R.string.title_activity_medicines) + " de " + activePatient.name();
                break;
            case 3:
                title = getString(R.string.title_activity_schedules) + " de " + activePatient.name();
                break;
            default:
                title = "Calendula";
                break;
        }
        toolbarTitle.setText(title);
    }

    private void initializeDrawer(Bundle savedInstanceState) {
        drawerMgr = new LeftDrawerMgr(this, toolbar);
        drawerMgr.init(savedInstanceState);
    }

    private void launchActivity(Intent i) {
        startActivity(i);
        this.overridePendingTransition(0, 0);
    }

    public class PopulatePrescriptionDatabaseTask extends AsyncTask<String, String, Void> {


        ProgressDialog dialog;
        int msgResource = R.string.enable_prescriptions_progress_messgae;


        public PopulatePrescriptionDatabaseTask() {
        }

        public PopulatePrescriptionDatabaseTask(int msgResource) {
            this.msgResource = msgResource;
        }

        @Override
        protected Void doInBackground(String... params) {
            new PopulatePrescriptionDBService().updateIfNeeded(HomePagerActivity.this);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(HomePagerActivity.this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getString(msgResource));
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Snack.show("Acabas de habilitar el modo farmacia!", HomePagerActivity.this);
            fabMgr.onPharmacyModeChanged(true);
            drawerMgr.onPharmacyModeChanged(true);
        }
    }

}
