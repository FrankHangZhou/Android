
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include "Rfid.h"
#include "android/log.h"
#include <stdio.h>
#include <stdlib.h>

static const char *TAG="RFID";
static const int BLOCK=0;
static const int EARA=1;
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#define IOCTL_CARD_ACTIVE   _IO('G', 0x03)
#define IOCTL_CARD_ACTIVE_TYPEB  _IO('G', 0x09)
#define IOCTL_CLOSE_DEV     _IO('G', 0x05)
#define IOCTL_SET_KEY       _IO('G', 0x04)  //�޸�����
#define IOCTL_TEST_KEY      _IO('G', 0x06)	//�Ƿ��޸ĳɹ�
#define IOCTL_READ_CARD     _IO('G', 0x01)
#define IOCTL_WRITE_CARD    _IO('G', 0x02)
#define IOCTL_READ_SECNR    _IO('G', 0x07)
#define IOCTL_WRITE_SECNR   _IO('G', 0x08)
#define IOCTL_CARD_TYPE     _IO('G', 0x0A)  //��ʼ��������
#define RFID_FILE  			"/sys/class/switch/trigger/state"

//#define  FM1702_OK     0                //��ȷ
//#define  FM1702_NOTAGERR   1          //�޿���ѯ��ʧ��
//#define  FM1702_AUTHERR   4           //������֤���ɹ�
//#define  FM1702_CHIPERR   43          //оƬδ�ӵ������
//******************* ����������붨��********************
#define FM1702_OK                0 //��ȷ
#define FM1702_NOTAGERR        1 //�޿���ѯ��ʧ��
#define FM1702_CRCERR           2 //��ƬCRCУ�����
#define FM1702_EMPTY            3 //��ֵ�������
#define FM1702_AUTHERR          4 //������֤���ɹ�
#define FM1702_PARITYERR        5 //��Ƭ��żУ�����
#define FM1702_CODEERR          6 //ͨѶ����(BCCУ���)
#define FM1702_SERNRERR         8 //��Ƭ���кŴ���(anti-collision ����)
#define FM1702_SELECTERR        9 //��Ƭ���ݳ����ֽڴ���(SELECT����)
#define FM1702_NOTAUTHERR      10 //��Ƭû��ͨ����֤
#define FM1702_BITCOUNTERR     11 //�ӿ�Ƭ���յ���λ������
#define FM1702_BYTECOUNTERR   12 //�ӿ�Ƭ���յ����ֽ����������������Ч
#define FM1702_RESTERR           13 //����restore��������
#define FM1702_TRANSERR         14 //����transfer��������
#define FM1702_WRITEERR         15 //����write��������
#define FM1702_INCRERR           16 //����increment��������
#define FM1702_DECRERR          17 //����decrement��������
#define FM1702_READERR          18 //����read��������
#define FM1702_LOADKEYERR      19 //����LOADKEY��������
#define FM1702_FRAMINGERR       20 //FM1702֡����
#define FM1702_REQERR            21 //����req��������
#define FM1702_SELERR             22 //����sel��������
#define FM1702_ANTICOLLERR      23 //����anticoll��������
#define FM1702_INTIVALERR        24 //���ó�ʼ����������
#define FM1702_READVALERR       25 //���ø߼�����ֵ��������
#define FM1702_DESELECTERR       26 //ȡ��ѡ������
#define FM1702_CMD_ERR           42 //�������
#define FM1702_CHIPERR            43 //оƬδ�ӵ������

//frank 2013-4-8 for boruan

int fd = -1;
int isOpened=0;



int isopen(){
	  LOGI("RFID ----------------> isOpened == %s",(isOpened==1 ? 1 : -1));
	  return (isOpened==1 ? 1 : -1);
}



int openFM1702(){
		LOGI("RFID----------------> open");

			if(isOpened==0){
				fd=open("/dev/fm1702",O_RDWR);
				if(fd<0){
					LOGE("RFID fd open error");
					isOpened=0;
					return -1;
				}

				LOGD("RFID fd == %d",fd);
				isOpened=1;
				return 1;
			}else{
				LOGI("RFID ---------------->already opened");
				return 1;
			}

}

int selectFM1702(){

	    LOGD("RFID ----------------> select_type%d",0x02);
		unsigned char type_char[6]={0x02,0x33,0x33,0x33,0x33,0x33};
		int i,ret=-1;

		ret = ioctl(fd, IOCTL_CARD_TYPE, type_char);
		LOGE("RFID ret == %d",ret);
	if(ret == -1)return -44;
	else return -ret;
}

int closeFM1702(){
		LOGI(" RFID ----------------> close");
		if(isOpened==1){
			int i,ret=-1;
			ret = close(fd);
			LOGE("RFID ret == %d",ret);
			if(ret != FM1702_OK){
				isOpened=1;
				LOGE(" RFID ----------------> close fail!!");
				return -1;
			}
			isOpened=0;
			return -1;
		}else{
			LOGI(" RFID ----------------> already closed");
			return 1;
		}

}


int readcard(){
    int i;
    unsigned char UID[6];
    unsigned char user_UID[18];
    unsigned char *p=user_UID;
	LOGI("RFID ----------------> read ");
	int ret=-1;

	ret = ioctl(fd, IOCTL_CARD_ACTIVE, UID);
	LOGE("RFID ret ====== %d",ret);
	if(ret != FM1702_OK){
		sprintf(user_UID, "%d", -ret);
		if(ret==FM1702_CHIPERR){//δ�ӵ�
			return -1;
		}
		return -2;
       
		
	}else{
		for (i=0; i<6; i++)
		{
			sprintf(p, "%.2x", UID[i]);
			p+=3;
		}
		for (i=0; i<17; i++)
		{
			if(user_UID[i]=='\0')
				user_UID[i]=' ';
		}
	}

	LOGI("RFID ----------------> %s",user_UID);
	return 1;
}

