package com.google.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;

public class Tools {

	/**  
	 * �ƶ����翪��  
	 */ 
	public static void toggleMobileData(Context context, boolean enabled) {  
	    ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	    Class<?> conMgrClass = null; // ConnectivityManager��  
	    Field iConMgrField = null; // ConnectivityManager���е��ֶ�  
	    Object iConMgr = null; // IConnectivityManager�������  
	    Class<?> iConMgrClass = null; // IConnectivityManager��  
	    Method setMobileDataEnabledMethod = null; // setMobileDataEnabled����  
	    try {   
	        // ȡ��ConnectivityManager��   
		conMgrClass = Class.forName(conMgr.getClass().getName());   
		// ȡ��ConnectivityManager���еĶ���mService   
		iConMgrField = conMgrClass.getDeclaredField("mService");   
		// ����mService�ɷ���   
		iConMgrField.setAccessible(true);   
		// ȡ��mService��ʵ����IConnectivityManager   
		iConMgr = iConMgrField.get(conMgr);   
		// ȡ��IConnectivityManager��   
		iConMgrClass = Class.forName(iConMgr.getClass().getName());   
		// ȡ��IConnectivityManager���е�setMobileDataEnabled(boolean)����   
		setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);   
		// ����setMobileDataEnabled�����ɷ���   
		setMobileDataEnabledMethod.setAccessible(true);   
		// ����setMobileDataEnabled����   
		setMobileDataEnabledMethod.invoke(iConMgr, enabled);  
		} catch (ClassNotFoundException e) {   
		    e.printStackTrace();  
		} catch (NoSuchFieldException e) {   
		    e.printStackTrace();  
		} catch (SecurityException e) {   
		    e.printStackTrace();  
		} catch (NoSuchMethodException e) {   
		    e.printStackTrace();  
		} catch (IllegalArgumentException e) {   
		    e.printStackTrace();  
		} catch (IllegalAccessException e) {   
		    e.printStackTrace();  
		} catch (InvocationTargetException e) {   
		    e.printStackTrace();  
		} 
	}
	
	
	/**
     * ��װapk�ļ�
     *
     * @param context
     * @param info
     * @param observer
     * @throws Exception
     */
    public static void installFile(Context context, String path, IPackageInstallObserver observer) {
        File file = new File(path);
        if (file == null || !file.isFile())
            return;
        PackageInfo packageInfo = getPackageInfoByPath(context, path);
        if (packageInfo == null) {
        } else {
            int flags = 0;
            try {
                getPackageManger().installPackage(Uri.fromFile(file), observer, flags, packageInfo.packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static PackageInfo getPackageInfoByPath(Context context, String path) {
        return context.getPackageManager().getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
    }
    
    
    /**
     * �õ�packagemanager����װapk��
     *
     * @return ����IPackageManager����
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static IPackageManager getPackageManger() {
        try {
            Class clazz = Tools.class.getClassLoader().loadClass("android.os.ServiceManager");
            Method method = clazz.getMethod("getService", new Class[]{String.class});
            IBinder b = (IBinder) method.invoke(null, "package");
            return IPackageManager.Stub.asInterface(b);
        } catch (Exception e) {
            return null;
        }
    }
	
	
}
