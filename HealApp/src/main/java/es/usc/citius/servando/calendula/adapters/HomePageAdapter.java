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

package es.usc.citius.servando.calendula.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.fragments.DailyAgendaFragment;
import es.usc.citius.servando.calendula.fragments.MedicinesListFragment;
import es.usc.citius.servando.calendula.fragments.RoutinesListFragment;
import es.usc.citius.servando.calendula.fragments.ScheduleListFragment;
import es.usc.citius.servando.calendula.util.ScreenUtils;

/**
 * Created by joseangel.pineiro on 11/15/13.
 */
public class HomePageAdapter extends FragmentPagerAdapter {

    String[] titles;
    private float dpWidth;

    public HomePageAdapter(FragmentManager fm, Context ctx, Activity activity) {
        super(fm);
        // obtain the window width in dp to use later
        dpWidth = ScreenUtils.getDpSize(activity).x;

        titles = new String[]{
                ctx.getString(R.string.title_home),
                ctx.getString(R.string.title_activity_routines),
                ctx.getString(R.string.title_activity_medicines),
                ctx.getString(R.string.title_activity_schedules)
        };
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DailyAgendaFragment();
            case 1:
                return new RoutinesListFragment();
            case 2:
                return new MedicinesListFragment();
            case 3:
                return new ScheduleListFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages. Home and agenda
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";//titles[position];
    }
}
