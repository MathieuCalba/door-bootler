package org.doorbeller.android.door;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.androidbridge.SendMMS3.APNHelper;
import com.androidbridge.SendMMS3.APNHelper.APN;
import com.androidbridge.SendMMS3.PhoneEx;
import com.androidbridge.nokia.IMMConstants;
import com.androidbridge.nokia.MMContent;
import com.androidbridge.nokia.MMEncoder;
import com.androidbridge.nokia.MMMessage;
import com.androidbridge.nokia.MMResponse;
import com.androidbridge.nokia.MMSender;

public class MMSSender implements Sender {

	private static final String TAG = MMSSender.class.getSimpleName();
	

	public enum State {
		UNKNOWN, CONNECTED, NOT_CONNECTED
	}
	
	private ConnectivityBroadcastReceiver mReceiver;
	private Context ctx;

	private boolean mSending;

	private boolean mListening;

	private ConnectivityManager mConnMgr;

	private WakeLock mWakeLock;

	public State mState;

	public NetworkInfo mNetworkInfo;

	public NetworkInfo mOtherNetworkInfo;
	
	@Override
	public void prepare() {
		connectToAPN();		
	}

	

	private void connectToAPN() {
		Log.d(TAG, "1. connect To APN");
		mListening = true;
		mSending = false;
		mConnMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		mReceiver = new ConnectivityBroadcastReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ctx.registerReceiver(mReceiver, filter);

		try {

			// Ask to start the connection to the APN. Pulled from Android
			// source code.
			int result = beginMmsConnectivity();

			if (result != PhoneEx.APN_ALREADY_ACTIVE) {
				Log.v(TAG, "Extending MMS connectivity returned " + result
						+ " instead of APN_ALREADY_ACTIVE");
				// Just wait for connectivity startup without
				// any new request of APN switch.
				return;
			}

		} catch (IOException e) {
			// connection failed
			e.printStackTrace();
		}
	}

	protected void endMmsConnectivity() {
		// End the connectivity
		try {
			Log.v(TAG, "4. endMmsConnectivity");
			if (mConnMgr != null) {
				mConnMgr.stopUsingNetworkFeature(
						ConnectivityManager.TYPE_MOBILE,
						PhoneEx.FEATURE_ENABLE_MMS);
			}
		} finally {
			releaseWakeLock();
		}
	}

	protected int beginMmsConnectivity() throws IOException {
		// Take a wake lock so we don't fall asleep before the message is
		// downloaded.
		createWakeLock();

		int result = mConnMgr.startUsingNetworkFeature(
				ConnectivityManager.TYPE_MOBILE, PhoneEx.FEATURE_ENABLE_MMS);

		Log.v(TAG, "beginMmsConnectivity: result=" + result);

		switch (result) {
		case PhoneEx.APN_ALREADY_ACTIVE:
		case PhoneEx.APN_REQUEST_STARTED:
			acquireWakeLock();
			return result;
		}

		throw new IOException("Cannot establish MMS connectivity");
	}

	private synchronized void createWakeLock() {
		// Create a new wake lock if we haven't made one yet.
		if (mWakeLock == null) {
			PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"MMS Connectivity");
			mWakeLock.setReferenceCounted(false);
		}
	}

	private void acquireWakeLock() {
		// It's okay to double-acquire this because we are not using it
		// in reference-counted mode.
		mWakeLock.acquire();
	}

	private void releaseWakeLock() {
		// Don't release the wake lock if it hasn't been created and acquired.
		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}
	
	

	private void sendMMSUsingNokiaAPI(byte[] bs) {
		// Magic happens here.
		Log.v(TAG, "3. sending MMS");
		MMMessage mm = new MMMessage();
		setMessage(mm);
		addContents(mm, bs);

		MMEncoder encoder = new MMEncoder();
		encoder.setMessage(mm);

		try {
			encoder.encodeMessage();
			byte[] out = encoder.getMessage();

			MMSender sender = new MMSender();
			APNHelper apnHelper = new APNHelper(ctx);
			List<APN> results = apnHelper.getMMSApns();

			if (results.size() > 0) {

				final String MMSCenterUrl = results.get(0).MMSCenterUrl;
				final String MMSProxy = results.get(0).MMSProxy;
				final int MMSPort = Integer.valueOf(results.get(0).MMSPort);
				final Boolean isProxySet = (MMSProxy != null)
						&& (MMSProxy.trim().length() != 0);

				sender.setMMSCURL(MMSCenterUrl);
				sender.addHeader("X-NOKIA-MMSC-Charging", "100");

				MMResponse mmResponse = sender.send(out, isProxySet, MMSProxy,
						MMSPort);
				Log.d(TAG, "Message sent to " + sender.getMMSCURL());
				Log.d(TAG, "Response code: " + mmResponse.getResponseCode()
						+ " " + mmResponse.getResponseMessage());

				Enumeration keys = mmResponse.getHeadersList();
				while (keys.hasMoreElements()) {
					String key = (String) keys.nextElement();
					String value = (String) mmResponse.getHeaderValue(key);
					Log.d(TAG, (key + ": " + value));
				}

				if (mmResponse.getResponseCode() == 200) {
					// 200 Successful, disconnect and reset.
					endMmsConnectivity();
					mSending = false;
					mListening = false;
				} else {
					// kill dew :D hhaha
				}
			} else {
				Log.v(TAG, "No MMS APNs configured");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void trySendMMs(byte[] bs) {

		// Check availability of the mobile network.
		if ((mNetworkInfo == null)
				|| (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE_MMS)) {
			Log.v(TAG, "   type is not TYPE_MOBILE_MMS, bail");
			return;
		}

		if (!mNetworkInfo.isConnected()) {
			Log.v(TAG, "   TYPE_MOBILE_MMS not connected, bail");
			return;
		} else {
			Log.v(TAG, "connected..");

			if (mSending == false) {
				mSending = true;
				sendMMSUsingNokiaAPI(bs);
			}
		}
	}

	private void setMessage(MMMessage mm) {
		mm.setVersion(IMMConstants.MMS_VERSION_10);
		mm.setMessageType(IMMConstants.MESSAGE_TYPE_M_SEND_REQ);
		mm.setTransactionId("0000000066");
		mm.setDate(new Date(System.currentTimeMillis()));
		mm.setFrom("+66820223268/TYPE=PLMN"); // doesnt work, i wish this worked
												// as it should be
		mm.addToAddress("+66820223268/TYPE=PLMN");
		mm.setDeliveryReport(true);
		mm.setReadReply(false);
		mm.setSenderVisibility(IMMConstants.SENDER_VISIBILITY_SHOW);
		mm.setSubject("This is a nice message!!");
		mm.setMessageClass(IMMConstants.MESSAGE_CLASS_PERSONAL);
		mm.setPriority(IMMConstants.PRIORITY_LOW);
		mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_MIXED);

		// In case of multipart related message and a smil presentation
		// available
		// mm.setContentType(IMMConstants.CT_APPLICATION_MULTIPART_RELATED);
		// mm.setMultipartRelatedType(IMMConstants.CT_APPLICATION_SMIL);
		// mm.setPresentationId("<A0>"); // where <A0> is the id of the content
		// containing the SMIL presentation

	}

	private void addContents(MMMessage mm, byte[] bs) {

		// Adds text content
		MMContent part1 = new MMContent();
		part1.setContent(bs, 0, bs.length);
		part1.setContentId("<0>");
		part1.setType(IMMConstants.CT_IMAGE_JPEG);
		mm.addContent(part1);

	}

	private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)
					|| mListening == false) {
				Log.w(TAG, "onReceived() called with " + mState.toString()
						+ " and " + intent);
				return;
			}

			Log.d(TAG, "2a. connection changed");
			boolean noConnectivity = intent.getBooleanExtra(
					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

			// mReason =
			// intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
			// mIsFailover =
			// intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER,
			// false);

			if (noConnectivity) {
				mState = State.NOT_CONNECTED;
				mNetworkInfo = null;
				mOtherNetworkInfo = null;
			} else {
				mState = State.CONNECTED;
				mNetworkInfo = (NetworkInfo) intent
						.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				mOtherNetworkInfo = (NetworkInfo) intent
						.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

			}
			Log.d(TAG, "2b. connection changed : " + mState.name());
		}
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void onPause() {

		if (mReceiver != null) {
			ctx.unregisterReceiver(mReceiver);
		}

		
	}


	@Override
	public void sendImage(byte[] bs) {
		trySendMMs(bs);
		
	};
}
