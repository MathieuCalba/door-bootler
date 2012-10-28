package org.doorbeller.android.door.sender;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.boorbeller.library.box.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.box.androidlib.Box;
import com.box.androidlib.DAO.BoxFile;
import com.box.androidlib.ResponseListeners.FileUploadListener;

public class BoxSender implements Sender {

	protected static final String TAG = BoxSender.class.getName();
	private final Context ctx;

	public BoxSender(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public void prepare() {
		// no-op

	}

	@Override
	public boolean isConnected() {
		// TODO check connectivity
		return true;
	}

	@Override
	public void onPause() {
		// no-op

	}

	@Override
	public void sendImage(byte[] bs) {
		
		
		final SharedPreferences prefs = ctx.getSharedPreferences(Constants.PREFS_FILE_NAME, 0);
        String authToken = prefs.getString(Constants.PREFS_KEY_AUTH_TOKEN, null);
        
        long folderId = 0l; // root folder
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bs);
        
		final Box box = Box.getInstance(Constants.API_KEY);
        box.upload(authToken, Box.UPLOAD_ACTION_UPLOAD, inputStream, "door.jpg", folderId, new FileUploadListener() {
			
			@Override
			public void onIOException(IOException e) {
				Log.v(TAG, e.toString());
				
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
				Log.v(TAG, status);
				
				boxFile.getId();
				
			}
		}); 
		        
	}

	@Override
	public boolean isSending() {
		return false;
	}

}
