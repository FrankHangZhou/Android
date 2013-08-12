package com.np.enthernet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/*
 * frank 2013-5-23-15-36
 * 测试反射的方法是否同步和设置下
 */
public class Nptools {

	private final static String devInfoClassName = "android.net.ethernet.EthernetDevInfo";
	private static Class<?> mClassType = null;

	public static void doInit(Context mContext) {
		Log.d("debug", "=====start openEthernet==============");
		Object mEthManager = mContext.getSystemService("ethernet");

		Toast.makeText(mContext, "尝试打开以太网...", Toast.LENGTH_SHORT).show();
		Class devInfoClass = null;
		try {
			devInfoClass = mEthManager.getClass().getClassLoader()
					.loadClass(devInfoClassName);
		} catch (Exception e2) {
			Toast.makeText(mContext, "无法获取以太网接口", Toast.LENGTH_SHORT).show();
			e2.printStackTrace();
			return;
		}
		if (devInfoClass == null) {
			Toast.makeText(mContext, "无法获取以太网接口", Toast.LENGTH_SHORT).show();
			return;
		}
		Object info = null;
		try {
			info = devInfoClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(mContext, "创建以太网信息对象失败", Toast.LENGTH_LONG).show();
			return;
		}

		try {
			// 先判断是不是静态的模式，如果不是则先设置成静态的模式
			/****************************************************************/
			Method mode = devInfoClass.getMethod("getConnectMode", null);
			String aa = (String) mode.invoke(info);
			Log.d("debug", "=====default model==============   " + aa);

			if (aa.contains("DHCP")) {
				Log.d("debug", "=====Is dhcp,set manual first=====");
				Method setConnectModeMethod = devInfoClass.getMethod(
						"setConnectMode", String.class);
				setConnectModeMethod.invoke(info, "manual");

			}
			
			/****************************************************************/

			Method setIfNameMethod = devInfoClass.getMethod("setIfName",
					String.class);
			setIfNameMethod.invoke(info, "eth0");
			Method setIpAddressMethod = devInfoClass.getMethod("setIpAddress",
					String.class);
		//	setIpAddressMethod.invoke(info, "192.168.1.10");
			setIpAddressMethod.invoke(info, "192.168.250.115");
			Method setNetMaskMethod = devInfoClass.getMethod("setNetMask",
					String.class);
			setNetMaskMethod.invoke(info, "255.255.255.0");
			// add dns MUST!! frank
			Method setNetDnsMethod = devInfoClass.getMethod("setDnsAddr",
					String.class);
			setNetDnsMethod.invoke(info, "202.101.172.35");

			Method setConnectModeMethod = devInfoClass.getMethod(
					"setConnectMode", String.class);
			setConnectModeMethod.invoke(info, "manual");
		} catch (Exception e1) {
			e1.printStackTrace();
			Toast.makeText(mContext, "修改以太网信息对象失败:" + e1.toString(),
					Toast.LENGTH_SHORT).show();
		}

		try {
			Class ethManagerCls = mEthManager.getClass();
			Method[] methods = ethManagerCls.getMethods();

			// 必须先调用getState方法
			Method getStateMethod = ethManagerCls.getMethod("getState");
			Object stateObj = getStateMethod.invoke(mEthManager);
			Method updateDevInfoMethod = ethManagerCls.getMethod(
					"updateDevInfo", devInfoClass);
			updateDevInfoMethod.invoke(mEthManager, info);
			Method setEnabledMethod = ethManagerCls.getMethod("setEnabled",
					boolean.class);
			setEnabledMethod.invoke(mEthManager, true);
			Toast.makeText(mContext, "以太网打开成功", Toast.LENGTH_SHORT).show();
		} catch (Exception e1) {
			e1.printStackTrace();
			Toast.makeText(mContext, "修改以太网信息对象失败:" + e1.toString(),
					Toast.LENGTH_SHORT).show();
		}

		/*
		 * new Thread(){ public void run(){ while(true){ try {
		 * Thread.sleep(1000); } catch (InterruptedException e) {
		 * e.printStackTrace(); } String result = CmdShell.execCommand("route");
		 * 
		 * int defaultindex = result.indexOf("default"); int index192168 =
		 * result.indexOf("192.168"); if( defaultindex >= 0 && index192168 >= 0
		 * ) { CmdShell.execCommand("route del default gw 192.168.1.150"); } } }
		 * }.start();
		 */

	}
	
   /*
    * 获取以太网状态
    */
	public static int StatusEth(Context mContext){
		Object mEthManager = mContext.getSystemService("ethernet");
		Class ethManagerCls = mEthManager.getClass();
		
		try {
			Method getStateMethod = ethManagerCls.getMethod("getState",null);
			int status=(Integer) getStateMethod.invoke(mEthManager);
			
			return status;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;//-1未知状态
		
		
	}
	
	
	

	public static void doExit(Context mContext) {

		Log.d("debug", "=====start shutdownEthernet==============");
		Object mEthManager = mContext.getSystemService("ethernet");

		Class devInfoClass = null;
		try {
			devInfoClass = mEthManager.getClass().getClassLoader()
					.loadClass(devInfoClassName);
		} catch (Exception e2) {
			Toast.makeText(mContext, "无法获取" + devInfoClassName + "以太网接口",
					Toast.LENGTH_LONG).show();
			e2.printStackTrace();
			return;
		}
		if (devInfoClass == null) {
			Toast.makeText(mContext, "无法获取" + devInfoClassName + "以太网接口",
					Toast.LENGTH_LONG).show();
			return;
		}
		Object info = null;
		try {
			info = devInfoClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(mContext, "创建以太网信息对象失败", Toast.LENGTH_LONG).show();
			return;
		}

		try {
			Class ethManagerCls = mEthManager.getClass();
			Method[] methods = ethManagerCls.getMethods();

			// 必须先调用getState方法
			Method getStateMethod = ethManagerCls.getMethod("getState");
			Object stateObj = getStateMethod.invoke(mEthManager);

			Method setEnabledMethod = ethManagerCls.getMethod("setEnabled",
					boolean.class);
			setEnabledMethod.invoke(mEthManager, false);

		} catch (Exception e1) {
			e1.printStackTrace();
			Toast.makeText(mContext, "修改以太网信息对象失败:" + e1.toString(),
					Toast.LENGTH_LONG);
		}

		Toast.makeText(mContext, "以太网关闭成功", Toast.LENGTH_LONG);
	}

}
