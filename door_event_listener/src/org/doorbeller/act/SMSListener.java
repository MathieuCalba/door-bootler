package org.doorbeller.act;

import org.doorbeller.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

public class SMSListener extends BroadcastReceiver {

	public static final String SMS_EXTRA_NAME = "pdus";
	public static final String SMS_URI = "content://sms";

	private int mSoundID;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		String message = "";

		if (extras != null) {
			Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
			for (int i = 0; i < smsExtra.length; ++i) {
				message = extractSMSMessage(message, smsExtra, i);
			}
		}

		if (!TextUtils.isEmpty(message) && message.contains("Ding Dong")) {
			// TODO : get the real picture from MMS to display
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
			NotificationHelper.showNotification(context, bitmap);
			// TODO : we need to store this picture somewhere, so we can display it later

			final SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					soundPool.play(mSoundID, 1, 1, 1, 0, 1f);
				}
			});
			mSoundID = soundPool.load(context, R.raw.old_phone_ringing, 1);

			// TODO : add Vibrations

			this.abortBroadcast();
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