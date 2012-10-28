package net.mitchtech.ioio;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import net.mitchtech.ioio.simpledigitalinput.R;
import android.os.Bundle;
import android.widget.TextView;

public class SimpleDigitalInputActivity extends AbstractIOIOActivity {
	private final int BUTTON1_PIN = 4;
	private final int LED_Notif_Pin = 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		private DigitalInput mButton1;
		private DigitalOutput LED;			//Notification LED on Hardware

		@Override
		public void setup() throws ConnectionLostException {
			try {
				mButton1 = ioio_.openDigitalInput(BUTTON1_PIN, DigitalInput.Spec.Mode.PULL_UP);
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
					//Physical Debugging Notification LED on Hardware
					LED.write(true);	//For physical debugging 
					//The button pressed event is here, i.e. the buzzer has been activated
					//Phone will turn on here
					sleep(10);
				} else {
					//When The buzzer is not pressed
					LED.write(false);	
				}

				sleep(10);
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				throw e;
			}
		}
	}

	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread() {
		return new IOIOThread();
	}
}