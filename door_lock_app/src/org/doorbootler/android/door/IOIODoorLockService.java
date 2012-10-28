package org.doorbootler.android.door;

import org.doorbootler.android.door.events.OpenDoorRequest;

import de.greenrobot.event.EventBus;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.content.Intent;
import android.os.IBinder;

public class IOIODoorLockService extends IOIOService {

	public static final String EXTRA_DOOR_LOCK_VALUE = "door_lock_value";

	public IOIODoorLockService() {
		super();
	}

	private final int PAN_PIN = 3;
	private final int BUTTON1_PIN = 4;
	private final int LED_Notif_Pin = 5;

	private final int PWM_FREQ = 100;
	public long openDoorDuration;

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new DoorLockLooper();
	}

	class DoorLockLooper extends BaseIOIOLooper {
		private DigitalInput mButton1;
		private DigitalOutput LED; // Notification LED on Hardware
		private PwmOutput panPWMOutput;
		private PwmOutput tiltPWMOutput;
		private boolean doorToOpen;

		@Override
		public void loop() throws ConnectionLostException {
			try {
				final boolean reading1 = mButton1.read();

				if (!reading1) {
					// Physical Debugging Notification LED on Hardware
					LED.write(true); // For physical debugging
					EventBus.getDefault().post(new OpenDoorRequest());
					// The button pressed event is here, i.e. the buzzer has
					// been activated
					// Phone will turn on here
					Thread.sleep(10);

				} else {
					// When The buzzer is not pressed
					LED.write(false);
				}

				if (openDoorDuration > 0) {
					doDoorOpen();
				}

				Thread.sleep(10);
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				throw e;
			}
		}

		private void doDoorOpen() {
			try {
				panPWMOutput.setPulseWidth(2000);
				tiltPWMOutput.setPulseWidth(1000);
				
				Thread.sleep(openDoorDuration);
				
				panPWMOutput.setPulseWidth(0);
				tiltPWMOutput.setPulseWidth(0);
				
				doorToOpen = false;
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void setup() throws ConnectionLostException,
				InterruptedException {
			try {
				mButton1 = ioio_.openDigitalInput(BUTTON1_PIN,
						DigitalInput.Spec.Mode.PULL_UP);
				LED = ioio_.openDigitalOutput(LED_Notif_Pin, true);

				panPWMOutput = ioio_.openPwmOutput(new DigitalOutput.Spec(
						PAN_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
				tiltPWMOutput = ioio_.openPwmOutput(new DigitalOutput.Spec(
						PAN_PIN, Mode.OPEN_DRAIN), PWM_FREQ);
			} catch (ConnectionLostException e) {
				throw e;
			}

		}

	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		openDoorDuration = intent.getIntExtra(EXTRA_DOOR_LOCK_VALUE, 0);		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}