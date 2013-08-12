/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <jni.h>
#include "SYProtocol.h"
#include "Rfid.h"
#include <android/log.h>
#include <jni.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <sys/ioctl.h>
#define CHAR_LEN		1024
#define IOCTL_FINGER_ON   _IO('G',0X07)
#define IOCTL_FINGER_OFF  _IO('G',0X08)
#define  DEVICE_NAME  "/dev/gpio" 

/*
 *
 *  打开串口，打开电源，给指纹模块发送清空指纹flash命令，由于客户并不把指纹一直
 *  存到指纹模块里面而是通过上传到服务器比对，所以用这个命令，相对简单操作上面  
 *  
 *  
 *  返回值说明：1，返回-1 打开错误；2，返回 -2表示硬件不支持或者未上电;3，返回1表示打开指纹设备
 */

JNIEXPORT jint JNICALL Java_com_lyt_boruan_Interface_Zhiwencheck(JNIEnv *pJE,
		jobject jo) {
	static int fd = -1;
	int flage;
	fd = open(DEVICE_NAME, O_RDWR);//1,打开指纹电源
	LOGE("fd = %d  \n", fd);
	if (fd == -1) {
		LOGE("open device %s error \n ", DEVICE_NAME);
		return -1;
	}

	ioctl(fd, IOCTL_FINGER_ON, 0);//1,打开指纹电源
	close(fd);                   //2,关闭指纹电源
	LOGD("Init()open--over ");

	int s = PSOpenDevice(1, 0, 57600 / 9600, 2);//3,打开指纹串口
	if (s == 0) {

		return -1;
	}

	//commond test
	sleep(1); //注意：这里必须加上！！
	int Empty = PSEmpty(0xffffffff); //4,发送测试命令
	LOGD("commond test..... \n");
	if (Empty == -2) {

		flage = -2;

	} else {

		flage = 1;

	}

	fd = open(DEVICE_NAME, O_RDWR); //5,关闭电源
	LOGE("fd again = %d  \n", fd);
	if (fd == -1) {
		LOGE("open device again %s error \n ", DEVICE_NAME);
		return -1;
	}

	ioctl(fd, IOCTL_FINGER_OFF, 0); //5,关闭电源
	close(fd);

	//关闭串口
	PSCloseDevice();  //6,关闭串口

	return flage;
}

/*
 * RFID 硬件检测
 * 返回值说明：1，返回-1表示硬件不支持或无硬件；2，返回-2表示无卡；3，返回1表示读到卡的UID
 * 
 */
JNIEXPORT jint JNICALL Java_com_lyt_boruan_Interface_Rfidcheck(JNIEnv *pJE,
		jobject jo) {
	int i = 0;
	int flag = -1;
	int open = openFM1702(); //1，打开RFID
	LOGD("RFID test..... %d\n", open);
	int select = selectFM1702(); //2，选择类型
	LOGD("RFID test..... %d\n", select);
	if (select == -43) {

		flag = -1;
		return flag;
	}

	for (i = 0; i < 5; i++) {
		int uid = readcard(); ///1 or -1;//3,读卡测试
		if (uid == 1) {
			LOGD("RFID---uid: RFID open\n");
			flag = 1;
			closeFM1702(); //4,别忘记关闭当前测试
			return 1;
		} else {
			if (uid == -1) { //硬件错误
				flag = -1;
				LOGD("RFID---uid: no hw support!!!\n");
			}
			flag = -2; //无卡
		}
	}

	closeFM1702(); //4,别忘记关闭当前测试

	return flag;

}

/*
 * 判断GPS是否存在
 * 返回值类型： 返回1 表示GPS硬件支持，返回-1表示GPS硬件不支持
 */
JNIEXPORT jint JNICALL Java_com_lyt_boruan_Interface_Gpscheck
  (JNIEnv *pJE, jobject jo){

	if (access("/dev/ttyUSB1",R_OK)==0) {  
		LOGD("GPS---OK\n");
		return 1;

	} else {
		LOGD("GPS---not support\n");
		return -1;
	}
	
	return 1;

}

/*
 * 判断3G是否存在
 * 返回值类型： 返回1 表示3G硬件支持，返回-1表示3G硬件不支持
 */
JNIEXPORT jint JNICALL Java_com_lyt_boruan_Interface_Datacheck
  (JNIEnv *pJE, jobject jo){

	if (access("/dev/ttyUSB2",R_OK)==0) {
		LOGD("3g---OK\n");
		return 1;

	} else {
		LOGD("3g---not support\n");
		return -1;
	}
	return 1;

}