package org.doorbeller.act;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSListener extends BroadcastReceiver{

    private SharedPreferences preferences;
    
	public static final String SMS_EXTRA_NAME = "pdus";
	public static final String SMS_URI = "content://sms";

    @Override
	public void onReceive( Context context, Intent intent ) {
        Bundle extras = intent.getExtras();
        String messages = "";
        
        if ( extras != null ) {
            Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );
            
            for ( int i = 0; i < smsExtra.length; ++i ) {
            	SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
            	String body = sms.getMessageBody().toString();
            	String address = sms.getOriginatingAddress();
                messages += "SMS from " + address + " :\n";                    
                messages += body + "\n";
                Log.i("NOVODA", "FROM:["+address+"] MSG:["+body+"]");
            }
            
            Toast.makeText( context, messages, Toast.LENGTH_SHORT ).show();
        }
        
        // WARNING!!! 
        // If you uncomment next line then received SMS will not be put to incoming.
        // Be careful!
        // this.abortBroadcast(); 
	}
}