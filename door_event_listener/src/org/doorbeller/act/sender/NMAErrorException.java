package org.doorbeller.act.sender;

public class NMAErrorException extends Exception {

	private static final long serialVersionUID = 6785888098241301524L;

	private String mError;
	private Exception mOriginalException;

	public NMAErrorException(String error) {
		super(error, null);
	}

	public NMAErrorException(Exception e) {
		this(null, e);
	}

	public NMAErrorException(String error, Exception e) {
		super();
		mError = error;
		mOriginalException = e;
	}

	public String getError() {
		return mError;
	}

	public Exception getOriginalException() {
		return mOriginalException;
	}

}
