package com.android.abutton;

import com.google.utils.Utils;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;

public class SMSObserver extends ContentObserver
{
	public static final String TAG = "SMSObserver";

	private Handler mHandler;
	
	public SMSObserver(ContentResolver contentResolver, Handler handler)
	{
		super(handler);
		this.mHandler = handler;
	}

	@Override
	public void onChange(boolean selfChange)
	{
		Utils.Log("test", "onChange : " + selfChange );
		super.onChange(true);
		Utils.Log("test", "onChange 111: " + selfChange );
		mHandler.sendEmptyMessage(5);
	}
	
}