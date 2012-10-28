package org.doorbeller.act.sender;

import org.doorbeller.act.NotificationHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class OpeningDoorSender extends BroadcastReceiver {

	public static final String ACTION_OPEN_DOOR = "org.doorbeller.ACTION_OPEN_DOOR";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (!TextUtils.isEmpty(action) && action.equalsIgnoreCase(ACTION_OPEN_DOOR)) {
				NotificationHelper.hideNotification(context);
				openDoor(context);
			}
		}
	}

	private void openDoor(Context ctx) {
		// if data connectivity ok for this device and the other device too
		// -> sending the door opening approval by push with NMA
		// else
		// -> sending the door opening approval by sms
		Intent i = new Intent(SMSSender.ACTION);
		final String[] extras = { "Door1" };
		i.putExtra(SMSSender.SMS_EXTRA_NAME, extras);
		ctx.sendBroadcast(i);
	}

}
