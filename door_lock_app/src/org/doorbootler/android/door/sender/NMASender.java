package org.doorbootler.android.door.sender;

import org.doorbootler.library.notifymyandroid.NetworkService;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NMASender implements Sender {

	private final Context mContext;

	private boolean hasSent = false;

	public NMASender(Context ctx) {
		super();
		mContext = ctx;
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendImage(byte[] bs) {
		Log.i("NMASender", "sendImage");
		Intent i = new Intent(NetworkService.ACTION_OPEN_DOOR);
		i.putExtra(NetworkService.EXTRA_APP, "DoorBootler");
		i.putExtra(NetworkService.EXTRA_EVENT, "Ding Dong");
		i.putExtra(NetworkService.EXTRA_PRIORITY, 0);
		mContext.startService(i);
		hasSent = true;
	}

	@Override
	public boolean isSending() {
		return hasSent;
	}

}
