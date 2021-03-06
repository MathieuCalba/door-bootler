package org.doorbootler.android.door.sender;

public interface Sender {

	void prepare();

	boolean isConnected();

	void onPause();

	void sendImage(byte[] bs);

	boolean isSending();

}
