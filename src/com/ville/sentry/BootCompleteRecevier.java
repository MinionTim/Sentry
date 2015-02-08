package com.ville.sentry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootCompleteRecevier extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
			Toast.makeText(context, "Sentry Start", Toast.LENGTH_SHORT).show();
		}
	}

}
