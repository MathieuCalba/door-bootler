package org.doorbeller.android.door;

import java.nio.channels.Channel;

import org.doorbeller.android.PusherConstants;
import org.doorbeller.android.R;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

public class DoorBellActivity extends Activity {

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

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_door);
		
		
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
	protected void onDestroy() {
		super.onDestroy();

	}

	public void onDoorBellClick() {
		connectToPusher();
	}

	public void connectToPusher() {
		
		mSoundPool.play(mSoundID, 1, 1, 1, 0, 1f);
	}

	protected void openDoor() {
		mSoundPool.stop(mSoundID);
		// IO signal to door;
		
	}


}
