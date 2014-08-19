package com.android.abutton;

import com.google.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	if (!Utils.isServiceStarted(context, context.getPackageName())) {
			Utils.startServiceIntent(context);
		}
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
        	ConnectivityManager manager = (ConnectivityManager) context
        			.getSystemService(Context.CONNECTIVITY_SERVICE);
        	NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        	NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        	Utils.Log("test", "网络状�?改变:" + wifi.isConnected() + " 3g:" + gprs.isConnected());
            @SuppressWarnings("deprecation")
			NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
            	if (gprs.isConnected()) {
            		if (!Utils.isServiceStarted(context, context.getPackageName())) {
            			Utils.startServiceIntent(context);
            		}
				}
                if (NetworkInfo.State.CONNECTED == info.getState()) {
                	if (!Utils.isServiceStarted(context, context.getPackageName())) {
            			Utils.startServiceIntent(context);
            		}
                } else if (info.getType() == 1) {
                    if (NetworkInfo.State.DISCONNECTING == info.getState()) {
                    }
                }
            }
        }
    }

}
