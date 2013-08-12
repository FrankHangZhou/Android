//#ifndef  _SYPROTOCOL_H_
//#define  _SYPROTOCOL_H_

//#include "SYDevice.h"
#include <android/log.h>
#define  LOG_TAG  "IC_card"
#define  DFR(...)   LOGD(__VA_ARGS__)
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)


#include <stdio.h>//frank
///////////////////错误返回码////////////////////
#define PS_OK                0x00
#define PS_COMM_ERR          0x01
#define PS_NO_FINGER         0x02
#define PS_GET_IMG_ERR       0x03
#define PS_FP_TOO_DRY        0x04
#define PS_FP_TOO_WET        0x05
#define PS_FP_DISORDER       0x06
#define PS_LITTLE_FEATURE    0x07
#define PS_NOT_MATCH         0x08
#define PS_NOT_SEARCHED      0x09
#define PS_MERGE_ERR         0x0a
#define PS_ADDRESS_OVER      0x0b
#define PS_READ_ERR          0x0c
#define PS_UP_TEMP_ERR       0x0d
#define PS_RECV_ERR          0x0e
#define PS_UP_IMG_ERR        0x0f
#define PS_DEL_TEMP_ERR      0x10
#define PS_CLEAR_TEMP_ERR    0x11
#define PS_SLEEP_ERR         0x12
#define PS_INVALID_PASSWORD  0x13
#define PS_RESET_ERR         0x14
#define PS_INVALID_IMAGE     0x15
#define PS_HANGOVER_UNREMOVE 0X17


///////////////缓冲区//////////////////////////////
#define CHAR_BUFFER_A          0x01
#define CHAR_BUFFER_B          0x02
#define MODEL_BUFFER           0x03

//////////////////���ں�////////////////////////
#define COM1                   0x01
#define COM2                   0x02
#define COM3                   0x03

//////////////////波特率////////////////////////
#define BAUD_RATE_9600         0x00
#define BAUD_RATE_19200        0x01
#define BAUD_RATE_38400        0x02
#define BAUD_RATE_57600        0x03   //default
#define BAUD_RATE_115200       0x04

#define WINAPI

typedef unsigned char BYTE;

/////////////////////////////////////////
//for open:0-false 1- true
//int WINAPI PSOpenDevice(int nDeviceType,int nPortNum,int nPortPara,int nPackageSize);//C调用C++frank20120331
extern int WINAPI PSOpenDevice(int nDeviceType,int nPortNum,int nPortPara,int nPackageSize);
extern int WINAPI PSCloseDevice();
extern void WINAPI Delay(int nTimes);
//frank
extern int WINAPI Add(int x,int y);
///////////////////////////////////////////////
//////             ָ��                  //////
///////////////////////////////////////////////
///////////////////////////////////////////////
	//检测手指并录取图像
extern int WINAPI      PSGetImage(int nAddr);

	//根据原始图像生成指纹特征
extern int  WINAPI    PSGenChar(int nAddr,int iBufferID);

//精确比对CharBufferA与CharBufferB中的特征文件
extern int WINAPI     PSMatch(int nAddr,int* iScore);

	//以CharBufferA或CharBufferB中的特征文件搜索整个或部分指纹库
extern int  WINAPI    PSSearch(int nAddr,int iBufferID, int iStartPage, int iPageNum, int *iMbAddress);

//将CharBufferA与CharBufferB中的特征文件合并生成模板存于ModelBuffer
extern int  WINAPI    PSRegModule(int nAddr);

//将ModelBuffer中的文件储存到flash指纹库中
extern int  WINAPI    PSStoreChar(int nAddr,int iBufferID, int iPageID);

//从flash指纹库中读取一个模板到ModelBuffer
extern int  WINAPI     PSLoadChar(int nAddr,int iBufferID,int iPageID);

//将特征缓冲区中的文件上传给上位机
extern int WINAPI     PSUpChar(int nAddr,int iBufferID, unsigned char* pTemplet, int* iTempletLength);

//从上位机下载一个特征文件到特征缓冲区
extern int WINAPI     PSDownChar(int nAddr,int iBufferID, unsigned char* pTemplet, int iTempletLength);

//上传原始图像
extern int WINAPI     PSUpImage(int nAddr,unsigned char* pImageData, int* iImageLength);

	//下载原始图像
extern int WINAPI     PSDownImage(int nAddr,unsigned char *pImageData, int iLength);

//上传原始图像
extern int  WINAPI     PSImgData2BMP(unsigned char* pImgData,const char* pImageFile);

//下载原始图像

extern int  WINAPI     PSGetImgDataFromBMP(const char *pImageFile,unsigned char *pImageData,int *pnImageLen);

//删除flash指纹库中的一个特征文件
extern int WINAPI     PSDelChar(int nAddr,int iStartPageID,int nDelPageNum);

//清空flash指纹库
extern int WINAPI     PSEmpty(int nAddr);

//读参数表
extern int WINAPI     PSReadParTable(int nAddr,unsigned char* pParTable);

//++读Flash
extern int WINAPI     PSPowerDown(int nAddr);

//设置设备握手口令
extern int WINAPI     PSSetPwd(int nAddr,unsigned char* pPassword);

//验证设备握手口令
extern int WINAPI     PSVfyPwd(int nAddr,unsigned char* pPassword);

//系统复位，进入上电初始状态
extern int WINAPI      PSReset(int nAddr);


	//读记事本
extern int	WINAPI	    PSReadInfo(int nAddr,int nPage,unsigned char* UserContent);

	//写记事本
extern int	WINAPI	    PSWriteInfo(int nAddr,int nPage,unsigned char* UserContent);

//写模块寄存器－波特率设置
extern int  WINAPI     PSSetBaud(int nAddr,int nBaudNum);
//写模块寄存器－安全等级设置
extern int WINAPI     PSSetSecurLevel(int nAddr,int nLevel);
//写模块寄存器－数据包大小设置
extern int   WINAPI   PSSetPacketSize(int nAddr,int nSize);

extern int   WINAPI    PSUpChar2File(int nAddr,int iBufferID, const char* pFileName);

extern int  WINAPI     PSDownCharFromFile(int nAddr,int iBufferID, const char* pFileName);

extern int WINAPI PSGetRandomData(int nAddr,unsigned char* pRandom);

extern int WINAPI PSSetChipAddr(int nAddr,unsigned char* pChipAddr);

//���ݴ����Ż�ȡ������Ϣ
char* WINAPI   PSErr2Str(int nErrCode);

#define DEVICE_USB		0
#define DEVICE_COM		1
#define DEVICE_UDisk	2


#define IMAGE_X 256
#define IMAGE_Y 288


//#endif

