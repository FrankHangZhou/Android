package com.lyt.boruan;

public class Interface {
	
	//指纹有无硬件检测
	public native int Zhiwencheck();
	
	//RFID有无硬件检测
	public native int Rfidcheck();
	
	//GPS和3G有无硬件检测
	public native int Gpscheck();
	//3G
	public native int Datacheck();
	
	static {
		System.loadLibrary("check");
	}
	

}
