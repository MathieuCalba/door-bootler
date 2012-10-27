package org.doorbeller.act;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OpeningDoor extends BroadcastReceiver {

	public static final String ACTION = "org.doorbeller.action.OPEN_DOOR";

	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationHelper.hideNotification(context);
		openDoor(context);
	}

	public static void openDoor(Context ctx) {
		Intent i = new Intent(SMSSender.ACTION);
		final String[] extras = { "Door1" };
		i.putExtra(SMSSender.SMS_EXTRA_NAME, extras);
		ctx.sendBroadcast(i);
	}

}
