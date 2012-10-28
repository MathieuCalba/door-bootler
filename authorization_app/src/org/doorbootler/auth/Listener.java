package org.doorbootler.auth;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.doorbootler.library.notifymyandroid.Constants;
import org.doorbootler.library.notifymyandroid.NMASender;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.box.androidlib.Box;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.ResponseListeners.FileUploadListener;

public class Listener extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_listener_test);
	}

	public void onBtClick(View v) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					NMASender.verify(Constants.API_KEY);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
	}

	public void onBtBisClick(View v) {
		byte[] byte_img_data = null;
		try {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
			byte_img_data = baos.toByteArray();
			baos.flush();
			baos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		final SharedPreferences prefs = getSharedPreferences(org.doorbootler.library.box.Constants.PREFS_FILE_NAME, 0);
		String authToken = prefs.getString(org.doorbootler.library.box.Constants.PREFS_KEY_AUTH_TOKEN, null);

		long folderId = 0l; // root folder
		ByteArrayInputStream inputStream = new ByteArrayInputStream(byte_img_data);

		final Box box = Box.getInstance(Constants.API_KEY);
		box.upload(authToken, Box.UPLOAD_ACTION_UPLOAD, inputStream, "door.jpg", folderId, new FileUploadListener() {

			@Override
			public void onIOException(IOException e) {
				Log.v("Listener", e.toString());

			}

			@Override
			public void onProgress(long bytesTransferredCumulative) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMalformedURLException(MalformedURLException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete(BoxFile boxFile, String status) {
				Log.v("Listener", status);

				boxFile.getId();
			}
		});

	}

}
