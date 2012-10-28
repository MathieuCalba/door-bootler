package org.doorbootler.auth;

import org.doorbootler.auth.sender.OpeningDoorSender;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.jakewharton.notificationcompat2.NotificationCompat2;

public class NotificationHelper {

	public static final String EXTRA_OVER_DATA_NETWORK = "org.doorbeller.EXTRA_OVER_DATA_NETWORK";

	public static void showNotification(Context ctx, Bitmap bitmap, boolean doorRequestOnDataNetwork) {
		final NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		final String title = ctx.getString(R.string.notification_title);
		final String content = ctx.getString(R.string.notification_content);

		Intent i = new Intent(ctx, OfficeActivity.class);
		i.putExtra(EXTRA_OVER_DATA_NETWORK, doorRequestOnDataNetwork);
		final PendingIntent pIntentTalk = PendingIntent.getActivity(ctx, 0, i, 0);

		i = new Intent(OpeningDoorSender.ACTION_OPEN_DOOR);
		i.putExtra(EXTRA_OVER_DATA_NETWORK, doorRequestOnDataNetwork);
		final PendingIntent pIntentOpen = PendingIntent.getBroadcast(ctx, 0, i, 0);

		NotificationCompat2.Builder notifBuilder = new NotificationCompat2.Builder(ctx);
		notifBuilder.setTicker(title);
		notifBuilder.setContentTitle(title);
		notifBuilder.setLargeIcon(bitmap);
		notifBuilder.setSmallIcon(R.drawable.ic_launcher);
		notifBuilder.setContentIntent(pIntentTalk);
		notifBuilder.setStyle(new NotificationCompat2.BigPictureStyle().bigPicture(bitmap).setBigContentTitle(content));
		notifBuilder.setPriority(NotificationCompat2.PRIORITY_MAX);
		notifBuilder.addAction(0, ctx.getString(R.string.btn_talk), pIntentTalk);
		notifBuilder.addAction(0, ctx.getString(R.string.btn_open), pIntentOpen);

		mgr.notify(R.id.door_notification, notifBuilder.build());
	}

	public static void hideNotification(Context ctx) {
		final NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.cancel(R.id.door_notification);
	}

}
