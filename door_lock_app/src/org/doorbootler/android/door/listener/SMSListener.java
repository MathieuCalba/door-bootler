package org.doorbootler.android.door.listener;

import org.doorbootler.android.door.events.OpenDoorAuthorizedEvent;
import org.doorbootler.library.sms.SMSUtils;

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