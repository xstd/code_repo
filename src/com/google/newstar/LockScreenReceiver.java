package com.google.newstar;

import com.google.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class LockScreenReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		//隐藏图标
		/*PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, MainActivity.class), 
        		PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);*/
		
//		if (!Utils.isServiceStarted(context, context.getPackageName())) {
			Utils.startServiceIntent(context);
//		}
	}

}
