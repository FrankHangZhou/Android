package com.frankhuijiyun.android.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.R.integer;
import android.util.Log;
import android.util.Xml;


/**
 * XML文档解析工具类
 * 
 * @author Frank 2012-11-05
 * 
 */
public class XMLParserUtil {

	/**
	 * 获取版本更新信息
	 * 
	 * @param is
	 *            读取连接服务version.xml文档的输入流
	 * @return
	 */
	public static VersionInfo getUpdateInfo(InputStream is) {
		VersionInfo info = new VersionInfo();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); // 工厂解析器
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(is, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:
					if ("kernelversion".equals(parser.getName())) {
						info.setkernelVersion(parser.nextText());
					} else if ("androidversion".equals(parser.getName())) {
						info.setAndroidVersion(parser.nextText());
					} else if ("updateTime".equals(parser.getName())) {
						info.setUpdateTime(parser.nextText());
					} else if ("downloadKernelURL".equals(parser.getName())) {
						info.setDownloadKernelURL(parser.nextText());
					} else if ("downloadAndroidURL".equals(parser.getName())) {
						info.setDownloadAndroidURL(parser.nextText());
					} else if ("displayMessage".equals(parser.getName())) {
						info.setDisplayMessage(parseTxtFormat(
								parser.nextText(), "##"));
					} else if ("versionCode".equals(parser.getName())) {
						info.setVersionCode(Integer.parseInt(parser.nextText()));
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return info;
	}

	/**
	 * 根据指定字符格式化字符串（换行）
	 * 
	 * @param data
	 *            需要格式化的字符串
	 * @param formatChar
	 *            指定格式化字符
	 * @return
	 */
	public static String parseTxtFormat(String data, String formatChar) {
		StringBuffer backData = new StringBuffer();
		String[] txts = data.split(formatChar);
		for (int i = 0; i < txts.length; i++) {
			backData.append(txts[i]);
			backData.append("\n");
		}
		return backData.toString();
	}

	/**
	 * 创建xml 内核版本，android版本以及版本号 <?xml version="1.0" encoding="UTF-8" ?> -
	 * <update> <kernelversion>jiyun@ubuntu #4</kernelversion>
	 * <androidversion>2.0_20120530</androidversion>
	 * <versionCode>2</versionCode> <updateTime>2012-05-30</updateTime>
	 * <displayMessage>亲～～新版本发布,赶紧下载吧 ## 1.修改了内核jiyun@ubuntu #4 ##
	 * </displayMessage> </update>
	 * 
	 */
	public static void createXmlFile(String path, String kernelversion,
			String androidversion, int versionCode, String updatetime,
			String displayMessage) {
		Log.e("download", " createNewFile() ");
		File linceseFile = new File(path);
		try {
			linceseFile.createNewFile();
		} catch (IOException e) {
			Log.e("download", "exception in createNewFile() method");
		}
		FileOutputStream fileos = null;
		try {
			fileos = new FileOutputStream(linceseFile);
		} catch (FileNotFoundException e) {
			Log.e("download", "can't create FileOutputStream");
		}
		XmlSerializer serializer = Xml.newSerializer();
		try {
			serializer.setOutput(fileos, "UTF-8");
			serializer.startDocument(null, true);
			serializer.startTag(null, "update");
			serializer.startTag(null, "kernelversion");
			serializer.text(kernelversion);
			serializer.endTag(null, "kernelversion");
			serializer.startTag(null, "androidversion");
			serializer.text(androidversion);
			serializer.endTag(null, "androidversion");
			serializer.startTag(null, "versionCode");
			serializer.text(versionCode+"");
			serializer.endTag(null, "versionCode");
			serializer.startTag(null, "updateTime");
			serializer.text(updatetime);
			serializer.endTag(null, "updateTime");
			serializer.startTag(null, "displayMessage");
			serializer.text(displayMessage);
			serializer.endTag(null, "displayMessage");
			serializer.endTag(null, "update");
			serializer.endDocument();
			serializer.flush();
			fileos.close();
		} catch (Exception e) {
			Log.e("download", "error occurred while creating xml file");
		}

	}

}
