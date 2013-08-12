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
	// ��ʼ�����ع���
	FTPDownloader fd = FTPDownloader.getInstance();
	DownloadConfiger config = new DownloadConfiger();

	/*
	 * ��������ַ �û��� ����
	 */
	String server = null;
	String user = null;
	String password = null;

	/** Called when the activity is first created. */

	/*
	 * UI����
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
					// Toast.makeText(getApplicationContext(), "�������:  " + time,
					// Toast.LENGTH_LONG).show();
					break;
				case 3:
					Toast.makeText(getApplicationContext(), "�Ѿ�����",
							Toast.LENGTH_LONG).show();
					break;

				case 4:
					showUpdateDialog();

					break;
				case 5:
					textView2.setText("\n��ʼ�����ں�....." + "\n\n\r"
							+ new Date(System.currentTimeMillis()));

					break;
				case 6:
					textView2.setText("\n����Android" + "\n"
							+ new Date(System.currentTimeMillis()));

					break;
				case 7:
					// textView2.setText("\n�����ں����" + "\n"
					// + new Date(System.currentTimeMillis())+"��ʱ��"+time);
					 Poweroff();

					break;
				case 8:
					textView2.setText("\n����Android���" + "\n"
							+ new Date(System.currentTimeMillis()));

					break;
				case 9:
					textView2.setText("\n���dʧ��,�����쳣��������ļ�������" + "\n"
							+ new Date(System.currentTimeMillis()));

					break;
				case 10:
					// textView2.setText("\n��С: " + FileLength + "�ֽ�");

					break;
				case 11:
					textView2.setText("\n��ѹ�ɹ�\n"
							+ new Date(System.currentTimeMillis()));

					break;
				case 12:
					textView2.setText("\n��ѹʧ��\n"
							+ new Date(System.currentTimeMillis()));

					break;

				default:
					break;
				}
			}
		}

	};

	/*
	 * �Ƿ�ػ�
	 */

	private void Poweroff() {
		Builder builder = new Builder(this);
		builder.setTitle("�汾���½���");
		builder.setMessage("ֻ�йػ�������Ż���Ч��");
		builder.setPositiveButton("�ػ�", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("com.lyt.auto_test.halt");
				intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
				sendBroadcast(intent);
				dialog.dismiss();

			}

			// �������ؿ�
			// showDownloadDialog();

		});
		builder.setNegativeButton("�Ȼ��Լ��ػ�", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();

	}

	/*
	 * ��ʾ���¶Ի���
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

			kernel = "1,�����ںˣ� " + upkernelurl + "\n\r" + "\r\r���°汾��"
					+ info.getkernelVersion() + "\n\r" + "\r\r�ϴ�ʱ�䣺"
					+ info.getUpdateTime();
		} else {
			kernel = "1,�ں��Ѿ�����";

		}
		if (upandroidurl != null) {// û��ַ�Ͳ������µ�

			android = "2,����Android�� " + upandroidurl + "\n\r" + "\r\r���°汾��"
					+ info.getAndroidVersion() + "\n\r" + "\r\r�ϴ�ʱ�䣺"
					+ info.getUpdateTime();
		} else {

			android = "2,Android�Ѿ�����";
		}
		// ���ramdisk

		if (!MySystemProperties.get("ro.remote").contains("2013-7-16")) {

			ramdisk = "3,����ramdisk" + "\n\r\r�ϴ�ʱ�䣺" + info.getUpdateTime();

		} else {
			ramdisk = "3,ramdisk�Ѿ�����";
		}

		/******************************************************/

		AlertDialog.Builder builder = new AlertDialog.Builder(
				FtpClientdownloadpatchActivity.this);
		builder.setTitle("�汾����");
		builder.setMessage(kernel + "\n" + android + "\n" + ramdisk + "\n"
				+ "�������ݣ�" + info.getDisplayMessage());
		builder.setPositiveButton("����", new OnClickListener() {
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
				// �������ؿ�
				// showDownloadDialog();
			}
		});
		builder.setNegativeButton("�Ժ���˵", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();

	}

	/*
	 * ����
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
		textView = (TextView) findViewById(R.id.textView2);// �ں�
		textView1 = (TextView) findViewById(R.id.textView3);// Android
		textView2 = (TextView) findViewById(R.id.textView4);// ������Ϣ
		textView.setText("������ǰ�ں˰汾��" + ReadConfiger.getFormattedKernelVersion());
		textView1.setText("������ǰAndroid�汾��" + ReadConfiger.getAndroidVersion());

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

			Toast.makeText(getApplicationContext(), "�뿪��WIFI����", 1).show();
			// ����֮ǰɾ���������� ������һ�������APP��ɾ�������Ҫ��ÿ�ο�����ʱ��ɾ����
			ZipUtils.delAllFile(savePAth);
			return;
		}

		/*
		 * ����xml
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
				FtpClientdownloadpatchActivity.this, "��ȡ����", "��ȴ�...", true,
				false);
		// ����֮ǰɾ���������� ������һ�������APP��ɾ�������Ҫ��ÿ�ο�����ʱ��ɾ����
		ZipUtils.delAllFile(savePAth);
	}

	/*
	 * �ж�WIFI�����Ƿ�����
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
	 * �����ļ�version.xml���ظ���ȷ��
	 */
	protected boolean checkUpdate() {
		// TODO Auto-generated method stub

		fd.config(server, user, password,
				"/lytadmin1/Test/Beijingxzx/version.xml", "/sdcard/version.xml");

		if (fd.connect()) {
			sb.append("���ӳɹ�...\n");
		} else {
			sb.append("����ʧ��...�����¼��������������\n");
			sendmess(0);
			return false;
		}
		if (fd.login()) {
			sb.append("��¼�ɹ�...\n");

		} else {
			sb.append("��¼ʧ��...\n");
			fd.disConnect();
			sendmess(0);
			return false;
		}
		if (fd.download("version.xml")) {

			sb.append("����������Ϣ���سɹ�...\n");

		} else {
			sb.append("����������Ϣ����ʧ��...\n");
			fd.disConnect();
			sendmess(0);
			return false;
		}
		// fd.disConnect(); ���ر�
		sb.append("����������Ϣ���ؽ���...\n");
		Log.d(TAG, "sb: " + sb.toString());
		sendmess(0);
		return true;

	}

	public void Readconfig() {

		// �ӷ���˻�ȡ�汾��Ϣ
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
				Log.i(TAG, "checkUpdate---�ļ��쳣�ƻ�");
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

			// ��Ӷ�ȡramdisk�汾��Ϣ 2013-7-16
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
			 * ����в�ͬ��ȥ�����ļ�Ȼ�����
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
	 * �����ں�zimage
	 */

	private boolean DownFile() {
		if (upkernelurl == null && upandroidurl == null) {

			return false;
		}
		// ����֮ǰɾ����������
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
		// �ж��Ƿ�������
		if (fd.isconnect()) {
			Log.d(TAG, "is ACTIVITY");
		} else {
			Log.d(TAG, "must reconnect");
			sb.append("/****�ں�����****/\n\n");
			if (fd.connect()) {
				sb.append("�ں��������ӷ������ɹ�...\n");
				sendmess(0);
			} else {
				sb.append("�ں��������ӷ�����ʧ�ܣ������¼��������������\n");
				sendmess(0);
				return false;
			}
			if (fd.login()) {
				sb.append("�ں��������ӷ�������¼�ɹ�...\n");
				sendmess(0);
			} else {
				sb.append("�ں��������ӷ�������¼ʧ��,�����¼��������������\n");
				fd.disConnect();
				sendmess(0);
				return false;
			}

		}
		if (fd.download("zimage.zip")) {// file
			Log.d(TAG, "�ں����سɹ�...");
			sb.append("�ں����سɹ�...\n");
			sendmess(0);

		} else {
			Log.d(TAG, "�ں�����ʧ��...");
			sb.append("�ں�����ʧ��,�����¼��������������\n");
			fd.disConnect();
			sendmess(0);
			return false;
		}
		fd.disConnect();
		sb.append("�ں����ع��̽���...\n");
		Log.d(TAG, "sb: " + sb.toString());
		sendmess(0);
		if (KernelStorePath.contains("zImage")) {// �����ں�
			Log.i(TAG, "UZIP");
			if (execCommand("busybox unzip " + KernelStorePath + " " + "-d"
					+ " " + savePAth)) {
				Log.i(TAG, "unzip success");
				sb.append("�ں˽�ѹ�ɹ���Ч���ںˣ�\n");
				sendmess(0);
				// ����ں˸����ļ�
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
				sb.append("�ں˽�ѹʧ�ܣ����������ظ���\n");
				sendmess(0);
				return false;// ��ѹʧ��
			}
			return true;
		} else {
			sb.append("�ں˼���ʧ����Ч���ں�...\n");
			sendmess(0);
			Log.d(TAG, "sb: " + sb.toString());
			return false;
		}

	}

	/*
	 * //�������ramdisk 2013-7-16
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
		// �ж��Ƿ�������
		if (fd.isconnect()) {
			Log.d(TAG, "ramdiskdown is ACTIVITY");
		} else {
			Log.d(TAG, "ramdiskdown must reconnect");
			sb.append("/****ramdisk����****/\n\n");
			if (fd.connect()) {
				sb.append("ramdisk�������ӷ������ɹ�...\n");
				sendmess(0);
			} else {
				sb.append("ramdisk�������ӷ�����ʧ�ܣ������¼��������������\n");
				sendmess(0);
				return false;
			}
			if (fd.login()) {
				sb.append("ramdisk�������ӷ�������¼�ɹ�...\n");
				sendmess(0);
			} else {
				sb.append("ramdisk�������ӷ�������¼ʧ��,�����¼��������������\n");
				fd.disConnect();
				sendmess(0);
				return false;
			}

		}
		if (fd.download("ramdisk.zip")) {// file
			Log.d(TAG, "ramdisk���سɹ�...");
			sb.append("ramdisk���سɹ�...\n");
			sendmess(0);

		} else {
			Log.d(TAG, "ramdisk����ʧ��...");
			sb.append("ramdisk����ʧ��,�����¼��������������\n");
			fd.disConnect();
			sendmess(0);
			return false;
		}
		fd.disConnect();
		sb.append("ramdisk���ع��̽���...\n");
		Log.d(TAG, "sb: " + sb.toString());
		sendmess(0);
		if (RamdiskStorePath.contains("ramdisk")) {// �����ں�
			Log.i(TAG, "UZIP");
			if (execCommand("busybox unzip " + RamdiskStorePath + " " + "-d"
					+ " " + savePAth)) {
				Log.i(TAG, "unzip success");
				sb.append("ramdisk��ѹ�ɹ���Ч��ramdisk��\n");
				sendmess(0);
				// ���ramdisk�����ļ�
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
				sb.append("ramdisk��ѹʧ�ܣ����������ظ���\n");
				sendmess(0);
				return false;// ��ѹʧ��
			}
			return true;
		} else {
			sb.append("ramdisk����ʧ����Ч��ramdisk...\n");
			sendmess(0);
			Log.d(TAG, "sb: " + sb.toString());
			return false;
		}

	}

	/*
	 * ִ��SU
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