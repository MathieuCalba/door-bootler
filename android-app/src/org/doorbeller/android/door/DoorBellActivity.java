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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;
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

	private SoundPool mSoundPool;

	protected boolean mLoaded;

	private int mSoundID;

	private SmsManager mSmsManager;

	public NetworkInfo mNetworkInfo;

	public NetworkInfo mOtherNetworkInfo;

	public boolean mSending;

	private Camera mCamera;

	private SurfaceHolder mPreviewHolder;

	private boolean mInPreview;

	private boolean mCameraConfigured;

	private boolean mPendingPreview;

	private boolean mPreviewConfigured;

	private int mPreviewWidth;

	private int mPreviewHeight;

	private ImageView mPicture;

	private Sender sender = new MMSSender();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_door);

		mPicture = (ImageView) findViewById(R.id.picture);
		mLocalViewSurface = (SurfaceView) findViewById(R.id.video_local);

		mPreviewHolder = mLocalViewSurface.getHolder();
		mPreviewHolder.addCallback(this);
		mPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

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
		mSoundPool.setLoop(mSoundID, -1);
	}

	@Override
	protected void onResume() {
		super.onResume();

		EventBus.getDefault().register(this, OpenDoorEvent.class);
	}

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		unsetSoundAndVideo();
		
		sender.onPause();
		
		super.onPause();

	}	

	public void onEvent(OpenDoorEvent e) {
		openDoor();
	}

	public void onDoorBellClick(View target) {
		sender.prepare();

		try {
			mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);

			if (mPreviewConfigured) {
				initPreview(mPreviewWidth, mPreviewHeight);
				startPreview();
			} else {
				mPendingPreview = true;
			}
		} catch (RuntimeException e) {
			Log.v(TAG, "Camera failed", e);
		}

		mSoundPool.play(mSoundID, 1, 1, 1, 0, 1f);

	}


	public void onPreviewFrame(byte[] data, Camera camera) {
		if (sender.isConnected() && !mSending) {
			Log.d(TAG, "preview image while connected");

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
				
				
				byte[] bs = outStream.toByteArray();
				Bitmap bm = BitmapFactory.decodeByteArray(bs, 0, bs.length);
				mPicture.setBackgroundDrawable(new BitmapDrawable(bm));
				sender.sendImage(bs);
			}
		} else {
			// Log.d(TAG, "preview image while disconnected");
		}
	}

	private boolean isConnectedToAPN() {
		return mNetworkInfo != null && mNetworkInfo.isConnected();
	}


	protected void openDoor() {
		Log.v(TAG, "opening door");
		unsetSoundAndVideo();

		// IO signal to door;

	}

	private void unsetSoundAndVideo() {

		if (mSoundPool != null) {
			mSoundPool.stop(mSoundID);
		}

		if (mCamera != null) {
			if (mInPreview) {
				mCamera.stopPreview();
			}
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}

		mInPreview = false;

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
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();
			mInPreview = true;
			mPendingPreview = false;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.v(TAG, "init camera preview");
		initPreview(width, height);
		mPreviewWidth = width;
		mPreviewHeight = height;
		mPreviewConfigured = true;

		if (mPendingPreview) {
			startPreview();
		}
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
