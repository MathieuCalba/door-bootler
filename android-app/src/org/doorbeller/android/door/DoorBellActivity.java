package org.doorbeller.android.door;

import java.io.IOException;

import jibe.sdk.client.JibeIntents;
import jibe.sdk.client.JibeServiceException;
import jibe.sdk.client.JibeServiceListener;
import jibe.sdk.client.JibeServiceListener.ConnectFailedReason;
import jibe.sdk.client.events.JibeSessionEvent;
import jibe.sdk.client.simple.ChallengeDialog;
import jibe.sdk.client.simple.SimpleApi;
import jibe.sdk.client.simple.SimpleConnectionStateListener;
import jibe.sdk.client.simple.session.JibeBundle;
import jibe.sdk.client.simple.session.JibeBundleTransferConnection;
import jibe.sdk.client.simple.session.JibeBundleTransferConnection.JibeBundleTransferConnectionListener;
import jibe.sdk.client.simple.videocall.VideoCallConnection;
import jibe.sdk.client.simple.videosharing.OneWayVideoSharingConnection;
import jibe.sdk.client.video.CameraMediaSource;
import jibe.sdk.client.videosharing.VideoSharingResult;
import jibe.sdk.client.videosharing.VideoSharingService;

import org.doorbeller.android.R;
import org.doorbeller.android.ViewMeActivity;
import org.doorbeller.android.R.id;
import org.doorbeller.android.R.layout;
import org.doorbeller.android.R.string;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

public class DoorBellActivity extends Activity {

	private static final String TAG = DoorBellActivity.class.getSimpleName();

	public boolean mIsSender;
	private String mChallengeReceiverDisplayName = null;
	private String mChallengeReceiverPhoneNumber = null;
	private String mChallengerDisplayName = null;

	private ProgressDialog mWaitingForReceiverToAcceptDialog = null;
	private ChallengeDialog mIncomingChallengeDialog = null;

	private JibeBundleTransferConnection mBundleConnection = null;
	private OneWayVideoSharingConnection mVideoConnection = null;

	public boolean mIsNetworkFailure = false;
	public boolean mGameStart = false;

	private SurfaceView mLocalViewSurface;
	private CameraMediaSource mCameraMediaSource;

	protected boolean mConnected;

	protected SimpleApi mInitialized;

	private SimpleConnectionStateListener mVideoConnStateListener = new SimpleConnectionStateListener() {
		
		@Override
		public void onInitialized(SimpleApi source) {
			mInitialized = source;
			
		}
		
		@Override
		public void onInitializationFailed(SimpleApi source, ConnectFailedReason reasons) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onTerminated(SimpleApi source, int info) {
			mWaitingForReceiverToAcceptDialog.cancel();
			
		}
		
		@Override
		public void onStarted(SimpleApi source) {
			mWaitingForReceiverToAcceptDialog.cancel();
			
		}
		
		@Override
		public void onStartFailed(SimpleApi source, int info) {
			mWaitingForReceiverToAcceptDialog.cancel();
			
		}
	};

	 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video);

		// Init surfaces and camera
		mLocalViewSurface = (SurfaceView) findViewById(R.id.video_local);
		mLocalViewSurface.setZOrderMediaOverlay(true);
		mCameraMediaSource = new CameraMediaSource(
				CameraMediaSource.CAMERA_BACK, mLocalViewSurface);
		
		mVideoConnection = new OneWayVideoSharingConnection(getApplicationContext(),
				mVideoConnStateListener, mCameraMediaSource);
		mVideoConnection.setAutoAccept(true);
		
	}

	protected void mInitialized() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	public void onDoorBellClick() {
		if (mConnected) {			
			String contactId = getPhoneNumber();
			startVideoSharing(contactId);
		}
	}
	
	
	
	private void startVideoSharing(String phoneNumber) {
		try {
			mVideoConnection.start(phoneNumber);
			showSenderSideDialog();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JibeServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getPhoneNumber() {
		// TODO
		return "";
	}

	
	private void showSenderSideDialog() {
		mWaitingForReceiverToAcceptDialog = new ProgressDialog(
				DoorBellActivity.this);
		if (mChallengeReceiverDisplayName != null) {
			mWaitingForReceiverToAcceptDialog.setMessage(getString(R.string.outgoing_challenge_dialog_message, mChallengeReceiverDisplayName));
		} else {
			mWaitingForReceiverToAcceptDialog.setMessage(getString(R.string.outgoing_challenge_dialog_search_message));
		}
		mWaitingForReceiverToAcceptDialog.setIndeterminate(true);
		mWaitingForReceiverToAcceptDialog.setCancelable(false);		
		mWaitingForReceiverToAcceptDialog.show();
	}
	
	private void showMessage(final String message) {
		if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					showMessage(message);
				}
			});

			return;
		}

		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
