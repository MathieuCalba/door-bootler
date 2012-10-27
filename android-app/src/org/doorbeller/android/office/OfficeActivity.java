package org.doorbeller.android.office;

import org.doorbeller.android.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class OfficeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_office);
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
		finish();
	}

	public void closeDoor() {
		finish();
	}

}
