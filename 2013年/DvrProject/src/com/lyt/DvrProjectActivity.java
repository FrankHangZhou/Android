package com.lyt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.FloatMath;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.graphics.BitmapFactory.Options;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.lyt.ditushow.*;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.lyt.camera.CameraPreview;
import com.lyt.communication.face;

/*
 * 2013-03-13 @author frank
 * DVR��Ƭ�����Լ�¼����
 * ע�����շ�Ϊvideo�Լ�DVR����������Ƭ
 */
public class DvrProjectActivity extends Activity implements
		CameraPreview.OnCameraStatusListener {

	private String TAG = "dvr";
	public static final String PATH = Environment.getExternalStorageDirectory()
			.toString() + "/AndroidDvr/";// ������ָ񱾵���Ƭ
	public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	private CameraPreview mCameraPreview;

	private SnapSetting snapinfo = null;

	private Timer mTimer = null;// for camera
	private TimerTask mTimerTask = null;

	// spinner ȫ��
	private int stores1;
	private int stores2;

	// ����״̬
	private String snapflage;
	private boolean isGO;
	private CameraThread mCameaTread = null;

	protected static final int STOP = 1;
	protected static final int POSCHANGED = 2;
	protected static final int UPDATE = 3;
	TextView tv2;

	// ���նԻ�������Ĭ�ϵĳ�ʼ��ͼƬ
	private Uri uridefault;

	// �ٶȵ�ͼ,��λ���
	/********************************************************************/
	private BMapManager mBMapMan = null;
	private MapView mMapView = null;
	private static final String BMAPKEY = "F6E1DC2EACF9D8576BA96FDF8151EC82F81ACFD1";
	private static Context mapContext;
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	public NotifyLister mNotifyer = null;
	LocationData locData = null;
	MyLocationOverlay myLocationOverlay = null;
	private MapController mMapController = null;
	public MKMapViewListener mMapListener = null;
	/********************************************************************/

	String showrecord = "";
	private String dvrpath = null;

	Spinner s1;
	Spinner s2;
	Button snap;
	private AlertDialog mLowBatteryDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ���ú���
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ����ȫ��
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// �ٶȵ�ͼ��λ���
		/****************************************************************/
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init(BMAPKEY, new MyGeneralListener());
		mapContext = getApplicationContext();
		// ע�⣺������ʹ��setContentViewǰ��ʼ��BMapManager���󣬷���ᱨ��
		/****************************************************************/

		setContentView(R.layout.main);
		// ��ʾ�Ӵ�
		TextView tv = (TextView) findViewById(R.id.view2);
		TextPaint tp = tv.getPaint();
		tp.setFakeBoldText(true);

		// ������״̬
		tv2 = (TextView) findViewById(R.id.view1);

		// ����Ԥ������
		mCameraPreview = (CameraPreview) findViewById(R.id.preview);
		mCameraPreview.setOnCameraStatusListener(this);

		// �ٶȵ�ͼ��λ���
		/****************************************************************/
		mMapView = (MapView) findViewById(R.id.bmapView);

        mMapController = mMapView.getController();
        
        initMapView();
        
        mLocClient = new LocationClient( this );
        mLocClient.registerLocationListener( myListener );
        
        //λ��������ش���
        mNotifyer = new NotifyLister();
        mNotifyer.SetNotifyLocation(30.298465083333333,120.129234
        		,3000,"bd09ll");//4����������Ҫλ�����ѵĵ�����꣬���庬������Ϊ��γ�ȣ����ȣ����뷶Χ������ϵ����(gcj02,gps,bd09,bd09ll)
        mLocClient.registerNotify(mNotifyer);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//��gps
        option.setCoorType("bd09ll");     //������������
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mMapView.getController().setZoom(14);
        mMapView.getController().enableClick(true);
        
        mMapView.displayZoomControls(true);
        mMapListener = new MKMapViewListener() {
			
			@Override
			public void onMapMoveFinish() {
				// TODO Auto-generated method stub
				Log.d("loctest","onMapMoveFinish");
			}
			
			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				// TODO Auto-generated method stub
				String title = "";
				Log.d("loctest","onClickMapPoi");
				if (mapPoiInfo != null){
					title = mapPoiInfo.strText;
				//	Toast.makeText(mBMapMan.this,title,Toast.LENGTH_SHORT).show();
				}
			}
		};
		mMapView.regMapViewListener(mBMapMan, mMapListener);
		myLocationOverlay = new MyLocationOverlay(mMapView);
		locData = new LocationData();
	    myLocationOverlay.setData(locData);
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		mMapView.refresh();

		/****************************************************************/

		// snapinfo

		snapinfo = new SnapSetting();
		snap = (Button) findViewById(R.id.buttonsnap);
		snap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// ��������
				if (mLowBatteryDialog != null)
					mLowBatteryDialog.dismiss();
				showsnapsetting();

			}
		});

	}

	@Override
	public void onResume() {
		Log.i(TAG, "onResume ---");
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.i(TAG, "onPause ---");
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDestroy ---");

		mMapView.destroy();
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}

		if (stores1 == 1 || stores1 == 0 || stores1 == 2 || stores1 == 3
				|| stores1 == 4) {// ���ָ���Ƭ �˳���ʱ��ر��߳�
			if (mCameaTread != null) {
				isGO = false;
				mCameaTread = null;

			}
		}

		com.lyt.loading.MainActivity.Interface.LocketDisconnect();

		super.onDestroy();
	}

	// ������״̬����
	/***********************************************************************************************************/
	final Handler cwjHandler = new Handler();

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateviewstatus();
		}
	};

	public void updateviewstatus() {

		tv2.setText("��ǰ״̬��" + snapinfo.getstatus());

	}

	/************************************************************************************************************/
	/*
	 * �������ò��� 1�����ָ��Ǳ���4·��Ƭͨ�� 2����ѡ��������4·ͨ��
	 */
	private void showsnapsetting() {
		final View config = View.inflate(this, R.layout.snap_setting, null);//
		final TextView statusview = (TextView) config
				.findViewById(R.id.statusview);
		final myImageView image1 = (myImageView) config
				.findViewById(R.id.iamge);

		// ע��㲥�����ص���Ƭʵʱ����
		final BroadcastReceiver rssiReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				final String action = intent.getAction();
				if (action.equals("videocameraupdate")) {

					// ״̬
					statusview.setText("��ǰ����״̬��" + snapflage);

					Bundle bundle = intent.getExtras();
					// �����Ƭuri
					Uri uri = Uri.parse(bundle.getString("uriStr"));
					Log.i(TAG, "intent " + uri);
					// �������ʱ��
					long dateTaken = bundle.getLong("dateTaken");
					try {
						// ��ý�����ݿ��ȡ����Ƭ
						String[] proj = { MediaStore.Images.Media.DATA };

						Cursor cursor = managedQuery(uri, proj, null, null,
								null);

						int column_index = cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

						cursor.moveToFirst();
						// ����������ֵ��ȡͼƬ·��
						String path = cursor.getString(column_index);

						Log.i(TAG, "path ---" + path);
						Bitmap b = BitmapFactory.decodeFile(path, null);
						image1.setImageBitmap(b);
						image1.setColour(Color.GRAY);
						image1.setBorderWidth(8);

						System.gc();
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else if (action.equals("videocameraupdatedvr")) {

					// ״̬
					statusview.setText("��ǰ����״̬��" + snapinfo.getstatus());
					Bundle bundle = intent.getExtras();
					// �����Ƭuri
					String path = bundle.getString("uriStr");
					Log.i(TAG, "================");
					Log.i(TAG, "                ");
					Log.i(TAG, "show dvr picture: " + path);
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 2;// ͼƬ��߶�Ϊԭ���Ķ���֮һ����ͼƬΪԭ�����ķ�֮һ

					Bitmap b = BitmapFactory.decodeFile(path, options);
					Log.v(TAG, "display--showrecord:" + showrecord);
					image1.setImageBitmap(b);
					image1.setColour(Color.RED);
					image1.setBorderWidth(8);
					System.gc();

				}

			}
		};

		IntentFilter filter;

		if (uridefault != null) {// ���һ��ʼ������ͼƬ�����

			try {
				Log.i(TAG, "uridefault  beginn " + uridefault);
				String[] proj = { MediaStore.Images.Media.DATA };

				Cursor cursor = managedQuery(uridefault, proj, null, null, null);

				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

				cursor.moveToFirst();
				// ����������ֵ��ȡͼƬ·��
				String path = cursor.getString(column_index);

				Log.i(TAG, "path ---" + path);
				Bitmap b = BitmapFactory.decodeFile(path, null);
				image1.setImageBitmap(b);
				image1.setColour(Color.GRAY);
				image1.setBorderWidth(8);

				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (dvrpath != null) {// ���һ��ʼ������ͼƬ�����

			try {
				Log.i(TAG, "uridefault  beginn " + dvrpath);
				Log.i(TAG, "path ---" + dvrpath);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;// ͼƬ��߶�Ϊԭ���Ķ���֮һ����ͼƬΪԭ�����ķ�֮һ

				Bitmap b = BitmapFactory.decodeFile(dvrpath, options);
				Log.v(TAG, "display--showrecord:" + showrecord);
				image1.setImageBitmap(b);
				image1.setColour(Color.RED);
				image1.setBorderWidth(8);

				System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		s1 = (Spinner) config.findViewById(R.id.spinnercolor);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.model, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		s1.setAdapter(adapter);
		if (stores1 == 1 || stores1 == 2 || stores1 == 3 || stores1 == 4) {
			s1.setSelection(stores1, true);
		}

		s1.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				showToast("Spinner: position=" + position + " id=" + id);
				stores1 = position;// �洢

			}

			public void onNothingSelected(AdapterView<?> parent) {
				showToast("Spinner1: unselected");
			}
		});

		// �ֱ���
		s2 = (Spinner) config.findViewById(R.id.spinnercolor2);
		ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
				this, R.array.ratio, android.R.layout.simple_spinner_item);
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s2.setAdapter(adapter1);
		if (stores2 == 1 || stores2 == 2) {
			s2.setSelection(stores2, true);
		}
		s2.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				showToast("s2: position=" + position + " id=" + id);
				stores2 = position;
			}

			public void onNothingSelected(AdapterView<?> parent) {
				showToast("s2: unselected");
			}
		});

		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("                            ��������");
		b.setCancelable(true);
		b.setView(config);
		// ���沢���˳�
		Button saveandstart = (Button) config
				.findViewById(R.id.btn_saveandstart);
		saveandstart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// mCameraPreview.takePicture();//test
				// ��¼����״̬����ʼ����,ֻҪ����˱��沢�����¿�ʼ����Ҫ���¿�ʼ��
				// ���ʱ��
				EditText edit = (EditText) config.findViewById(R.id.EditText1);
				String time = edit.getText().toString();
				Log.i(TAG, "time:       " + time);

				// ����״̬

				// �Ƿ�ͨ��3G�ϴ�
				CheckBox check = (CheckBox) config
						.findViewById(R.id.radioButton1);

				snapinfo.setstatus(snapflage); // ״̬
				snapinfo.setmodel(stores1); // ģʽ
				snapinfo.setradio(stores2); // �ֱ���
				if (time != null && time.length() > 0) {// ���ʱ��
					snapinfo.setinterneltime(time);
				} else {
					snapinfo.setinterneltime("1");// Ĭ��һ����
				}
				if (check.isChecked()) { // �Ƿ�ͨ��3G�ϴ�
					snapinfo.setupdate(true);
				} else {
					snapinfo.setupdate(false);
				}

				if (stores1 == 0 || stores1 == 1 || stores1 == 2
						|| stores1 == 3 || stores1 == 4) {// ���ָ���Ƭ����DVR��ͨ������

					if (mCameaTread == null) {
						if (stores1 == 4)
							snapinfo.setstatus("��������" + "ģʽ�����ָ�"); // ��������״̬
						else
							snapinfo.setstatus("��������" + "ģʽ��ͨ��" + stores1); // ��������״̬
						isGO = true;
						mCameaTread = new CameraThread();
						new Thread(mCameaTread).start();
					}

					// ����������
					Thread t = new Thread() {
						public void run() {
							cwjHandler.post(mUpdateResults); // ����UI�߳̿��Ը��½����
						}
					};
					t.start();
				}

				if (mLowBatteryDialog != null) {
					mLowBatteryDialog.dismiss();
					// ȡ���㲥
					unregisterReceiver(rssiReceiver);
				}

			}
		});
		// ֹͣ
		Button stop = (Button) config.findViewById(R.id.btn_stop);
		stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLowBatteryDialog != null) {
					mLowBatteryDialog.dismiss();
					snapflage = "null";// ����״̬
					snapinfo.setstatus(snapflage); // ״̬
					// ȡ���㲥
					unregisterReceiver(rssiReceiver);
				}
				if (stores1 == 1 || stores1 == 2 || stores1 == 3
						|| stores1 == 4 || stores1 == 0) {// ���ָ���Ƭ
					if (mCameaTread != null) {
						isGO = false;
						mCameaTread = null;

					}

					// ����������
					Thread t = new Thread() {
						public void run() {
							cwjHandler.post(mUpdateResults); // ����UI�߳̿��Ը��½����
						}
					};
					t.start();
				}

			}
		});
		// ȡ��
		Button cancel = (Button) config.findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLowBatteryDialog != null) {
					mLowBatteryDialog.dismiss();
					// ȡ���㲥
					unregisterReceiver(rssiReceiver);
				}

			}
		});

		AlertDialog d = b.create();
		// ���ô�Сλ��
		Window dialogWindow = d.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.TOP);

		// ��Window��Attributes�ı�ʱϵͳ����ô˺���,����ֱ�ӵ�����Ӧ������Դ��ڲ����ĸ���,Ҳ������setAttributes
		// dialog.onWindowAttributesChanged(lp);
		dialogWindow.setAttributes(lp);

		d.show();
		d.getWindow().setLayout(600, 460);

		// ��ʾ��һ�ε�Ĭ������
		if (snapinfo != null) {

			Log.i(TAG, "displaygetstatus:             " + snapinfo.getstatus());
			Log.i(TAG,
					"displaygetinterneltime:       "
							+ snapinfo.getinterneltime());
			Log.i(TAG, "displaygetupdate:             " + snapinfo.getupdate());
			Log.i(TAG, "displaygetgetmodel:             " + snapinfo.getmodel());
			Log.i(TAG, "displaygetgetradio:             " + snapinfo.getradio());

			// ״̬
			statusview.setText("��ǰ����״̬��" + snapinfo.getstatus());
			// ���ռ��ʱ��
			EditText edit = (EditText) config.findViewById(R.id.EditText1);
			edit.setText(snapinfo.getinterneltime());
			// ʯͷͨ��3G�ϴ�
			CheckBox check = (CheckBox) config.findViewById(R.id.radioButton1);
			if (snapinfo.getupdate() != null)
				check.setChecked(snapinfo.getupdate());

		}

		mLowBatteryDialog = d;

		if (mLowBatteryDialog != null) {// ����Ի�����ʾ��ʱ��ע��
			Log.i(TAG, "mLowBatteryDialog---888");

			filter = new IntentFilter();
			filter.addAction("videocameraupdate");
			filter.addAction("videocameraupdatedvr");
			registerReceiver(rssiReceiver, filter);

		}
	}

	private void showToast(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	// ��̨��ʱ����

	class CameraThread implements Runnable {

		int model;
		int flag = 0;
		int data;

		public void run() {
			model = snapinfo.getmodel();
			while (isGO) {
				if (model == 4) {// ���ָ�

					Log.e(TAG, "==snap==");
					mCameraPreview.takePicture();// test
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {

					Log.e(TAG, "==snap==model: " + model);
					if (showrecord != "") {
						try {
							data = Integer.parseInt(showrecord);
							Log.v(TAG, "startEchoThread-----------11 ");
							if (data == -101) {
								// �ж������Ƿ�ͨ
								Log.v(TAG, "checknetwork ");
								if (isNetworkAvailable(DvrProjectActivity.this)) {
									// ��������½����,����ָ�
									com.lyt.loading.MainActivity.Interface
											.danreset();// ����dan
									SystemClock.sleep(10 * 1000);
									if (Integer
											.parseInt(com.lyt.loading.MainActivity.Interface
													.login()) == 1) {
										// OK
										Log.v(TAG, "Network OK ");
										flag = 0;
									} else {
										Log.v(TAG, "Network LOgin errror ");
										flag = -1;

									}
								} else {
									Log.v(TAG, "checknetwork not valid ");
									flag = -1;

								}

							}
						} catch (NumberFormatException e) {
							// ��������򲻽��д���
							Log.v(TAG, "NumberFormatException " + e);
						}

					}
					if (flag == 0) {// ��DVR��Ƭ
						Log.v(TAG, "snap begin  whatpath " + model);
						dvrpath = snap(Integer.toString(model));
						Log.v(TAG, "show picture��    " + dvrpath);
						Intent intent = new Intent("videocameraupdatedvr");
						intent.putExtra("uriStr", dvrpath);
						sendBroadcast(intent);

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}

			}

		}

	}

	/*
	 * dvr���� ���� pathֵͨ��0~4
	 */

	private String snap(String path) {
		Log.v(TAG, "snap 2");
		showrecord = com.lyt.loading.MainActivity.Interface.snap(path);
		return showrecord;

	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager mConnMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = mConnMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mMobile = mConnMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean flag = false;
		if ((mWifi != null)
				&& ((mWifi.isAvailable()) || (mMobile.isAvailable()))) {
			if ((mWifi.isConnected()) || (mMobile.isConnected())) {
				flag = true;
			}
		}
		return flag;
	}

	/********************************* ������� *****************************************************************/

	/**
	 * �洢ͼ�񲢽���Ϣ�����ý�����ݿ�
	 */
	private Uri insertImage(ContentResolver cr, String name, long dateTaken,
			String directory, String filename, Bitmap source, byte[] jpegData) {

		OutputStream outputStream = null;
		String filePath = directory + filename;
		try {
			File dir = new File(directory);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(directory, filename);
			if (file.createNewFile()) {
				outputStream = new FileOutputStream(file);
				if (source != null) {
					Log.e(TAG, "==111==");
					source.compress(CompressFormat.JPEG, 75, outputStream);
					// } else {
					Log.e(TAG, "==write picture==");
					outputStream.write(jpegData);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Throwable t) {
				}
			}
		}
		ContentValues values = new ContentValues(7);
		values.put(MediaStore.Images.Media.TITLE, name);
		values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
		values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		values.put(MediaStore.Images.Media.DATA, filePath);
		return cr.insert(IMAGE_URI, values);
	}

	/**
	 * ������ս����¼�
	 */
	@Override
	public void onCameraStopped(byte[] data) {
		Log.e(TAG, "==onCameraStopped==");
		Log.e(TAG, "==onCameraStopped==");
		Log.e(TAG, "==onCameraStopped==");
		// ����ͼ��
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		// ϵͳʱ��
		long dateTaken = System.currentTimeMillis();
		// ͼ������
		String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken)
				.toString() + ".jpg";
		// �洢ͼ��PATHĿ¼��
		Uri uri = insertImage(getContentResolver(), filename, dateTaken, PATH,
				filename, bitmap, data);
		uridefault = uri;// ����
		snapflage = "��������" + "ģʽ�����ָ�";

		// ���ؽ��
		Intent intent = new Intent("videocameraupdate");
		intent.putExtra("uriStr", uri.toString());
		intent.putExtra("dateTaken", dateTaken);
		sendBroadcast(intent);
		mCameraPreview.restartpreview();

	}

	// �ٶȵ�ͼ,��λ���
	/********************************************************************/
	 protected void onSaveInstanceState(Bundle outState) {
	    	super.onSaveInstanceState(outState);
	    	mMapView.onSaveInstanceState(outState);
	    	
	    }
	    
	    @Override
	    protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    	super.onRestoreInstanceState(savedInstanceState);
	    	mMapView.onRestoreInstanceState(savedInstanceState);
	    }
	    

	private void initMapView() {
		mMapView.setLongClickable(true);
		// mMapController.setMapClickEnable(true);
		mMapView.setSatellite(true);
	}

	// �����¼���������������ͨ�������������Ȩ��֤�����
	static class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(mapContext, "���������������", Toast.LENGTH_LONG)
						.show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(mapContext, "������ȷ�ļ���������", Toast.LENGTH_LONG)
						.show();
			}
			// ...
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// ��ȨKey����
				Toast.makeText(mapContext, "��ȨKey����", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * ��������������λ�õ�ʱ�򣬸�ʽ�����ַ������������Ļ��
	 */
	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			Log.e("LOC", "==MyLocationListenner==");
			Log.e("LOC", "==MyLocationListenner==");
			Log.e("LOC", "==MyLocationListenner==");
			Log.e("LOC", "==MyLocationListenner==");
			
			
			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.direction = 2.0f;
			locData.accuracy = location.getRadius();
			// locData.direction = location.getDerect();
			Log.d("loctest",
					String.format("before: lat: %f lon: %f",
							location.getLatitude(), location.getLongitude()));
			// GeoPoint p = CoordinateConver.fromGcjToBaidu(new
			// GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *
			// 1e6)));
			// Log.d("loctest",String.format("before: lat: %d lon: %d",
			// p.getLatitudeE6(),p.getLongitudeE6()));
			myLocationOverlay.setData(locData);
			mMapView.refresh();
			mMapController.animateTo(new GeoPoint(
					(int) (locData.latitude * 1e6),
					(int) (locData.longitude * 1e6)), null);
		}

		public void onReceivePoi(BDLocation poiLocation) {
			if (poiLocation == null) {
				return;
			}
		}
	}

	public class NotifyLister extends BDNotifyListener {
		public void onNotify(BDLocation mlocation, float distance) {
		}
	}

	/****************************************************************************/

}
