package com.frankhuijiyun.android.util;

import java.lang.reflect.Method;

import android.util.Log;

/*
 * fank@反射系统prop
 */
public class MySystemProperties {
	private static final String TAG = "frank11";

	// String SystemProperties.get(String key){}
	public static String get(String key) {
		init();

		String value = null;

		try {
			value = (String) mGetMethod.invoke(mClassType, key);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	// int SystemProperties.get(String key, int def){}
	public static int getInt(String key, int def) {
		init();

		int value = def;
		try {
			Integer v = (Integer) mGetIntMethod.invoke(mClassType, key, def);
			value = v.intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	// void //int SystemProperties.set(String key, String val){}
	public static void set(String key, String val) {
		init();
		Log.i("frank11", "set");
		try {
			mSetMethod.invoke(mClassType, key, val);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	public static String getSdkVersion() {
		return get("gsm.version.ril-impl");
	}

	// -------------------------------------------------------------------
	private static Class<?> mClassType = null;
	private static Method mGetMethod = null;
	private static Method mGetIntMethod = null;
	private static Method mSetMethod = null;

	private static void init() {
		try {
			if (mClassType == null) {
				mClassType = Class.forName("android.os.SystemProperties");

				mGetMethod = mClassType.getDeclaredMethod("get", String.class);
				mGetIntMethod = mClassType.getDeclaredMethod("getInt",
						String.class, int.class);
				mSetMethod = mClassType.getDeclaredMethod("set", String.class,
						String.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}