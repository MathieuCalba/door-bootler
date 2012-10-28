package org.boorbeller.library.sms;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSUtils {

	public static final String SMS_EXTRA_NAME = "pdus";
	public static final String SMS_URI = "content://sms";

	public static String extractSMSMessage(String messages, Object[] smsExtra, int i) {
		SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);
		String body = sms.getMessageBody().toString();
		String address = sms.getOriginatingAddress();
		messages += "SMS from " + address + " :\n";
		messages += body + "\n";
		Log.v("NOVODA", "FROM:[" + address + "] MSG:[" + body + "]");
		return messages;
	}

	public static String getMessages(Intent intent) {
		Bundle extras = intent.getExtras();
		String messages = "";
		if (extras != null) {
			Object[] smsExtra = (Object[]) extras.get(SMSUtils.SMS_EXTRA_NAME);
			for (int i = 0; i < smsExtra.length; ++i) {
				messages = SMSUtils.extractSMSMessage(messages, smsExtra, i);
			}
		}

		return messages;
	}

	public static void sendSMS() {
		Log.v("NOVODA", "Sending SMS");
		// SmsManager smsManager = SmsManager.getDefault();
		// smsManager.sendTextMessage("07981932411", null, "blah blah blah blah", null, null);
	}

}
