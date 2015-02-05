package com.ville.sentry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NetworkChangedReceiver extends BroadcastReceiver {
	private static final String TAG = "Ville";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		boolean isConntected = Utility.isConnected(context);
		Toast.makeText(context, "isConntected ? " + isConntected, 1).show();
		AppLog.d(TAG, "[NetworkChangedReceiver.onReceiver] isConntected ? " + isConntected);
	}

}
