package org.doorbootler.auth;

import org.doorbootler.library.notifymyandroid.Constants;
import org.doorbootler.library.notifymyandroid.NMASender;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

public class NetworkService extends IntentService {

	public static final String ACTION_OPEN_DOOR = "org.doorbeller.act.ACTION_OPEN_DOOR";

	public static final String EXTRA_APP = "org.doorbeller.act.EXTRA_APP";
	public static final String EXTRA_EVENT = "org.doorbeller.act.EXTRA_EVENT";
	public static final String EXTRA_DESCRIPTION = "org.doorbeller.act.EXTRA_DESCRIPTION";
	public static final String EXTRA_PRIORITY = "org.doorbeller.act.EXTRA_PRIORITY";

	public NetworkService() {
		super("NetworkService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			if (!TextUtils.isEmpty(action)) {
				if (action.equalsIgnoreCase(ACTION_OPEN_DOOR)) {
					String app = intent.getStringExtra(EXTRA_APP);
					String event = intent.getStringExtra(EXTRA_EVENT);
					String description = intent.getStringExtra(EXTRA_DESCRIPTION);
					int priority = intent.getIntExtra(EXTRA_PRIORITY, 0);

					if (NMASender.notifyPush(app, event, description, priority, Constants.API_KEY, null)) {
					} else {
					}
				}
			}
		}
	}

}
