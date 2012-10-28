package org.doorbootler.library.notifymyandroid;


import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

public class NetworkService extends IntentService {

	public static final String ACTION_OPEN_DOOR = "org.doorbootler.auth.ACTION_OPEN_DOOR";

	public static final String EXTRA_APP = "org.doorbootler.auth.EXTRA_APP";
	public static final String EXTRA_EVENT = "org.doorbootler.auth.EXTRA_EVENT";
	public static final String EXTRA_DESCRIPTION = "org.doorbootler.auth.EXTRA_DESCRIPTION";
	public static final String EXTRA_PRIORITY = "org.doorbootler.auth.EXTRA_PRIORITY";

	public NetworkService() {
		super("NetworkService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i("NetworkService", "onHandleIntent()");
		if (intent != null) {
			String action = intent.getAction();
			if (!TextUtils.isEmpty(action)) {
				if (action.equalsIgnoreCase(ACTION_OPEN_DOOR)) {
					String app = intent.getStringExtra(EXTRA_APP);
					String event = intent.getStringExtra(EXTRA_EVENT);
					String description = intent.getStringExtra(EXTRA_DESCRIPTION);
					int priority = intent.getIntExtra(EXTRA_PRIORITY, 0);

					if (NMASender.notifyPush(app != null ? app : "app", event != null ? event : "event", description != null ? description : "description",
							priority, Constants.API_KEY, null)) {
						Log.i("NetworkService", "notify push succefull");
					} else {
						Log.i("NetworkService", "notify push failed");
					}
				}
			}
		}
	}

}
