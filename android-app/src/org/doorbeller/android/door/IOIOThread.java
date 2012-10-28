package net.mitchtech.ioio;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;

class IOIOThread extends AbstractIOIOActivity.IOIOThread {
		/**
		 * 
		 */
		private final BuzzerHardwareListner buzzerHardwareThread;


		/**
		 * @param buzzerHardwareListner
		 */
		IOIOThread(BuzzerHardwareListner buzzerHardwareListner) {
			buzzerHardwareThread = buzzerHardwareListner;
		}

		private DigitalInput Buzzer;
		private PwmOutput PWMOut;
		private DigitalOutput LED;


		@Override
		public void setup() throws ConnectionLostException {
			try {
				Buzzer = ioio_.openDigitalInput(buzzerHardwareThread.BUTTON1_PIN, DigitalInput.Spec.Mode.PULL_UP);
				PWMOut = ioio_.openPwmOutput(new DigitalOutput.Spec(buzzerHardwareThread.Door_Open_Pin, Mode.OPEN_DRAIN), buzzerHardwareThread.PWM_FREQ);
				LED = ioio_.openDigitalOutput(buzzerHardwareThread.LED_Notif_Pin, true);
				
			} catch (ConnectionLostException e) {
				throw e;
			}
		}
	
		@Override
		public void loop() throws ConnectionLostException {
			try {
				final boolean reading1 = Buzzer.read();

				if (!reading1) {
					LED.write(true);	//For physical debugging	
					//This is where the App Does stuff because the buzzer has now been pressed!!
//					button1txt = getString(R.string.button1) + " active!";
//					PWMOut.setPulseWidth(1500 * 2);
//					sleep(10);
//					PWMOut.setPulseWidth(1000 * 2);
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