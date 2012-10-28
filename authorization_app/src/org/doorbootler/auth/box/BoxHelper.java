package org.doorbootler.auth.box;

import java.io.File;

import org.doorbootler.library.box.Constants;

import android.content.Context;
import android.content.SharedPreferences;

import com.box.androidlib.Box;
import com.box.androidlib.ResponseListeners.FileDownloadListener;

public class BoxHelper {

	protected static final String TAG = BoxHelper.class.getName();

	public static void getFile(Context ctx, long fileId, FileDownloadListener listener) {
		final SharedPreferences prefs = ctx.getSharedPreferences(Constants.PREFS_FILE_NAME, 0);
		String authToken = prefs.getString(Constants.PREFS_KEY_AUTH_TOKEN, null);

		Box box = Box.getInstance(Constants.API_KEY);
		File f = new File(ctx.getCacheDir(), "door.jpg");
		box.download(authToken, fileId, f, null, listener);
	}
}
