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
 * DVR照片拍摄以及录像功能
 * 注意拍照分为video以及DVR传过来的照片
 */
public class DvrProjectActivity extends Activity implements
		CameraPreview.OnCameraStatusListener {

	private String TAG = "dvr";
	public static final String PATH = Environment.getExternalStorageDirectory()
			.toString() + "/AndroidDvr/";// 存放田字格本地照片
	public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	private CameraPreview mCameraPreview;

	private SnapSetting snapinfo = null;

	private Timer mTimer = null;// for camera
	private TimerTask mTimerTask = null;

	// spinner 全局
	private int stores1;
	private int stores2;

	// 拍照状态
	private String snapflage;
	private boolean isGO;
	private CameraThread mCameaTread = null;

	protected static final int STOP = 1;
	protected static final int POSCHANGED = 2;
	protected static final int UPDATE = 3;
	TextView tv2;

	// 拍照对话框开启后默认的初始化图片
	private Uri uridefault;

	// 百度地图,定位相关
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
		// 设置横屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置全屏
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 百度地图定位相关
		/****************************************************************/
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init(BMAPKEY, new MyGeneralListener());
		mapContext = getApplicationContext();
		// 注意：请在用使用setContentView前初始化BMapManager对象，否则会报错
		/****************************************************************/

		setContentView(R.layout.main);
		// 提示加粗
		TextView tv = (TextView) findViewById(R.id.view2);
		TextPaint tp = tv.getPaint();
		tp.setFakeBoldText(true);

		// 主界面状态
		tv2 = (TextView) findViewById(R.id.view1);

		// 照相预览界面
		mCameraPreview = (CameraPreview) findViewById(R.id.preview);
		mCameraPreview.setOnCameraStatusListener(this);

		// 百度地图定位相关
		/****************************************************************/
		mMapView = (MapView) findViewById(R.id.bmapView);

        mMapController = mMapView.getController();
        
        initMapView();
        
        mLocClient = new LocationClient( this );
        mLocClient.registerLocationListener( myListener );
        
        //位置提醒相关代码
        mNotifyer = new NotifyLister();
        mNotifyer.SetNotifyLocation(30.298465083333333,120.129234
        		,3000,"bd09ll");//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
        mLocClient.registerNotify(mNotifyer);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        option.setCoorType("bd09ll");     //设置坐标类型
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
				// 设置拍照
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
				|| stores1 == 4) {// 田字格照片 退出的时候关闭线程
			if (mCameaTread != null) {
				isGO = false;
				mCameaTread = null;

			}
		}

		com.lyt.loading.MainActivity.Interface.LocketDisconnect();

		super.onDestroy();
	}

	// 主界面状态更新
	/***********************************************************************************************************/
	final Handler cwjHandler = new Handler();

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateviewstatus();
		}
	};

	public void updateviewstatus() {

		tv2.setText("当前状态：" + snapinfo.getstatus());

	}

	/************************************************************************************************************/
	/*
	 * 拍照设置部分 1，田字格是本地4路照片通道 2，单选是连续拍4路通道
	 */
	private void showsnapsetting() {
		final View config = View.inflate(this, R.layout.snap_setting, null);//
		final TextView statusview = (TextView) config
				.findViewById(R.id.statusview);
		final myImageView image1 = (myImageView) config
				.findViewById(R.id.iamge);

		// 注册广播用来回调照片实时更新
		final BroadcastReceiver rssiReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				final String action = intent.getAction();
				if (action.equals("videocameraupdate")) {

					// 状态
					statusview.setText("当前拍照状态：" + snapflage);

					Bundle bundle = intent.getExtras();
					// 获得照片uri
					Uri uri = Uri.parse(bundle.getString("uriStr"));
					Log.i(TAG, "intent " + uri);
					// 获得拍照时间
					long dateTaken = bundle.getLong("dateTaken");
					try {
						// 从媒体数据库获取该照片
						String[] proj = { MediaStore.Images.Media.DATA };

						Cursor cursor = managedQuery(uri, proj, null, null,
								null);

						int column_index = cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

						cursor.moveToFirst();
						// 最后根据索引值获取图片路径
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

					// 状态
					statusview.setText("当前拍照状态：" + snapinfo.getstatus());
					Bundle bundle = intent.getExtras();
					// 获得照片uri
					String path = bundle.getString("uriStr");
					Log.i(TAG, "================");
					Log.i(TAG, "                ");
					Log.i(TAG, "show dvr picture: " + path);
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 2;// 图片宽高都为原来的二分之一，即图片为原来的四分之一

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

		if (uridefault != null) {// 如果一开始就是有图片则更新

			try {
				Log.i(TAG, "uridefault  beginn " + uridefault);
				String[] proj = { MediaStore.Images.Media.DATA };

				Cursor cursor = managedQuery(uridefault, proj, null, null, null);

				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

				cursor.moveToFirst();
				// 最后根据索引值获取图片路径
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

		if (dvrpath != null) {// 如果一开始就是有图片则更新

			try {
				Log.i(TAG, "uridefault  beginn " + dvrpath);
				Log.i(TAG, "path ---" + dvrpath);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;// 图片宽高都为原来的二分之一，即图片为原来的四分之一

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
				stores1 = position;// 存储

			}

			public void onNothingSelected(AdapterView<?> parent) {
				showToast("Spinner1: unselected");
			}
		});

		// 分辨率
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
		b.setTitle("                            拍照设置");
		b.setCancelable(true);
		b.setView(config);
		// 保存并且退出
		Button saveandstart = (Button) config
				.findViewById(R.id.btn_saveandstart);
		saveandstart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// mCameraPreview.takePicture();//test
				// 记录拍照状态并开始拍照,只要点击了保存并且重新开始则需要重新开始？
				// 间隔时间
				EditText edit = (EditText) config.findViewById(R.id.EditText1);
				String time = edit.getText().toString();
				Log.i(TAG, "time:       " + time);

				// 拍照状态

				// 是否通过3G上传
				CheckBox check = (CheckBox) config
						.findViewById(R.id.radioButton1);

				snapinfo.setstatus(snapflage); // 状态
				snapinfo.setmodel(stores1); // 模式
				snapinfo.setradio(stores2); // 分辨率
				if (time != null && time.length() > 0) {// 间隔时间
					snapinfo.setinterneltime(time);
				} else {
					snapinfo.setinterneltime("1");// 默认一分钟
				}
				if (check.isChecked()) { // 是否通过3G上传
					snapinfo.setupdate(true);
				} else {
					snapinfo.setupdate(false);
				}

				if (stores1 == 0 || stores1 == 1 || stores1 == 2
						|| stores1 == 3 || stores1 == 4) {// 田字格照片或者DVR单通道拍照

					if (mCameaTread == null) {
						if (stores1 == 4)
							snapinfo.setstatus("正在拍照" + "模式：田字格"); // 设置拍照状态
						else
							snapinfo.setstatus("正在拍照" + "模式：通道" + stores1); // 设置拍照状态
						isGO = true;
						mCameaTread = new CameraThread();
						new Thread(mCameaTread).start();
					}

					// 更新主界面
					Thread t = new Thread() {
						public void run() {
							cwjHandler.post(mUpdateResults); // 高速UI线程可以更新结果了
						}
					};
					t.start();
				}

				if (mLowBatteryDialog != null) {
					mLowBatteryDialog.dismiss();
					// 取消广播
					unregisterReceiver(rssiReceiver);
				}

			}
		});
		// 停止
		Button stop = (Button) config.findViewById(R.id.btn_stop);
		stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLowBatteryDialog != null) {
					mLowBatteryDialog.dismiss();
					snapflage = "null";// 拍照状态
					snapinfo.setstatus(snapflage); // 状态
					// 取消广播
					unregisterReceiver(rssiReceiver);
				}
				if (stores1 == 1 || stores1 == 2 || stores1 == 3
						|| stores1 == 4 || stores1 == 0) {// 田字格照片
					if (mCameaTread != null) {
						isGO = false;
						mCameaTread = null;

					}

					// 更新主界面
					Thread t = new Thread() {
						public void run() {
							cwjHandler.post(mUpdateResults); // 高速UI线程可以更新结果了
						}
					};
					t.start();
				}

			}
		});
		// 取消
		Button cancel = (Button) config.findViewById(R.id.btn_cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mLowBatteryDialog != null) {
					mLowBatteryDialog.dismiss();
					// 取消广播
					unregisterReceiver(rssiReceiver);
				}

			}
		});

		AlertDialog d = b.create();
		// 设置大小位置
		Window dialogWindow = d.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.TOP);

		// 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
		// dialog.onWindowAttributesChanged(lp);
		dialogWindow.setAttributes(lp);

		d.show();
		d.getWindow().setLayout(600, 460);

		// 显示上一次的默认配置
		if (snapinfo != null) {

			Log.i(TAG, "displaygetstatus:             " + snapinfo.getstatus());
			Log.i(TAG,
					"displaygetinterneltime:       "
							+ snapinfo.getinterneltime());
			Log.i(TAG, "displaygetupdate:             " + snapinfo.getupdate());
			Log.i(TAG, "displaygetgetmodel:             " + snapinfo.getmodel());
			Log.i(TAG, "displaygetgetradio:             " + snapinfo.getradio());

			// 状态
			statusview.setText("当前拍照状态：" + snapinfo.getstatus());
			// 拍照间隔时间
			EditText edit = (EditText) config.findViewById(R.id.EditText1);
			edit.setText(snapinfo.getinterneltime());
			// 石头通过3G上传
			CheckBox check = (CheckBox) config.findViewById(R.id.radioButton1);
			if (snapinfo.getupdate() != null)
				check.setChecked(snapinfo.getupdate());

		}

		mLowBatteryDialog = d;

		if (mLowBatteryDialog != null) {// 如果对话框显示的时候注册
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

	// 后台定时拍照

	class CameraThread implements Runnable {

		int model;
		int flag = 0;
		int data;

		public void run() {
			model = snapinfo.getmodel();
			while (isGO) {
				if (model == 4) {// 田字格

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
								// 判断网络是否通
								Log.v(TAG, "checknetwork ");
								if (isNetworkAvailable(DvrProjectActivity.this)) {
									// 如果真则登陆测试,网络恢复
									com.lyt.loading.MainActivity.Interface
											.danreset();// 重启dan
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
							// 如果错误，则不进行处理
							Log.v(TAG, "NumberFormatException " + e);
						}

					}
					if (flag == 0) {// 拍DVR照片
						Log.v(TAG, "snap begin  whatpath " + model);
						dvrpath = snap(Integer.toString(model));
						Log.v(TAG, "show picture：    " + dvrpath);
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
	 * dvr拍照 参数 path值通道0~4
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

	/********************************* 本地相机 *****************************************************************/

	/**
	 * 存储图像并将信息添加入媒体数据库
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
	 * 相机拍照结束事件
	 */
	@Override
	public void onCameraStopped(byte[] data) {
		Log.e(TAG, "==onCameraStopped==");
		Log.e(TAG, "==onCameraStopped==");
		Log.e(TAG, "==onCameraStopped==");
		// 创建图像
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		// 系统时间
		long dateTaken = System.currentTimeMillis();
		// 图像名称
		String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken)
				.toString() + ".jpg";
		// 存储图像（PATH目录）
		Uri uri = insertImage(getContentResolver(), filename, dateTaken, PATH,
				filename, bitmap, data);
		uridefault = uri;// 更新
		snapflage = "正在拍照" + "模式：田字格";

		// 返回结果
		Intent intent = new Intent("videocameraupdate");
		intent.putExtra("uriStr", uri.toString());
		intent.putExtra("dateTaken", dateTaken);
		sendBroadcast(intent);
		mCameraPreview.restartpreview();

	}

	// 百度地图,定位相关
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

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	static class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(mapContext, "您的网络出错啦！", Toast.LENGTH_LONG)
						.show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(mapContext, "输入正确的检索条件！", Toast.LENGTH_LONG)
						.show();
			}
			// ...
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				// 授权Key错误：
				Toast.makeText(mapContext, "授权Key错误", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 监听函数，又新位置的时候，格式化成字符串，输出到屏幕中
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
