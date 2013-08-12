
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
#define IOCTL_SET_KEY       _IO('G', 0x04)  //修改密码
#define IOCTL_TEST_KEY      _IO('G', 0x06)	//是否修改成功
#define IOCTL_READ_CARD     _IO('G', 0x01)
#define IOCTL_WRITE_CARD    _IO('G', 0x02)
#define IOCTL_READ_SECNR    _IO('G', 0x07)
#define IOCTL_WRITE_SECNR   _IO('G', 0x08)
#define IOCTL_CARD_TYPE     _IO('G', 0x0A)  //初始化卡类型
#define RFID_FILE  			"/sys/class/switch/trigger/state"

//#define  FM1702_OK     0                //正确
//#define  FM1702_NOTAGERR   1          //无卡或询卡失败
//#define  FM1702_AUTHERR   4           //密码验证不成功
//#define  FM1702_CHIPERR   43          //芯片未加电或已损坏
//******************* 函数错误代码定义********************
#define FM1702_OK                0 //正确
#define FM1702_NOTAGERR        1 //无卡或询卡失败
#define FM1702_CRCERR           2 //卡片CRC校验错误
#define FM1702_EMPTY            3 //数值溢出错误
#define FM1702_AUTHERR          4 //密码验证不成功
#define FM1702_PARITYERR        5 //卡片奇偶校验错误
#define FM1702_CODEERR          6 //通讯错误(BCC校验错)
#define FM1702_SERNRERR         8 //卡片序列号错误(anti-collision 错误)
#define FM1702_SELECTERR        9 //卡片数据长度字节错误(SELECT错误)
#define FM1702_NOTAUTHERR      10 //卡片没有通过验证
#define FM1702_BITCOUNTERR     11 //从卡片接收到的位数错误
#define FM1702_BYTECOUNTERR   12 //从卡片接收到的字节数错误仅读函数有效
#define FM1702_RESTERR           13 //调用restore函数出错
#define FM1702_TRANSERR         14 //调用transfer函数出错
#define FM1702_WRITEERR         15 //调用write函数出错
#define FM1702_INCRERR           16 //调用increment函数出错
#define FM1702_DECRERR          17 //调用decrement函数出错
#define FM1702_READERR          18 //调用read函数出错
#define FM1702_LOADKEYERR      19 //调用LOADKEY函数出错
#define FM1702_FRAMINGERR       20 //FM1702帧错误
#define FM1702_REQERR            21 //调用req函数出错
#define FM1702_SELERR             22 //调用sel函数出错
#define FM1702_ANTICOLLERR      23 //调用anticoll函数出错
#define FM1702_INTIVALERR        24 //调用初始化函数出错
#define FM1702_READVALERR       25 //调用高级读块值函数出错
#define FM1702_DESELECTERR       26 //取消选定错误
#define FM1702_CMD_ERR           42 //命令错误
#define FM1702_CHIPERR            43 //芯片未加电或已损坏

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
		if(ret==FM1702_CHIPERR){//未加电
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

