package org.doorbootler.auth.sender;

import org.doorbootler.auth.NetworkService;
import org.doorbootler.auth.NotificationHelper;
import org.doorbootler.library.Utils;
import org.doorbootler.library.sms.SMSUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class OpeningDoorSender extends BroadcastReceiver {

	public static final String ACTION_OPEN_DOOR = "org.doorbootler.ACTION_OPEN_DOOR";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (!TextUtils.isEmpty(action) && action.equalsIgnoreCase(ACTION_OPEN_DOOR)) {
				NotificationHelper.hideNotification(context);

				boolean doorRequestOverDataNetwork = intent.getBooleanExtra(NotificationHelper.EXTRA_OVER_DATA_NETWORK, false);
				openDoor(context, doorRequestOverDataNetwork);
			}
		}
	}

	private void openDoor(Context ctx, boolean doorRequestOverDataNetwork) {
		// if data connectivity ok for this device and the other device too
		if (doorRequestOverDataNetwork && Utils.getConnection(ctx)) {
			// -> sending the door opening approval by push with NMA
			Intent i = new Intent(NetworkService.ACTION_OPEN_DOOR);
			i.putExtra(NetworkService.EXTRA_APP, "app");
			i.putExtra(NetworkService.EXTRA_EVENT, "event");
			i.putExtra(NetworkService.EXTRA_DESCRIPTION, "description");
			i.putExtra(NetworkService.EXTRA_PRIORITY, 0);
			ctx.startService(i);
		} else {
			// -> sending the door opening approval by sms
			SMSUtils.sendSMS();
		}
	}

}
