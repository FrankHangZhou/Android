package com.lyt.boruan;

public class Interface {
	
	//ָ������Ӳ�����
	public native int Zhiwencheck();
	
	//RFID����Ӳ�����
	public native int Rfidcheck();
	
	//GPS��3G����Ӳ�����
	public native int Gpscheck();
	//3G
	public native int Datacheck();
	
	static {
		System.loadLibrary("check");
	}
	

}
