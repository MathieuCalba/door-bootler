package org.doorbootler.auth;

import java.io.File;
import java.lang.ref.WeakReference;

import org.doorbootler.auth.sender.OpeningDoorSender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class OfficeActivity extends Activity {

	public static final String EXTRA_PICTURE_PATH = "org.doorbootler.auth.EXTRA_PICTURE_PATH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_office);

		// new LoadBitmapTask(getApplicationContext(), (ImageView) findViewById(R.id.door_picture));
	}

	public void onClickDontOpen(View v) {
		NotificationHelper.hideNotification(this);
		closeDoor();
	}

	public void onClickOpen(View v) {
		NotificationHelper.hideNotification(this);
		openDoor();
	}

	public void openDoor() {
		boolean doorRequestOnDataNetwork = getIntent().getBooleanExtra(NotificationHelper.EXTRA_OVER_DATA_NETWORK, false);

		Intent i = new Intent(OpeningDoorSender.ACTION_OPEN_DOOR);
		i.putExtra(NotificationHelper.EXTRA_OVER_DATA_NETWORK, doorRequestOnDataNetwork);
		sendBroadcast(i);

		finish();
	}

	public void closeDoor() {
		finish();
	}

	private static class LoadBitmapTask extends AsyncTask<Void, Void, Bitmap> {

		WeakReference<ImageView> mImgRef;
		WeakReference<Context> mContextRef;

		public LoadBitmapTask(Context ctx, ImageView img) {
			super();
			mContextRef = new WeakReference<Context>(ctx);
			mImgRef = new WeakReference<ImageView>(img);
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap = null;
			Context ctx = mContextRef.get();
			if (ctx != null) {
				File f = new File(ctx.getCacheDir(), "door.jpg");
				bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);

			if (result != null) {
				ImageView img = mImgRef.get();
				if (img != null) {
					img.setImageBitmap(result);
				}
			}
		}
	}

}
