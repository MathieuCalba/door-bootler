package org.doorbeller.act.sender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SMSSender extends BroadcastReceiver{

	public static final String ACTION = "org.doorbeller.action.OPEN";
	public static final String SMS_EXTRA_NAME = "doorbeller";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();

		if ( extras != null ) {
			Object[] doorbellerExtra = (Object[]) extras.get( SMS_EXTRA_NAME );
			for ( int i = 0; i < doorbellerExtra.length; ++i ) {
				sendSMS();
			}
		}
	}

	private void sendSMS() {
		Log.i("NOVODA", "Sending SMS");
		// SmsManager smsManager = SmsManager.getDefault();
		// smsManager.sendTextMessage("07981932411", null, "blah blah blah blah", null, null);
	}

}
