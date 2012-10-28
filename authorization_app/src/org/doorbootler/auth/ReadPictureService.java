package org.doorbootler.auth;

import org.doorbootler.auth.receiver.DoorRingReceiver;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ReadPictureService extends IntentService {

	public ReadPictureService() {
		super("ReadPictureService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO : should use the downloaded file
		// Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

		// File f = new File(getCacheDir(), "door.jpg");
		// Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
		DoorRingReceiver.handleDoorRequest(this, bitmap, true);
	}

}
