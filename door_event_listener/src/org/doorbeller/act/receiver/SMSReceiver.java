package org.doorbeller.act.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

public class SMSReceiver extends DoorRingReceiver {

	public static final String SMS_EXTRA_NAME = "pdus";
	public static final String SMS_URI = "content://sms";

	@Override
	public void onReceiveEvent(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		String message = "";

		if (extras != null) {
			Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
			for (int i = 0; i < smsExtra.length; ++i) {
				message = extractSMSMessage(message, smsExtra, i);
			}

			if (!TextUtils.isEmpty(message) && message.contains("Ding Dong")) {
				// With SMS, there is no picture available, so don't give one
				handleDoorRequest(context, null, false);

				abortBroadcast();
			}
		}
	}

	private String extractSMSMessage(String messages, Object[] smsExtra, int i) {
		SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
		String body = sms.getMessageBody().toString();
		String address = sms.getOriginatingAddress();
		messages += "SMS from " + address + " :\n";
		messages += body + "\n";
		Log.i("NOVODA", "FROM:[" + address + "] MSG:[" + body + "]");
		return messages;
	}

}