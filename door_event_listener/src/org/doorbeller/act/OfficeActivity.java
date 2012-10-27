package org.doorbeller.act;

import org.doorbeller.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

public class OfficeActivity extends Activity {

	public static final String EXTRA_PICTURE_PATH = "org.doorbeller.act.EXTRA_PICTURE_PATH";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_office);

		final Intent i = getIntent();
		if (i != null) {
			String uri = i.getStringExtra(EXTRA_PICTURE_PATH);
			if (!TextUtils.isEmpty(uri)) {
				Bitmap bitmap = BitmapFactory.decodeFile(uri);
				if (bitmap != null) {
					ImageView imageV = (ImageView) findViewById(R.id.door_picture);
					imageV.setImageBitmap(bitmap);
				}
			}
		}
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
		sendBroadcast(new Intent(OpeningDoor.ACTION));
		finish();
	}

	public void closeDoor() {
		finish();
	}

}
