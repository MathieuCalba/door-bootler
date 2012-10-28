package org.doorbeller.act.receiver;

import org.doorbeller.R;
import org.doorbeller.act.NotificationHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.text.TextUtils;
import android.util.Log;

import com.immersion.uhl.Launcher;

public abstract class DoorRingReceiver extends BroadcastReceiver {

	public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String ACTION_MAN_RECEIVED = "com.usk.app.notifymyandroid.NEW_NOTIFICATION";

	private int mSoundID;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			if (!TextUtils.isEmpty(action)) {
				if (action.equalsIgnoreCase(ACTION_SMS_RECEIVED) || action.equalsIgnoreCase(ACTION_MAN_RECEIVED)) {
					onReceiveEvent(context, intent);
				}
			}
		}
	}

	protected abstract void onReceiveEvent(Context context, Intent intent);

	protected void handleDoorRequest(final Context context, Bitmap bitmap, boolean doorRequestOverDataNetwork) {
		NotificationHelper.showNotification(context, bitmap, doorRequestOverDataNetwork);

		final SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				Launcher launcher = null;
				try {
					launcher = new Launcher(context);
				} catch (RuntimeException e) {
					Log.e("DoorRingReceiver", "Launcher creation impossible", e);
				}

				soundPool.play(mSoundID, 1, 1, 1, 0, 1f);

				try {
					launcher.play(Launcher.LONG_BUZZ_100);
				} catch (RuntimeException e) {
					Log.e("DoorRingReceiver", "Vibration failed", e);
				}
			}
		});
		mSoundID = soundPool.load(context, R.raw.old_phone_ringing, 1);
		
		
	}

}
