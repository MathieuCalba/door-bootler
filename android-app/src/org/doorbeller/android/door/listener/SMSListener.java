package org.doorbeller.android.door.listener;

import org.boorbeller.library.sms.SMSUtils;
import org.doorbeller.android.door.events.OpenDoorAuthorizedEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import de.greenrobot.event.EventBus;

public class SMSListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String messages = SMSUtils.getMessages(intent);
		if (!TextUtils.isEmpty(messages) && messages.contains("Open Door")) {
			EventBus.getDefault().post(new OpenDoorAuthorizedEvent());

			abortBroadcast();
		}
	}

}