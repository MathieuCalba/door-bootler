package org.boorbeller.library.notifymyandroid;

import android.text.TextUtils;
import android.util.Log;

public class NMAServerException extends Exception {

	private static final long serialVersionUID = -3304971952969578754L;

	private final String mError;

	public NMAServerException(String error) {
		super();
		mError = error;
	}

	public String getError() {
		return mError;
	}

	@Override
	public void printStackTrace() {
		if (!TextUtils.isEmpty(mError)) {
			Log.e("NMAErrorException", mError);
		}
		super.printStackTrace();
	}
}
