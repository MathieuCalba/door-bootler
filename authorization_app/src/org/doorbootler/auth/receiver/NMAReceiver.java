package org.doorbootler.auth.receiver;

import org.doorbeller.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

public class NMAReceiver extends DoorRingReceiver {

	@Override
	public void onReceiveEvent(Context context, Intent intent) {
		Bundle extras = intent.getExtras();

		if (extras != null) {
			Uri data = intent.getData();
			String title = intent.getStringExtra("title");
			String event = intent.getStringExtra("event");
			String desc = intent.getStringExtra("desc");
			int priority = intent.getIntExtra("prio", 0);
			String url = intent.getStringExtra("url");

			// if (!TextUtils.isEmpty(message) && message.contains("Ding Dong")) {
			// TODO : download the picture with the Box API
			// TODO : and store it locally
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
			handleDoorRequest(context, bitmap, true);

			abortBroadcast();
			// }
		}
	}

}
