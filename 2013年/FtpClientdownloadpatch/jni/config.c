/*
 ============================================================================
 Name        : config.c
 Author      : Frank
 Version     : v1.0
 Copyright   : JNI ftpclient
 Description : 配置FTP服务器的地址、用户名、密码、下载的目录、本地的目录
 "223.4.98.201", "lytadmin1", "740815",
 "/lytadmin1/Test/Beijingxzx/version.xml", "/sdcard/version.xml"
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <jni.h>
#include <string.h>

// for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
#include <android/log.h>

/*
 * Class:     com_frankhujiyun_android_config_DownloadConfiger
 * Method:    GetconfigArray
 * Signature: ()[Ljava/lang/String;
 */
#define JNI_STR_ARRAY_NUM    3
JNIEXPORT jobjectArray JNICALL Java_com_frankhujiyun_android_config_DownloadConfiger_GetconfigArray(
		JNIEnv *jpcEnv, jobject jcObj) {

	jstring jsStr = 0;
	jclass jcStrCls = 0;
	jobjectArray joArray = 0;
	int nIdx = 0;
	char *pachStr[JNI_STR_ARRAY_NUM] = { "223.4.98.201", "lytadmin1", "740815" };

	jcStrCls = (*jpcEnv)->FindClass(jpcEnv, "java/lang/String");
	joArray = (*jpcEnv)->NewObjectArray(jpcEnv, JNI_STR_ARRAY_NUM, jcStrCls, 0);
	for (; nIdx < JNI_STR_ARRAY_NUM; nIdx++) {
		jsStr = (*jpcEnv)->NewStringUTF(jpcEnv, pachStr[nIdx]);

		(*jpcEnv)->SetObjectArrayElement(jpcEnv, joArray, nIdx, jsStr);
	}
	return joArray;

}
