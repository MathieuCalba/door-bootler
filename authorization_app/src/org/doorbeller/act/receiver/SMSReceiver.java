package org.doorbeller.act.receiver;

import org.boorbeller.library.sms.SMSUtils;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class SMSReceiver extends DoorRingReceiver {

	@Override
	public void onReceiveEvent(Context context, Intent intent) {
		String messages = SMSUtils.getMessages(intent);
		if (!TextUtils.isEmpty(messages) && messages.contains("Ding Dong")) {
			// With SMS, there is no picture available, so don't give one
			handleDoorRequest(context, null, false);

			abortBroadcast();
		}
	}

}