package org.doorbeller.android;

import jibe.sdk.client.apptoapp.Config;
import android.app.Application;

public class JibeApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// set up App-ID and App-Secret in one central place.
		Config.getInstance().setAppToAppIdentifier("83f36202e79f4a8191a1b3fd06b4019b", "9c777547307c42f78def6920bb1b4a8d");
	}
}
