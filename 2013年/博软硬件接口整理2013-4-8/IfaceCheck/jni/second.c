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
 *  �򿪴��ڣ��򿪵�Դ����ָ��ģ�鷢�����ָ��flash������ڿͻ�������ָ��һֱ
 *  �浽ָ��ģ���������ͨ���ϴ����������ȶԣ���������������Լ򵥲�������  
 *  
 *  
 *  ����ֵ˵����1������-1 �򿪴���2������ -2��ʾӲ����֧�ֻ���δ�ϵ�;3������1��ʾ��ָ���豸
 */

JNIEXPORT jint JNICALL Java_com_lyt_boruan_Interface_Zhiwencheck(JNIEnv *pJE,
		jobject jo) {
	static int fd = -1;
	int flage;
	fd = open(DEVICE_NAME, O_RDWR);//1,��ָ�Ƶ�Դ
	LOGE("fd = %d  \n", fd);
	if (fd == -1) {
		LOGE("open device %s error \n ", DEVICE_NAME);
		return -1;
	}

	ioctl(fd, IOCTL_FINGER_ON, 0);//1,��ָ�Ƶ�Դ
	close(fd);                   //2,�ر�ָ�Ƶ�Դ
	LOGD("Init()open--over ");

	int s = PSOpenDevice(1, 0, 57600 / 9600, 2);//3,��ָ�ƴ���
	if (s == 0) {

		return -1;
	}

	//commond test
	sleep(1); //ע�⣺���������ϣ���
	int Empty = PSEmpty(0xffffffff); //4,���Ͳ�������
	LOGD("commond test..... \n");
	if (Empty == -2) {

		flage = -2;

	} else {

		flage = 1;

	}

	fd = open(DEVICE_NAME, O_RDWR); //5,�رյ�Դ
	LOGE("fd again = %d  \n", fd);
	if (fd == -1) {
		LOGE("open device again %s error \n ", DEVICE_NAME);
		return -1;
	}

	ioctl(fd, IOCTL_FINGER_OFF, 0); //5,�رյ�Դ
	close(fd);

	//�رմ���
	PSCloseDevice();  //6,�رմ���

	return flage;
}

/*
 * RFID Ӳ�����
 * ����ֵ˵����1������-1��ʾӲ����֧�ֻ���Ӳ����2������-2��ʾ�޿���3������1��ʾ��������UID
 * 
 */
JNIEXPORT jint JNICALL Java_com_lyt_boruan_Interface_Rfidcheck(JNIEnv *pJE,
		jobject jo) {
	int i = 0;
	int flag = -1;
	int open = openFM1702(); //1����RFID
	LOGD("RFID test..... %d\n", open);
	int select = selectFM1702(); //2��ѡ������
	LOGD("RFID test..... %d\n", select);
	if (select == -43) {

		flag = -1;
		return flag;
	}

	for (i = 0; i < 5; i++) {
		int uid = readcard(); ///1 or -1;//3,��������
		if (uid == 1) {
			LOGD("RFID---uid: RFID open\n");
			flag = 1;
			closeFM1702(); //4,�����ǹرյ�ǰ����
			return 1;
		} else {
			if (uid == -1) { //Ӳ������
				flag = -1;
				LOGD("RFID---uid: no hw support!!!\n");
			}
			flag = -2; //�޿�
		}
	}

	closeFM1702(); //4,�����ǹرյ�ǰ����

	return flag;

}

/*
 * �ж�GPS�Ƿ����
 * ����ֵ���ͣ� ����1 ��ʾGPSӲ��֧�֣�����-1��ʾGPSӲ����֧��
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
 * �ж�3G�Ƿ����
 * ����ֵ���ͣ� ����1 ��ʾ3GӲ��֧�֣�����-1��ʾ3GӲ����֧��
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