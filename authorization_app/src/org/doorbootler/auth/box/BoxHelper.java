package org.doorbootler.auth.box;

import java.io.File;
import java.io.IOException;

import org.doorbootler.library.box.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.box.androidlib.Box;
import com.box.androidlib.ResponseListeners.FileDownloadListener;

public class BoxHelper {
	protected static final String TAG = BoxHelper.class.getName();

	public void getFile(Context ctx, int fileId, int versionId){

		final SharedPreferences prefs = ctx.getSharedPreferences(Constants.PREFS_FILE_NAME, 0);
		String authToken = prefs.getString(Constants.PREFS_KEY_AUTH_TOKEN, null);


		Box box = Box.getInstance(Constants.API_KEY);
		File f = new File(ctx.getCacheDir(), "door.jpg");
		box.download(authToken, fileId, f, null, new FileDownloadListener() {

			@Override
			public void onIOException(IOException e) {
				Log.v(TAG, e.toString());

			}

			@Override
			public void onProgress(long bytesDownloaded) {
				// no-op

			}

			@Override
			public void onComplete(String status) {
				Log.v(TAG, status);
				// now send f to UI
			}
		});
	}
}
