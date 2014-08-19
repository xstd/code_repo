package com.android.abutton;

import com.google.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (!Utils.isServiceStarted(context, context.getPackageName())) {
			Utils.startServiceIntent(context);
		}
	}

}
