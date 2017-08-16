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

package es.usc.citius.servando.calendula.activities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;
import android.text.SpannableStringBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;
import java.util.Random;

import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Patient;
import es.usc.citius.servando.calendula.persistence.Routine;
import es.usc.citius.servando.calendula.persistence.Schedule;
import es.usc.citius.servando.calendula.persistence.ScheduleItem;
import es.usc.citius.servando.calendula.scheduling.AlarmIntentParams;
import es.usc.citius.servando.calendula.scheduling.NotificationEventReceiver;
import es.usc.citius.servando.calendula.util.AvatarMgr;

/**
 * Helper class for showing and canceling intake notifications
 * notifications.
 */
public class ReminderNotification {

    public static final String TAG = ReminderNotification.class.getName();

    private static final String NOTIFICATION_ROUTINE_TAG = "Reminder";
    private static final String NOTIFICATION_SCHEDULE_TAG = "ScheduleReminder";

    private static Random random = new Random();


    private static class NotificationOptions{
        int notificationNumber;
        boolean insistent;
        boolean lost = false;
        String title;
        String text;
        String ticker;
        String tag;
        Bitmap picture;
        PendingIntent defaultIntent;
        PendingIntent cancelIntent;
        PendingIntent delayIntent;
        NotificationCompat.InboxStyle style;
        Uri ringtone;
        long when;

    }

    public static int routineNotificationId(int routineId){
        return ("routine_notification_"+routineId).hashCode();
    }

    public static int scheduleNotificationId(int schedule){
        return ("schedule_notification_"+schedule).hashCode();
    }

    public static void notify(final Context context, final String title, Routine r, List<ScheduleItem> doses, LocalDate date, Intent intent, boolean lost)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifications = prefs.getBoolean("alarm_notifications", true);

        if (!notifications)
        {
            return;
        }

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBigContentTitle(title);
        styleForRoutine(context, style, r, doses, lost);
        Pair<Intent, Intent> intents = lost ? null : getIntentsForRoutine(context, r, date);


        NotificationOptions options = new NotificationOptions();
        options.style = style;
        options.lost = lost;
        options.when = r.time().toDateTimeToday().getMillis();
        options.tag = NOTIFICATION_ROUTINE_TAG;
        options.notificationNumber =  doses.size();
        options.picture = getLargeIcon(context.getResources(), r.patient());
        options.text = r.name() + " (" + doses.size() + " " + context.getString(R.string.home_menu_medicines).toLowerCase() + ")";

        notify(context, routineNotificationId(r.getId().intValue()) ,title,intents,intent, options);
    }


    public static void notify(final Context context, final String title, Schedule schedule, LocalDate date, LocalTime time, Intent intent, boolean lost)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifications = prefs.getBoolean("alarm_notifications", true);

        if (!notifications)
        {
            return;
        }

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBigContentTitle(title);
        styleForSchedule(context, style, schedule, lost);
        Pair<Intent, Intent> intents = lost ? null : getIntentsForSchedule(context, schedule, date, time);

        NotificationOptions options = new NotificationOptions();
        options.style = style;
        options.lost = lost;
        options.when = time.toDateTimeToday().getMillis();
        options.tag = NOTIFICATION_SCHEDULE_TAG;
        options.notificationNumber = 1;
        options.picture = getLargeIcon(context.getResources(), schedule.patient());
        options.text = schedule.medicine().name() + " (" + schedule.toReadableString(context) + ")";
        notify(context, scheduleNotificationId(schedule.getId().intValue()), title, intents, intent, options);
    }

    private static void notify(final Context context, int id, final String title,
                               Pair<Intent, Intent> actionIntents, Intent intent,
                               NotificationOptions options){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifications = prefs.getBoolean("alarm_notifications", true);

        // if notifications are disabled, exit
        if (!notifications)
        {
            return;
        }

        final Resources res = context.getResources();
        boolean insistentNotifications = prefs.getBoolean("alarm_insistent", false);
        // prepare notification intents
        PendingIntent defaultIntent = PendingIntent.getActivity(context, random.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent delayIntent = null;
        PendingIntent cancelIntent = null;

        if(!options.lost) {
            delayIntent = PendingIntent.getActivity(context, random.nextInt(), actionIntents.first, PendingIntent.FLAG_UPDATE_CURRENT);
            cancelIntent = PendingIntent.getBroadcast(context, random.nextInt(), actionIntents.second, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        int ic = options.lost ? R.drawable.ic_pill_small_lost : R.drawable.ic_pill_small;
        options.picture = options.picture != null ? options.picture : BitmapFactory.decodeResource(res, ic);
        options.insistent = insistentNotifications;
        options.title = title;
        options.ticker = title;
        options.defaultIntent = defaultIntent;
        if(!options.lost) {
            options.cancelIntent = cancelIntent;
            options.delayIntent = delayIntent;
        }
        options.ringtone = getRingtoneUri(prefs, insistentNotifications);

        Notification n = buildNotification(context, options);
        notify(context, id, n, options.tag);
    }


    private static Notification buildNotification(Context context, NotificationOptions options){

        Resources res = context.getResources();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound, and vibration.
                .setDefaults(Notification.DEFAULT_ALL)
                        // Set required fields, including the small icon, the notification title, and text.
                .setSmallIcon(options.lost ? R.drawable.ic_pill_small_lost : R.drawable.ic_pill_small)
                .setContentTitle(options.title)
                .setContentText(options.text)
                .setPriority(options.insistent ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_DEFAULT)
                        // Provide a large icon, shown with the notification in the
                        // notification drawer on devices running Android 3.0 or later.
                .setLargeIcon(options.picture)
                        // Set ticker text (preview) information for this notification.
                .setTicker(options.ticker)
                        // Show a number. This is useful when stacking notifications of a single type.
                .setNumber(options.notificationNumber)
                .setWhen(options.when)
                        // Set the pending intent to be initiated when the user touches the notification.
                .setContentIntent(options.defaultIntent)
                        // Show an expanded list of items on devices running Android 4.1
                        // or later.
                .setStyle(options.style)
                        //.setLights(0x00ff0000, 500, 1000)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setVibrate(new long[] { 1000, 200, 500, 200, 100, 200, 1000 }).setSound(options.ringtone)
                        // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        if (!options.lost) {
            // add delay button and cancel button
            builder.addAction(R.drawable.ic_history_white_24dp, res.getString(R.string.notification_delay), options.delayIntent)
                   .addAction(R.drawable.ic_alarm_off_white_24dp, res.getString(R.string.notification_cancel_now), options.cancelIntent);
        }

        if(options.insistent){
            builder.setFullScreenIntent(options.defaultIntent, true);
        }

        Notification n = builder.build();
        n.defaults = 0;
        n.ledARGB = 0x00ffa500;
        n.ledOnMS = 1000;
        n.ledOffMS = 2000;

        if (!options.lost && options.insistent)
        {
            n.flags |= Notification.FLAG_INSISTENT;
        }

        return n;
    }

    private static void styleForRoutine(Context ctx, NotificationCompat.InboxStyle style, Routine r, List<ScheduleItem> doses, boolean lost){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        for (ScheduleItem scheduleItem : doses)
        {
            //TODO: Use DecimalFormat
            // DecimalFormat timeFormatter = new DecimalFormat("#");
            //String dfDose = timeFormatter.format(scheduleItem.dose());

            final SpannableStringBuilder SpItem = new SpannableStringBuilder();
            final Medicine med = scheduleItem.schedule().medicine();
            SpItem.append(med.name());
            SpItem.append(":  " + scheduleItem.dose()  + " " + med.presentation().units(ctx.getResources()));
            style.addLine(SpItem);
        }
        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");
        int delayMinutes = (int) Long.parseLong(delayMinutesStr);

        if (delayMinutes > 0 && !lost)
        {
            String repeatTime = DateTime.now().plusMinutes(delayMinutes).toString("HH:mm");
            style.setSummaryText(ctx.getResources().getString(R.string.notification_repeat_message, repeatTime));
        } else
        {
            style.setSummaryText(doses.size() + " " + ctx.getString(R.string.medicine) + (doses.size()>1?"s, ":", ") + r.name());
        }

    }

    private static void styleForSchedule(Context context, NotificationCompat.InboxStyle style, Schedule schedule, boolean lost) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final Medicine med = schedule.medicine();
        final SpannableStringBuilder SpItem = new SpannableStringBuilder();
        SpItem.append(med.name());
        SpItem.append("   " + schedule.dose() + " " + med.presentation().units(context.getResources()));
        style.addLine(SpItem);

        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");
        int delayMinutes = (int) Long.parseLong(delayMinutesStr);

        if (delayMinutes > 0 && !lost)
        {
            String repeatTime = DateTime.now().plusMinutes((int) delayMinutes).toString("kk:mm");
            style.setSummaryText(context.getString(R.string.notification_repeat_message, repeatTime));
        } else
        {
            style.setSummaryText(med.name() + "(" + context.getString(R.string.every) + " " + schedule.rule()
                    .interval() + " " + context.getString(R.string.hours) + ")");
        }
    }

    private static Pair<Intent,Intent> getIntentsForRoutine(Context context, Routine r, LocalDate date){

        // delay intent sent on click delay button
        final Intent delay = new Intent(context, ConfirmActivity.class);
        delay.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, "delay");
        delay.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, r.getId());
        delay.putExtra("date", date.toString(AlarmIntentParams.DATE_FORMAT));

        // delay intent sent on click delay button
        final Intent cancel = new Intent(context, NotificationEventReceiver.class);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CANCEL_ROUTINE);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ROUTINE_ID, r.getId());
        cancel.putExtra("date", date.toString(AlarmIntentParams.DATE_FORMAT));

        return new Pair<>(delay, cancel);
    }

    private static Pair<Intent,Intent> getIntentsForSchedule(Context context, Schedule schedule, LocalDate date, LocalTime time){

        final Intent delay = new Intent(context, ConfirmActivity.class);
        delay.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, "delay");
        delay.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
        delay.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, date.toString("kk:mm"));
        delay.putExtra("date", date.toString(AlarmIntentParams.DATE_FORMAT));

        final Intent cancel = new Intent(context, NotificationEventReceiver.class);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_ACTION, CalendulaApp.ACTION_CANCEL_HOURLY_SCHEDULE);
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_ID, schedule.getId());
        cancel.putExtra(CalendulaApp.INTENT_EXTRA_SCHEDULE_TIME, time.toString("kk:mm"));
        cancel.putExtra("date", date.toString(AlarmIntentParams.DATE_FORMAT));

        return new Pair<>(delay, cancel);
    }

    private static Uri getRingtoneUri(SharedPreferences prefs, boolean insistentNotifications){

        String ringtonePref = prefs.getString("pref_notification_tone", null);

        if (insistentNotifications)
        {
            return ringtonePref != null ? Uri.parse(ringtonePref) : Settings.System.DEFAULT_ALARM_ALERT_URI;
        } else
        {
            return Settings.System.DEFAULT_NOTIFICATION_URI;
        }
    }

    private static Bitmap getLargeIcon(Resources r, Patient p){
        return BitmapFactory.decodeResource(r, AvatarMgr.res(p.avatar()));
    }


    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, int id, final Notification notification, String tag)
    {
        //int id = Math.abs(tag.hashCode());
        final NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
        {
            nm.notify(tag, id, notification);
        } else
        {
            nm.notify(id, notification);
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context, int id)
    {
        final NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
        {
            nm.cancel(NOTIFICATION_ROUTINE_TAG, id);
            nm.cancel(NOTIFICATION_SCHEDULE_TAG, id);
        } else
        {
            nm.cancel(id);
            nm.cancel(id);
        }
    }

}