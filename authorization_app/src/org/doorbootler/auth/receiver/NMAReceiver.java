package org.doorbootler.auth.receiver;

import java.io.IOException;

import org.doorbootler.auth.R;
import org.doorbootler.auth.box.BoxHelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.box.androidlib.ResponseListeners.FileDownloadListener;

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

			if (!TextUtils.isEmpty(event) && event.contains("Ding Dong")) {
				try {
					long fileId = Long.valueOf(desc);
					BoxHelper.getFile(context.getApplicationContext(), fileId, new FileListener(context.getApplicationContext()));
				} catch (NumberFormatException e) {
					Log.e("NMAReceiver", "couldn't get the box id, so use a fake picture", e);
					Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
					DoorRingReceiver.handleDoorRequest(context, bitmap, true);
				}
			}
		}
	}

	private static class FileListener implements FileDownloadListener {

		private final Context mContext;

		public FileListener(Context ctx) {
			super();
			mContext = ctx;
		}

		@Override
		public void onIOException(IOException e) {
			DoorRingReceiver.handleDoorRequest(mContext, null, true);
		}

		@Override
		public void onProgress(long bytesDownloaded) {
		}

		@Override
		public void onComplete(String status) {
			// TODO : should use the downloaded file
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher);
			DoorRingReceiver.handleDoorRequest(mContext, bitmap, true);
		}
	}

}
