package org.doorbeller.act;


import org.boorbeller.library.box.Constants;
import org.boorbeller.library.notifymyandroid.NMASender;
import org.doorbeller.R;

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
