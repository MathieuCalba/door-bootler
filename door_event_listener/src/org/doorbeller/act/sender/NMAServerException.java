package org.doorbeller.act.sender;

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

}
