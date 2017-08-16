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

package es.usc.citius.servando.calendula.scheduling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;

/**
 * This class receives our routine alarms
 */
public class NotificationEventReceiver extends BroadcastReceiver {

    public static final String TAG = NotificationEventReceiver.class.getName();


    @Override
    public void onReceive(Context context, Intent intent) {

        long routineId;
        long scheduleId;
        String scheduleTime;
        LocalDate date;

        int action = intent.getIntExtra(CalendulaApp.INTENT_EXTRA_ACTION, -1);

        Log.d(TAG, "Notification event received - Action : " + action);

        String dateStr = intent.getStringExtra("date");
        if (dateStr != null) {
            date = DateTimeFormat.forPattern(AlarmIntentParams.DATE_FORMAT).parseLocalDate(dateStr);
        } else {
            Log.w(TAG, "Date not supplied, assuming today.");
            date = LocalDate.now();
        }

        switch (action) {

            case CalendulaApp.ACTION_CANCEL_ROUTINE:
                routineId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, -1);
                if (routineId != -1) {
                    AlarmScheduler.instance().onIntakeCancelled(Routine.findById(routineId), date, context);
                    Toast.makeText(context, context.getString(R.string.reminder_cancelled_message), Toast.LENGTH_SHORT).show();
                }
                break;

            case CalendulaApp.ACTION_CANCEL_HOURLY_SCHEDULE:
                scheduleId = intent.getLongExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, -1);
                scheduleTime = intent.getStringExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME);
                if (scheduleId != -1 && scheduleTime != null) {
                    LocalTime t = DateTimeFormat.forPattern("kk:mm").parseLocalTime(scheduleTime);
                    AlarmScheduler.instance().onIntakeCancelled(Schedule.findById(scheduleId), t, date, context);
                    Toast.makeText(context,context.getString(R.string.reminder_cancelled_message),Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Log.d(TAG, "Request not handled " + intent.toString());
                break;
        }

    }

}