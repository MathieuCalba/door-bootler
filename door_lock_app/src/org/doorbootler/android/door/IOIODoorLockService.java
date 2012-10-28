package org.doorbootler.android.door;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class IOIODoorLockService extends IOIOService {
	
	public static final String EXTRA_DOOR_LOCK_VALUE = "door_lock_value";

	public IOIODoorLockService() {
		super();		
	}

	private final int BUTTON1_PIN = 4;
	private final int LED_Notif_Pin = 5;

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new DoorLockLooper();
	}
	
	class DoorLockLooper extends BaseIOIOLooper {
		private DigitalInput mButton1;
		private DigitalOutput LED; // Notification LED on Hardware		

		@Override
		public void loop() throws ConnectionLostException {
			try {
				final boolean reading1 = mButton1.read();

				if (!reading1) {
					// Physical Debugging Notification LED on Hardware
					LED.write(true); // For physical debugging
					// The button pressed event is here, i.e. the buzzer has
					// been activated
					// Phone will turn on here
					Thread.sleep(10);
				} else {
					// When The buzzer is not pressed
					LED.write(false);
				}

				Thread.sleep(10);
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				throw e;
			}
		}

		@Override
		public void setup() throws ConnectionLostException,
				InterruptedException {
			try {
				mButton1 = ioio_.openDigitalInput(BUTTON1_PIN,
						DigitalInput.Spec.Mode.PULL_UP);
				LED = ioio_.openDigitalOutput(LED_Notif_Pin, true);
			} catch (ConnectionLostException e) {
				throw e;
			}
			
		}
		
	}

	@Override
	public void onStart(Intent intent, int startId) {		
		super.onStart(intent, startId);			
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {		
		return null;
	}	
	
}