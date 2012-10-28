package org.doorbootler.auth;


import org.doorbootler.library.notifymyandroid.Constants;
import org.doorbootler.library.notifymyandroid.NMASender;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

public class Listener extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_listener_test);
	}

	public void onBtClick(View v) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					NMASender.verify(Constants.API_KEY);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

}
