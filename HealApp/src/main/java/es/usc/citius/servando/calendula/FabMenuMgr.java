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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.servando.calendula.activities.ConfirmSchedulesActivity;
import es.usc.citius.servando.calendula.activities.LeftDrawerMgr;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.RoutinesActivity;
import es.usc.citius.servando.calendula.activities.ScanActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.activities.SchedulesHelpActivity;
import es.usc.citius.servando.calendula.fragments.ScheduleTypeFragment;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.util.ScreenUtils;

/**
 * Helper to manage the home screen floating action button behaviour
 */
public class FabMenuMgr implements View.OnClickListener{


    LeftDrawerMgr drawerMgr;
    FloatingActionsMenu fabMenu;
    FloatingActionButton fab;
    HomePagerActivity activity;
    List<FloatingActionButton> scheduleActions;
    FloatingActionButton scanQrAction;

    private int currentPage = 0;


    public FabMenuMgr(FloatingActionButton fab, FloatingActionsMenu fabMenu, LeftDrawerMgr drawerMgr, HomePagerActivity a) {
        this.fab = fab;
        this.fabMenu = fabMenu;
        this.activity = a;
        this.drawerMgr = drawerMgr;
        this.scheduleActions = getScheduleActions();

        final SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

        fabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                if(!prefs.getBoolean("PREFERENCE_SCHEDULE_HELP_SHOWN", false)){
                    activity.launchActivityDelayed(SchedulesHelpActivity.class, 600);
                }
            }

            @Override
            public void onMenuCollapsed() {

            }
        });

    }

    private List<FloatingActionButton> getScheduleActions() {
        ArrayList<FloatingActionButton> actions = new ArrayList<>();

        FloatingActionButton actionA = (FloatingActionButton) fabMenu.findViewById(R.id.action_a);
        FloatingActionButton actionB = (FloatingActionButton) fabMenu.findViewById(R.id.action_b);
        FloatingActionButton actionC = (FloatingActionButton) fabMenu.findViewById(R.id.action_c);

        actions.add(actionA);
        actions.add(actionB);
        actions.add(actionC);
        scanQrAction = (FloatingActionButton) fabMenu.findViewById(R.id.action_d);
        if(CalendulaApp.isPharmaModeEnabled(activity)) {
            scanQrAction.setVisibility(View.VISIBLE);
            actions.add(scanQrAction);
        }else{
            scanQrAction.setVisibility(View.GONE);
        }
        return actions;
    }


    public void init() {

        for(FloatingActionButton f : scheduleActions){
            f.setOnClickListener(this);
        }

        fab.setOnClickListener(this);
        fab.setIconDrawable(new IconicsDrawable(activity)
                .icon(GoogleMaterial.Icon.gmd_plus)
                .paddingDp(5)
                .sizeDp(24)
                .color(Color.parseColor("#263238")));

        onViewPagerItemChange(0);
    }

    public void onViewPagerItemChange(int currentPage){

        this.currentPage = currentPage;

        fab.setColorNormal(Color.parseColor("#ecf0f1"));
        fab.setColorPressed(Color.parseColor("#e7e7e7"));

//        fab.setColorNormalResId(getFabColor(currentPage));
//        fab.setColorPressedResId(getFabPressedColor(currentPage));

        switch (currentPage){

            case 0:
                fabMenu.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                break;
            case 1:
            case 2:
                for(FloatingActionButton f : scheduleActions){
                    f.setVisibility(View.GONE);
                }
                fabMenu.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
                fab.bringToFront();
                break;

            case 3:
                for(FloatingActionButton f : scheduleActions){
                    f.setVisibility(View.VISIBLE);
                }
                fab.setVisibility(View.GONE);
                fabMenu.setVisibility(View.VISIBLE);
                fabMenu.bringToFront();
                break;
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.add_button:
                onClickAdd();
                break;

            // schedules
            case R.id.action_a:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_ROUTINES);
                break;
            case R.id.action_b:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_HOURLY);
                break;
            case R.id.action_c:
                startSchedulesActivity(ScheduleTypeFragment.TYPE_PERIOD);
                break;
            case R.id.action_d:
                startScanActivity();
                break;

        }


    }

    private void onClickAdd() {
        switch (currentPage){
            case 0:
                return;
            case 1:
                launchActivity(RoutinesActivity.class);
                break;
            case 2:
                launchActivity(MedicinesActivity.class);
                break;
            case 3:
                return;
        }
    }

    private void launchActivity(Class<?> type){
        activity.startActivity(new Intent(activity, type));
        activity.overridePendingTransition(0, 0);
    }

    private void startSchedulesActivity(int scheduleType){
        Intent i = new Intent(activity, ScheduleCreationActivity.class);
        i.putExtra("scheduleType", scheduleType);
        activity.startActivity(i);
        activity.overridePendingTransition(0, 0);
        fabMenu.collapse();
    }

    private void startScanActivity(){
        Intent i = new Intent(activity, ScanActivity.class);
        i.putExtra("after_scan_pkg", activity.getPackageName());
        i.putExtra("after_scan_cls", ConfirmSchedulesActivity.class.getName());
        activity.startActivity(i);
        activity.overridePendingTransition(0, 0);
        fabMenu.collapse();
        return;
    }

    public int getFabColor(int page) {
        switch (page) {
            case 1:
                return R.color.android_orange;
            case 2:
                return R.color.android_pink_dark;
            case 3:
                return R.color.android_green;
            default:
                return R.color.android_blue_darker;
        }
    }

    public int getFabPressedColor(int page) {
        switch (page) {
            case 1:
                return R.color.android_orange_dark;
            case 2:
                return R.color.android_pink;
            case 3:
                return R.color.android_green_dark;
            default:
                return R.color.android_blue_dark;
        }
    }

    public void onPatientUpdate(Patient p){
        for(FloatingActionButton f: scheduleActions){
            f.setColorNormal(p.color());
            f.setColorPressed(ScreenUtils.equivalentNoAlpha(p.color(), 0.7f));
        }
    }

    public void onPharmacyModeChanged(boolean enabled){
        if(enabled && !scheduleActions.contains(scanQrAction)){
            scheduleActions.add(scanQrAction);
            scanQrAction.setVisibility(View.VISIBLE);
        }else if(!enabled && scheduleActions.contains(scanQrAction)){
            scanQrAction.setVisibility(View.GONE);
            scheduleActions.remove(scanQrAction);
        }
        fabMenu.invalidate();
        onViewPagerItemChange(currentPage);
    }

}
