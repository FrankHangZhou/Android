package com.frankhujiyun.android;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import com.frankhuijiyun.android.util.FTPDownloader;
import com.frankhuijiyun.android.util.MySystemProperties;
import com.frankhuijiyun.android.util.ReadConfiger;
import com.frankhuijiyun.android.util.VersionInfo;
import com.frankhuijiyun.android.util.XMLParserUtil;
import com.frankhuijiyun.android.util.FTPDownloader.StreamListener;
import com.frankhuijiyun.android.util.ZipUtils;
import com.frankhujiyun.android.config.DownloadConfiger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FtpClientdownloadpatchActivity extends Activity {
	private static final String TAG = "--FTPDownloader--";

	ReadConfiger ReadConfiger;
	StringBuilder sb = new StringBuilder();
	public VersionInfo info = null;
	String upkernelurl = null;
	String upandroidurl = null;

	Boolean kernelok = null;
	Boolean ramdiskok = null;

	// ui
	private TextView textView;
	private TextView textView1;
	private TextView textView2;
	private ProgressDialog progressDialog;
	private ProgressBar progressBar;
	String savePAth = Environment.getExternalStorageDirectory() + "/DownFile";
	String KernelStorePath = Environment.getExternalStorageDirectory()
			+ "/DownFile/" + "zImage.zip";
	String RamdiskStorePath = Environment.getExternalStorageDirectory()
			+ "/DownFile/" + "ramdisk.zip";
	String Flagkernel = Environment.getExternalStorageDirectory()
			+ "/DownFile/" + "UpdateKernelture";
	// 初始化下载工具
	FTPDownloader fd = FTPDownloader.getInstance();
	DownloadConfiger config = new DownloadConfiger();

	/*
	 * 服务器地址 用户名 密码
	 */
	String server = null;
	String user = null;
	String password = null;

	/** Called when the activity is first created. */

	/*
	 * UI更新
	 */

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					if (progressDialog != null) {
						progressDialog.dismiss();
						progressDialog = null;
					}
					textView2.setText(sb.toString());
					break;
				case 1:
					progressBar.setProgress(msg.arg1);
					break;
				case 2:
					// Toast.makeText(getApplicationContext(), "下载完成:  " + time,
					// Toast.LENGTH_LONG).show();
					break;
				case 3:
					Toast.makeText(getApplicationContext(), "已经存在",
							Toast.LENGTH_LONG).show();
					break;

				case 4:
					showUpdateDialog();

					break;
				case 5:
					textView2.setText("\n开始下载内核....." + "\n\n\r"
							+ new Date(System.currentTimeMillis()));

					break;
				case 6:
					textView2.setText("\n下载Android" + "\n"
							+ new Date(System.currentTimeMillis()));

					break;
				case 7:
					// textView2.setText("\n下载内核完成" + "\n"
					// + new Date(System.currentTimeMillis())+"耗时："+time);
					 Poweroff();

					break;
				case 8:
					textView2.setText("\n下载Android完成" + "\n"
							+ new Date(System.currentTimeMillis()));

					break;
				case 9:
					textView2.setText("\n下載失敗,网络异常或服务器文件不存在" + "\n"
							+ new Date(System.currentTimeMillis()));

					break;
				case 10:
					// textView2.setText("\n大小: " + FileLength + "字节");

					break;
				case 11:
					textView2.setText("\n解压成功\n"
							+ new Date(System.currentTimeMillis()));

					break;
				case 12:
					textView2.setText("\n解压失败\n"
							+ new Date(System.currentTimeMillis()));

					break;

				default:
					break;
				}
			}
		}

	};

	/*
	 * 是否关机
	 */

	private void Poweroff() {
		Builder builder = new Builder(this);
		builder.setTitle("版本更新结束");
		builder.setMessage("只有关机重启后才会有效。");
		builder.setPositiveButton("关机", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("com.lyt.auto_test.halt");
				intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
				sendBroadcast(intent);
				dialog.dismiss();

			}

			// 弹出下载框
			// showDownloadDialog();

		});
		builder.setNegativeButton("等会自己关机", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();

	}

	/*
	 * 提示更新对话框
	 */
	private void showUpdateDialog() {

		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}

		String kernel = null;
		String android = null;
		String ramdisk = null;
		if (upkernelurl != null) {

			kernel = "1,更新内核： " + upkernelurl + "\n\r" + "\r\r最新版本："
					+ info.getkernelVersion() + "\n\r" + "\r\r上传时间："
					+ info.getUpdateTime();
		} else {
			kernel = "1,内核已经最新";

		}
		if (upandroidurl != null) {// 没地址就不是最新的

			android = "2,更新Android： " + upandroidurl + "\n\r" + "\r\r最新版本："
					+ info.getAndroidVersion() + "\n\r" + "\r\r上传时间："
					+ info.getUpdateTime();
		} else {

			android = "2,Android已经最新";
		}
		// 添加ramdisk

		if (!MySystemProperties.get("ro.remote").contains("2013-7-16")) {

			ramdisk = "3,更新ramdisk" + "\n\r\r上传时间：" + info.getUpdateTime();

		} else {
			ramdisk = "3,ramdisk已经最新";
		}

		/******************************************************/

		AlertDialog.Builder builder = new AlertDialog.Builder(
				FtpClientdownloadpatchActivity.this);
		builder.setTitle("版本更新");
		builder.setMessage(kernel + "\n" + android + "\n" + ramdisk + "\n"
				+ "更新内容：" + info.getDisplayMessage());
		builder.setPositiveButton("下载", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// TODO Auto-generated method stub
						try {
							kernelok = DownFile();
							if (!MySystemProperties.get("ro.remote").contains(
									"2013-7-16")) {
								ramdiskok = ramdiskdownload();
							}
							if(kernelok==true||ramdiskok==true){
								
								sendmess(7);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						return null;
					}

					protected void onPostExecute() {

					}

				}.execute();

				dialog.dismiss();
				// 弹出下载框
				// showDownloadDialog();
			}
		});
		builder.setNegativeButton("以后再说", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();

	}

	/*
	 * 更新
	 */
	public void sendmess(int value) {

		Message message = new Message();
		message.what = value;
		handler.sendMessage(message);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ReadConfiger = new ReadConfiger();
		textView = (TextView) findViewById(R.id.textView2);// 内核
		textView1 = (TextView) findViewById(R.id.textView3);// Android
		textView2 = (TextView) findViewById(R.id.textView4);// 下载信息
		textView.setText("机器当前内核版本：" + ReadConfiger.getFormattedKernelVersion());
		textView1.setText("机器当前Android版本：" + ReadConfiger.getAndroidVersion());

		System.out.println("**** onCreate");
		MySystemProperties.set("service.download", "NoFirst");
		System.out.println("**** onCreateget:  "
				+ MySystemProperties.get("service.download"));
		System.out.println("**** onCreateget:  "
				+ MySystemProperties.get("ro.remote"));

		String list[] = config.GetconfigArray();
		if (list.length == 3) {

			server = list[0];
			user = list[1];
			password = list[2];
		}

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setMax(100);

		if (checkWifi() == false) {

			Toast.makeText(getApplicationContext(), "请开启WIFI网络", 1).show();
			// 下载之前删除所有内容 测试用一运行这个APP就删除，最好要在每次开机的时候删除。
			ZipUtils.delAllFile(savePAth);
			return;
		}

		/*
		 * 下载xml
		 */

		Thread thread = new Thread() {
			public void run() {
				try {
					Looper.prepare();
					if (checkUpdate()) {
						Readconfig();
					} else {

						if (progressDialog != null) {
							progressDialog.dismiss();
							progressDialog = null;
						}
					}

					Looper.loop();

				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
		thread.start();
		progressDialog = ProgressDialog.show(
				FtpClientdownloadpatchActivity.this, "获取数据", "请等待...", true,
				false);
		// 下载之前删除所有内容 测试用一运行这个APP就删除，最好要在每次开机的时候删除。
		ZipUtils.delAllFile(savePAth);
	}

	/*
	 * 判断WIFI网络是否正常
	 */
	public boolean checkWifi() {
		WifiManager mWifiManager = (WifiManager) FtpClientdownloadpatchActivity.this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
		if (mWifiManager.isWifiEnabled() && ipAddress != 0) {
			System.out.println("**** WIFI is on");
			return true;
		} else {
			System.out.println("**** WIFI is off");
			return false;
		}
	}

	/*
	 * 配置文件version.xml下载更新确认
	 */
	protected boolean checkUpdate() {
		// TODO Auto-generated method stub

		fd.config(server, user, password,
				"/lytadmin1/Test/Beijingxzx/version.xml", "/sdcard/version.xml");

		if (fd.connect()) {
			sb.append("连接成功...\n");
		} else {
			sb.append("连接失败...请重新检查网络配置重试\n");
			sendmess(0);
			return false;
		}
		if (fd.login()) {
			sb.append("登录成功...\n");

		} else {
			sb.append("登录失败...\n");
			fd.disConnect();
			sendmess(0);
			return false;
		}
		if (fd.download("version.xml")) {

			sb.append("更新内容信息下载成功...\n");

		} else {
			sb.append("更新内容信息下载失败...\n");
			fd.disConnect();
			sendmess(0);
			return false;
		}
		// fd.disConnect(); 不关闭
		sb.append("更新内容信息下载结束...\n");
		Log.d(TAG, "sb: " + sb.toString());
		sendmess(0);
		return true;

	}

	public void Readconfig() {

		// 从服务端获取版本信息
		Log.i(TAG, "checkUpdate---1");
		VersionInfo info = getVersionInfoFromServer();
		int flage = 0;
		Log.i(TAG, "getAndroidVersion    " + info.getAndroidVersion());
		Log.i(TAG, "getkernelVersion    " + info.getkernelVersion());
		Log.i(TAG, "getUpdateTime    " + info.getUpdateTime());
		Log.i(TAG, "getVersionCode    " + info.getVersionCode());
		Log.i(TAG, "getDownloadAndroidURL    " + info.getDownloadAndroidURL());
		Log.i(TAG, "getDownloadKernelURL    " + info.getDownloadKernelURL());
		Log.i(TAG, "getDisplayMessage    " + info.getDisplayMessage());

		if (info != null) {
			Log.i(TAG, "checkUpdate---uu");
			if (info.getkernelVersion() == null
					&& info.getAndroidVersion() == null
					&& info.getDownloadKernelURL() == null
					&& info.getDownloadAndroidURL() == null
					&& info.getDisplayMessage() == null
					&& info.getUpdateTime() == null
					&& info.getVersionCode() == 0) {
				Log.i(TAG, "checkUpdate---文件异常破坏");
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
				sendmess(9);

				return;
			}
		}

		if (info != null) {
			Log.i(TAG, "checkUpdate---oooo");
			if (ReadConfiger.getFormattedKernelVersion().contains(
					info.getkernelVersion())) {
				Log.i(TAG, "kernelVersion---same");

			} else {

				flage = 1;
				upkernelurl = info.getDownloadKernelURL();
				Log.i(TAG, "kernelVersion---upkernelurl" + upkernelurl);
			}
			Log.i(TAG, "checkUpdate---pppp");
			if (ReadConfiger.getAndroidVersion().contains(
					info.getAndroidVersion())) {

				Log.i(TAG, "getAndroidVersion---same");

			} else {
				flage = 1;
				upandroidurl = info.getDownloadAndroidURL();
				Log.i(TAG, "getAndroidVersion---upandroidurl" + upandroidurl);
			}

			// 添加读取ramdisk版本信息 2013-7-16
			Log.i(TAG, "2013-7-16---ramdisk");
			if (MySystemProperties.get("ro.remote").contains("2013-7-16")) {

				Log.i(TAG, "getramdisk---same");

			} else {
				flage = 1;
				upandroidurl = info.getDownloadAndroidURL();
				Log.i(TAG, "getAndroidVersion---upandroidurl" + upandroidurl);
			}

		}

		Log.i(TAG, "progressDialog---00: " + progressDialog);
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		Log.i(TAG, "progressDialog---11: " + progressDialog);

		if (flage == 1) {
			Log.i(TAG, "showUpdateDialog");
			Message message = new Message();
			message.what = 4;
			handler.sendMessage(message);
			/*
			 * 如果有不同才去备份文件然后更新
			 */
			// String CopyPAth = Environment.getExternalStorageDirectory()
			// + "/sdfuse";
			// File file1 = new File(CopyPAth);
			// if (!file1.exists()) {
			// file1.mkdir();
			// }
			// XMLParserUtil.createXmlFile(CONFIG_PATH, info.getkernelVersion(),
			// info.getAndroidVersion(), info.getVersionCode(),
			// info.getUpdateTime(), info.getDisplayMessage());
			flage = 0;
		}

	}

	private VersionInfo getVersionInfoFromServer() {
		// VersionInfo info = null;
		File file = new File("/sdcard/version.xml");
		try {
			info = XMLParserUtil.getUpdateInfo(new FileInputStream(file));
			Log.i(TAG, "getVersionInfoFromServer------------info:  " + info);
		} catch (IOException e) {
			e.printStackTrace();
			sendmess(9);
			Log.i(TAG, "Downloadkernelurl------------bad");
			info = null;
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			return info;
		}

		return info;
	}

	/*
	 * 下载内核zimage
	 */

	private boolean DownFile() {
		if (upkernelurl == null && upandroidurl == null) {

			return false;
		}
		// 下载之前删除所有内容
		ZipUtils.delAllFile(savePAth);
		sendmess(5);
		File file1 = new File(savePAth);
		if (!file1.exists()) {
			file1.mkdir();
		}
		// FTPDownloader fd = FTPDownloader.getInstance();
		fd.config(server, user, password,
				"/lytadmin1/Test/Beijingxzx/zimage.zip",
				"/sdcard/DownFile/zimage.zip");
		fd.setsListener(new StreamListener() {
			long totalsize = 0;

			@Override
			public void totalSize(long total) {
				Log.d(TAG, "DownFile_____TotalSize: " + total);
				totalsize = total;

			}

			@Override
			public void progressed(long progressed) {
				Log.d(TAG, "DownFile_____Progress:" + progressed / totalsize
						* 100 + "%");

			}

			@Override
			public void progress(int progress) {
				Log.d(TAG, "DownFile_____Progress: " + progress);
				Message message = new Message();
				message.what = 1;
				message.arg1 = progress;
				handler.sendMessage(message);

			}
		});
		// 判断是否连接着
		if (fd.isconnect()) {
			Log.d(TAG, "is ACTIVITY");
		} else {
			Log.d(TAG, "must reconnect");
			sb.append("/****内核下载****/\n\n");
			if (fd.connect()) {
				sb.append("内核下载连接服务器成功...\n");
				sendmess(0);
			} else {
				sb.append("内核下载连接服务器失败，请重新检查网络配置重试\n");
				sendmess(0);
				return false;
			}
			if (fd.login()) {
				sb.append("内核下载连接服务器登录成功...\n");
				sendmess(0);
			} else {
				sb.append("内核下载连接服务器登录失败,请重新检查网络配置重试\n");
				fd.disConnect();
				sendmess(0);
				return false;
			}

		}
		if (fd.download("zimage.zip")) {// file
			Log.d(TAG, "内核下载成功...");
			sb.append("内核下载成功...\n");
			sendmess(0);

		} else {
			Log.d(TAG, "内核下载失败...");
			sb.append("内核下载失败,请重新检查网络配置重试\n");
			fd.disConnect();
			sendmess(0);
			return false;
		}
		fd.disConnect();
		sb.append("内核下载过程结束...\n");
		Log.d(TAG, "sb: " + sb.toString());
		sendmess(0);
		if (KernelStorePath.contains("zImage")) {// 更新内核
			Log.i(TAG, "UZIP");
			if (execCommand("busybox unzip " + KernelStorePath + " " + "-d"
					+ " " + savePAth)) {
				Log.i(TAG, "unzip success");
				sb.append("内核解压成功有效的内核！\n");
				sendmess(0);
				// 标记内核更新文件
				File filekernel = new File(Flagkernel);
				if (!filekernel.exists())
					Log.i(TAG, "createNewFile filekernel ");
				try {
					filekernel.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				sb.append("内核解压失败，请重新下载更新\n");
				sendmess(0);
				return false;// 解压失败
			}
			return true;
		} else {
			sb.append("内核检验失败无效的内核...\n");
			sendmess(0);
			Log.d(TAG, "sb: " + sb.toString());
			return false;
		}

	}

	/*
	 * //添加下载ramdisk 2013-7-16
	 */

	private boolean ramdiskdownload() {

		// FTPDownloader fd = FTPDownloader.getInstance();
		fd.config(server, user, password,
				"/lytadmin1/Test/Beijingxzx/ramdisk.zip",
				"/sdcard/DownFile/ramdisk.zip");
		fd.setsListener(new StreamListener() {
			long totalsize = 0;

			@Override
			public void totalSize(long total) {
				Log.d(TAG, "ramdiskdownDownFile_____TotalSize: " + total);
				totalsize = total;

			}

			@Override
			public void progressed(long progressed) {
				Log.d(TAG, "ramdiskdownDownFile_____Progress:" + progressed
						/ totalsize * 100 + "%");

			}

			@Override
			public void progress(int progress) {
				Log.d(TAG, "ramdiskdownDownFile_____Progress: " + progress);
				Message message = new Message();
				message.what = 1;
				message.arg1 = progress;
				handler.sendMessage(message);

			}
		});
		// 判断是否连接着
		if (fd.isconnect()) {
			Log.d(TAG, "ramdiskdown is ACTIVITY");
		} else {
			Log.d(TAG, "ramdiskdown must reconnect");
			sb.append("/****ramdisk下载****/\n\n");
			if (fd.connect()) {
				sb.append("ramdisk下载连接服务器成功...\n");
				sendmess(0);
			} else {
				sb.append("ramdisk下载连接服务器失败，请重新检查网络配置重试\n");
				sendmess(0);
				return false;
			}
			if (fd.login()) {
				sb.append("ramdisk下载连接服务器登录成功...\n");
				sendmess(0);
			} else {
				sb.append("ramdisk下载连接服务器登录失败,请重新检查网络配置重试\n");
				fd.disConnect();
				sendmess(0);
				return false;
			}

		}
		if (fd.download("ramdisk.zip")) {// file
			Log.d(TAG, "ramdisk下载成功...");
			sb.append("ramdisk下载成功...\n");
			sendmess(0);

		} else {
			Log.d(TAG, "ramdisk下载失败...");
			sb.append("ramdisk下载失败,请重新检查网络配置重试\n");
			fd.disConnect();
			sendmess(0);
			return false;
		}
		fd.disConnect();
		sb.append("ramdisk下载过程结束...\n");
		Log.d(TAG, "sb: " + sb.toString());
		sendmess(0);
		if (RamdiskStorePath.contains("ramdisk")) {// 更新内核
			Log.i(TAG, "UZIP");
			if (execCommand("busybox unzip " + RamdiskStorePath + " " + "-d"
					+ " " + savePAth)) {
				Log.i(TAG, "unzip success");
				sb.append("ramdisk解压成功有效的ramdisk！\n");
				sendmess(0);
				// 标记ramdisk更新文件
				File filekernel = new File(Flagkernel);
				if (!filekernel.exists())
					Log.i(TAG, "createNewFile filekernel ");
				try {
					filekernel.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				sb.append("ramdisk解压失败，请重新下载更新\n");
				sendmess(0);
				return false;// 解压失败
			}
			return true;
		} else {
			sb.append("ramdisk检验失败无效的ramdisk...\n");
			sendmess(0);
			Log.d(TAG, "sb: " + sb.toString());
			return false;
		}

	}

	/*
	 * 执行SU
	 */

	private Boolean execCommand(String command) {
		Process process = null;
		DataOutputStream os = null;
		String line = "";
		StringBuilder sb = new StringBuilder(line);
		try {
			process = Runtime.getRuntime().exec("su");

			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream);
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			while ((line = bufferedreader.readLine()) != null) {
				Log.i(TAG, "line: " + line);
				sb.append(line);
				sb.append('\n');
			}
			process.waitFor();
		} catch (Exception e) {
			Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "
					+ e.getMessage());

		}
		Log.i(TAG, "unzip: " + sb.toString());
		if (sb.toString().contains("inflating")) {
			Log.i(TAG, "unzip success");
			return true;

		}

		return false;

	}

	protected void onPause() {
		super.onPause();
		System.out.println("onPause");

	}

	protected void onStop() {
		super.onPause();
		System.out.println("onStop");

	}

	public void onBackPressed() {
		System.out.println("onBackPressed");
		super.onBackPressed();
	}

	protected void onDestroy() {
		System.out.println("onDestroy");
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}

		if (fd.isconnect()) {
			System.out.println("onPause but isconnect");
			fd.disConnect();

		}

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("onResume");

	}

}