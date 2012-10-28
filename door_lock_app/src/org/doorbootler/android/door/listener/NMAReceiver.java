package org.doorbootler.android.door.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

public class NMAReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();

		if (extras != null) {
			Uri data = intent.getData();
			String title = intent.getStringExtra("title");
			String event = intent.getStringExtra("event");
			String desc = intent.getStringExtra("desc");
			int priority = intent.getIntExtra("prio", 0);
			String url = intent.getStringExtra("url");

			if (!TextUtils.isEmpty(event) && event.contains("Door Open")) {
				// TODO : should open the door
			}
		}
	}

}
