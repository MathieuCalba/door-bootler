package org.doorbeller.android.door;

public interface Sender {

	void prepare();

	boolean isConnected();

	void onPause();

	void sendImage(byte[] bs);

}
