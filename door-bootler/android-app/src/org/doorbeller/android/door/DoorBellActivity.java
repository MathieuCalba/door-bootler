package org.doorbeller.android.door;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channel;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.doorbeller.android.door.events.OpenDoorEvent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.androidbridge.SendMMS3.APNHelper;
import com.androidbridge.SendMMS3.APNHelper.APN;
import com.androidbridge.SendMMS3.PhoneEx;
import com.androidbridge.nokia.IMMConstants;
import com.androidbridge.nokia.MMContent;
import com.androidbridge.nokia.MMEncoder;
import com.androidbridge.nokia.MMMessage;
import com.androidbridge.nokia.MMResponse;
import com.androidbridge.nokia.MMSender;

import de.greenrobot.event.EventBus;

public class DoorBellActivity extends Activity implements PreviewCallback,
		Callback {

	private static final String TAG = DoorBellActivity.class.getSimpleName();

	private SurfaceView mLocalViewSurface;

	protected boolean mSubscribed;

	protected String mSocketId;

	Channel channel;

	private ProgressDialog mWaitingForReceiverToAcceptDialog;

	private Object mChallengeReceiverDisplayName;

	private SoundPool mSoundPool;

	protected boolean mLoaded;

	private int mSoundID;

	private SmsManager mSmsManager;

	public NetworkInfo mNetworkInfo;

	public NetworkInfo mOtherNetworkInfo;

	public boolean mSending;

	private WakeLock mWakeLock;

	private ConnectivityManager mConnMgr;

	private boolean mListening;

	private ConnectivityBroadcastReceiver mReceiver;

	public enum State {
		UNKNOWN, CONNECTED, NOT_CONNECTED
	}

	private State mState;

	private Camera mCamera;

	private SurfaceHolder mPreviewHolder;

	private boolean mInPreview;

	private boolean mCameraConfigured;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_door);

		mLocalViewSurface = (SurfaceView) findViewById(R.id.video_local);
		
		// Set the hardware buttons to control the music
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// Load the sound
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				mLoaded = true;
			}
		});
		mSoundID = mSoundPool.load(this, R.raw.old_phone_ringing, 1);

	}

	@Override
	protected void onResume() {	
		super.onResume();
		
		EventBus.getDefault().register(this, OpenDoorEvent.class);
	}
	
	@Override
	protected void onPause() {

		mSoundPool.stop(mSoundID);

		if (mInPreview) {
			mCamera.stopPreview();
		}

		mCamera.release();
		mCamera = null;
		mInPreview = false;

		super.onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	
	public void onEvent(OpenDoorEvent e){
		openDoor();
	}	
	
	public void onDoorBellClick(View target) {
		connectToAPN();

		mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);	
		
		mPreviewHolder = mLocalViewSurface.getHolder();
		mPreviewHolder.addCallback(this);
		mPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);					
		
		mLocalViewSurface.invalidate();
		
		mSoundPool.play(mSoundID, 1, 1, 1, 0, 1f);

	}

	private void connectToAPN() {
		Log.d(TAG, "1. connect To APN");
		mListening = true;
		mSending = false;
		mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mReceiver = new ConnectivityBroadcastReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, filter);

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void endMmsConnectivity() {
		// End the connectivity
		try {
			Log.v(TAG, "endMmsConnectivity");
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
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
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

	public void onPreviewFrame(byte[] data, Camera camera) {
		if (isConnectedToAPN()) {
			Log.d(TAG, "2. connection changed");

			Parameters parameters = camera.getParameters();
			int imageFormat = parameters.getPreviewFormat();

			if (imageFormat == ImageFormat.NV21) {
				Size previewSize = parameters.getPreviewSize();

				Rect rect = new Rect(0, 0, previewSize.width,
						previewSize.height);
				YuvImage img = new YuvImage(data, ImageFormat.NV21,
						previewSize.width, previewSize.height, null);

				ByteArrayOutputStream outStream = null;

				try {
					outStream = new ByteArrayOutputStream();
					img.compressToJpeg(rect, 100, outStream);
					outStream.flush();
					outStream.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				trySendMMs(outStream.toByteArray());
			}
		}
	}

	private boolean isConnectedToAPN() {
		return mNetworkInfo != null && mNetworkInfo.isConnected();
	}

	private void sendMMSUsingNokiaAPI(byte[] bs) {
		// Magic happens here.

		MMMessage mm = new MMMessage();
		setMessage(mm);
		addContents(mm, bs);

		MMEncoder encoder = new MMEncoder();
		encoder.setMessage(mm);

		try {
			encoder.encodeMessage();
			byte[] out = encoder.getMessage();

			MMSender sender = new MMSender();
			APNHelper apnHelper = new APNHelper(this);
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

			Log.d(TAG, "2. connection changed");
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

		}
	};

	protected void openDoor() {
		mSoundPool.stop(mSoundID);
		// IO signal to door;

	}

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}

	private void initPreview(int width, int height) {
		if (mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				mCamera.setPreviewDisplay(mPreviewHolder);
			} catch (Throwable t) {
				Log.e(TAG, t.getMessage());
				Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			if (!mCameraConfigured) {
				Camera.Parameters parameters = mCamera.getParameters();
				Camera.Size size = getBestPreviewSize(width, height, parameters);

				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);
					mCamera.setParameters(parameters);
					mCameraConfigured = true;
				}
			}
		}
	}

	private void startPreview() {
		if (mCameraConfigured && mCamera != null) {
			mCamera.startPreview();
			mInPreview = true;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.v(TAG, "init camera preview");
		initPreview(width, height);		
		startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// no-op

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// no-op
	}

}
