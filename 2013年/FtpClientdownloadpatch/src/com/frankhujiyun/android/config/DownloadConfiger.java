package com.frankhujiyun.android.config;

public class DownloadConfiger {
	
	//Description : ����FTP�������ĵ�ַ���û���������
	public native String[] GetconfigArray();
	
	static {
		System.loadLibrary("config");
	}
}
