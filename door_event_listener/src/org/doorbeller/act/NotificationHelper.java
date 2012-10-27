package org.doorbeller.act;

import org.doorbeller.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.jakewharton.notificationcompat2.NotificationCompat2;

public class NotificationHelper {

	public static void showNotification(Context ctx, Bitmap bitmap) {
		final NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		String title = ctx.getString(R.string.notification_title);
		String content = ctx.getString(R.string.notification_content);
		PendingIntent pIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, OfficeActivity.class), 0);
		// TODO : depending on how we notify the door phone about the
		// instruction to open the door, we need a PendingIntent to a
		// BroadcastReceiver that sends an sms to open the door
		PendingIntent pIntentOpen = PendingIntent.getActivity(ctx, 0, new Intent(ctx, OfficeActivity.class), 0);

		NotificationCompat2.Builder notifBuilder = new NotificationCompat2.Builder(ctx);
		notifBuilder.setTicker(title);
		notifBuilder.setContentTitle(title);
		notifBuilder.setAutoCancel(true);
		notifBuilder.setLargeIcon(bitmap);
		notifBuilder.setSmallIcon(R.drawable.ic_launcher);
		notifBuilder.setContentIntent(pIntent);
		notifBuilder.setStyle(new NotificationCompat2.BigPictureStyle().bigPicture(bitmap).setBigContentTitle(content));
		notifBuilder.setPriority(NotificationCompat2.PRIORITY_MAX);
		notifBuilder.addAction(0, ctx.getString(R.string.btn_talk), pIntent);
		notifBuilder.addAction(0, ctx.getString(R.string.btn_open), pIntentOpen);

		mgr.notify(R.id.door_notification, notifBuilder.build());
	}

	public static void hideNotification(Context ctx) {
		final NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		mgr.cancel(R.id.door_notification);
	}

}
