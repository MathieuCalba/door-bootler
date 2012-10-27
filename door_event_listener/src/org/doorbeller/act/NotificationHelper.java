package org.doorbeller.act;

import org.doorbeller.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.jakewharton.notificationcompat2.NotificationCompat2;

public class NotificationHelper {

	public static void showNotification(Context ctx, Bitmap bitmap) {
		final NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		final String title = ctx.getString(R.string.notification_title);
		final String content = ctx.getString(R.string.notification_content);
		final PendingIntent pIntentTalk = PendingIntent.getActivity(ctx, 0, new Intent(ctx, OfficeActivity.class), 0);
		final PendingIntent pIntentOpen = PendingIntent.getBroadcast(ctx, 0, new Intent(OpeningDoor.ACTION), 0);

		NotificationCompat2.Builder notifBuilder = new NotificationCompat2.Builder(ctx);
		notifBuilder.setTicker(title);
		notifBuilder.setContentTitle(title);
		notifBuilder.setLargeIcon(bitmap);
		notifBuilder.setSmallIcon(R.drawable.ic_launcher);
		notifBuilder.setSound(Uri.parse("file:///android_asset/old_phone_ringing.mp3"));
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
