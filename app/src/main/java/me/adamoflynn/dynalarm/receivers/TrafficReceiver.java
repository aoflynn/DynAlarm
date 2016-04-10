package me.adamoflynn.dynalarm.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by Adam on 04/04/2016.
 */
public class TrafficReceiver extends ResultReceiver {

	private Receiver mReceiver;

	public interface Receiver {
		void onReceiveResult(int statusCode, Bundle resultData);
	}

	public TrafficReceiver(Handler handler) {
		super(handler);
	}

	public void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}

	@Override
	protected void onReceiveResult(int statusCode, Bundle resultData){
		if(mReceiver != null){
			mReceiver.onReceiveResult(statusCode, resultData);
		}
	}


}
