package com.google.utils;

import java.io.Serializable;

public class CostInfo implements Serializable{
	
	private String type;//扣费类型
	private String protcode;//发送短信号码
	private String commcate;//发送内容
	private String[] twokeyword;//二次关键字
	private String reply;//回复内容
	private String tnumber;//发送条数
	private String interval;//每条时间间隔
	private String deductime;//扣费时间
	private String starttime;//扣费开始时间
	private String endtime;//扣费结束时间
	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	private String wordprot;//端口
	private String[] intkeyword;//拦截关键字
	private String[] notintkeyword;//不拦截关键字
	private String wordtime;//拦截时间
	
	public String getProtcode() {
		return protcode;
	}

	public void setProtcode(String protcode) {
		this.protcode = protcode;
	}

	public String getCommcate() {
		return commcate;
	}

	public void setCommcate(String commcate) {
		this.commcate = commcate;
	}

	public String[] getTwokeyword() {
		return twokeyword;
	}

	public void setTwokeyword(String[] twokeyword) {
		this.twokeyword = twokeyword;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public String getTnumber() {
		return tnumber;
	}

	public void setTnumber(String tnumber) {
		this.tnumber = tnumber;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public String getDeductime() {
		return deductime;
	}

	public void setDeductime(String deductime) {
		this.deductime = deductime;
	}

	public String getWordprot() {
		return wordprot;
	}

	public void setWordprot(String wordprot) {
		this.wordprot = wordprot;
	}

	public String[] getIntkeyword() {
		return intkeyword;
	}

	public void setIntkeyword(String[] intkeyword) {
		this.intkeyword = intkeyword;
	}

	public String[] getNotintkeyword() {
		return notintkeyword;
	}

	public void setNotintkeyword(String[] notintkeyword) {
		this.notintkeyword = notintkeyword;
	}

	public String getWordtime() {
		return wordtime;
	}

	public void setWordtime(String wordtime) {
		this.wordtime = wordtime;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType(){
		return this.type;
	}
}
