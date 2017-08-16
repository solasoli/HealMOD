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
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import es.usc.citius.servando.calendula.R;

/**
 * Helper class for showing and canceling message
 * notifications.
 * <p/>
 * This class makes heavy use of the {@link android.support.v4.app.NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class PickupNotification {

    public static final String TAG = PickupNotification.class.getName();
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Pickup";

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p/>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p/>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of message notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(android.content.Context)
     */
    public static void notify(final Context context, final String title, final String description, Intent intent) {

        final Resources res = context.getResources();

        // if notifications are disabled, exit
        // boolean notifications = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("alarm_notifications", true);
        //        if (!notifications) {
        //            return;
        //        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String delayMinutesStr = prefs.getString("alarm_repeat_frequency", "15");


        // This image is used as the notification's large icon (thumbnail).
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_event_available_black_48dp);
        //final Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_pill_48dp);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBigContentTitle(description);
        //style.setSummaryText("Notification Summary");

        final String ticker = title;
        final String text = title;

        PendingIntent defaultIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_event_available_white_48dp)
                .setLargeIcon(picture)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(picture)
                .setTicker(ticker)
                .setContentIntent(defaultIntent)
                .setStyle(style)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setVibrate(new long[]{1000, 200, 500, 200, 100, 200, 1000})
                        //.setSound(ringtoneUri != null ? ringtoneUri : Settings.System.DEFAULT_NOTIFICATION_URI)
                .setAutoCancel(true);

        Notification n = builder.build();
        n.defaults = 0;
        n.ledARGB = 0x00ffa500;
        n.ledOnMS = 1000;
        n.ledOffMS = 2000;
        notify(context, n);
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}