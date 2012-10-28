package org.doorbootler.android.door;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.content.Intent;
import android.os.Bundle;

public class SimpleDigitalInputActivity extends AbstractIOIOActivity {
	private final int BUTTON1_PIN = 4;
	private final int LED_Notif_Pin = 5;
	private final int Pos_PIN = 3;
	private final int Neg_PIN = 6;

	private final int PWM_FREQ = 100;
	public boolean doorToOpen;
	private long openDoorDuration;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		private DigitalInput mButton1;
		private DigitalOutput LED; // Notification LED on Hardware
		private PwmOutput panPwmOutput;
		private PwmOutput tiltPwmOutput;
		
		@Override
		public void setup() throws ConnectionLostException {
			try {
				mButton1 = ioio_.openDigitalInput(BUTTON1_PIN,
						DigitalInput.Spec.Mode.PULL_UP);
				LED = ioio_.openDigitalOutput(LED_Notif_Pin, true);
			} catch (ConnectionLostException e) {
				throw e;
			}
		}

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
					sleep(10);
					startActivity(new Intent(SimpleDigitalInputActivity.this,
							DoorBellActivity.class));
				} else {
					// When The buzzer is not pressed
					LED.write(false);
				}


				if (openDoorDuration > 0) {
					doDoorOpen();
				}

				sleep(10);
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				throw e;
			}
		}

		private void doDoorOpen() {
			try {
				panPwmOutput.setPulseWidth(2000);
				tiltPwmOutput.setPulseWidth(1000);

				Thread.sleep(openDoorDuration);

				panPwmOutput.setPulseWidth(0);
				tiltPwmOutput.setPulseWidth(0);

				doorToOpen = false;
				openDoorDuration = 0;
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		openDoorDuration = 2000;
	}

}