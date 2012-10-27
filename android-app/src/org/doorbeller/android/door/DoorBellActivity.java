package org.doorbeller.android.door;

import org.doorbeller.android.PusherConstants;
import org.doorbeller.android.R;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.justinschultz.pusherclient.ChannelListener;
import com.justinschultz.pusherclient.Pusher;
import com.justinschultz.pusherclient.Pusher.Channel;
import com.justinschultz.pusherclient.PusherListener;

public class DoorBellActivity extends Activity implements PusherListener {

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

	private Pusher mPusher;

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
		mPusher = new Pusher(PusherConstants.PUSHER_API_KEY);
		mPusher.setPusherListener(this);
		mPusher.connect();		
		mSoundPool.play(mSoundID, 1, 1, 1, 0, 1f);
	}

	@Override
	public void onConnect(String socketId) {
		mSubscribed = true;
		mSocketId = socketId;
		mChallengeReceiverDisplayName = "Skillsmatter";
		// Private Channel
		channel = mPusher.subscribe(PusherConstants.PUSHER_CHANNEL);
		channel.send("bell-ringing", new JSONObject());

		channel.bind("open-door-request", new ChannelListener() {
			@Override
			public void onMessage(String message) {
				openDoor();
			}
		});		
	}

	protected void openDoor() {
		mSoundPool.stop(mSoundID);
		// IO signal to door;
		
	}

	@Override
	public void onMessage(String message) {
		Log.v(TAG, message);
	}

	@Override
	public void onDisconnect() {
		mSubscribed = false;
	}

}
