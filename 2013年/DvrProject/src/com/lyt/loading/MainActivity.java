package com.lyt.loading;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Toast;

import com.lyt.communication.*;
import com.lyt.*;

public class MainActivity extends Activity implements AnimationListener {

	private LoadingView main_imageview;
	private Animation alphaAnimation = null;
	private Class<?> nextUI = DvrProjectActivity.class;
	private String TAG = "loading";
	public static face Interface;
	String show = "";
	String showlogin = "";
	Timer time = null;
	TimerTask timetask = null;
	private boolean connectFlag = false;
	/*
	 * ��飺1������localsocket�Ƿ��� 2���������������ͨ���Ƿ�����
	 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: {
				// login��½dvr
				mylogin task = new mylogin();
				task.execute();
			}
				break;

			case 2: {
				main_imageview.stopAnim();
				Log.i(TAG, "sIndex");
				startActivity(new Intent(MainActivity.this, nextUI));
				MainActivity.this.finish();

			}
				break;

			case 3: {
				Toast.makeText(MainActivity.this, "���鵱ǰ���绷��", 1).show();
				main_imageview.stopAnim();
				Log.i(TAG, "error");
				MainActivity.this.finish();

			}
				break;

			}

			super.handleMessage(msg);
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// getWindow().setFormat(PixelFormat.TRANSLUCENT);//����
		super.onCreate(savedInstanceState);
		// ���ú���
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ����ȫ��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.loading);

		main_imageview = (LoadingView) findViewById(R.id.main_imageview);

		alphaAnimation = AnimationUtils.loadAnimation(this,
				R.anim.welcome_alpha);
		alphaAnimation.setFillEnabled(true); // ����Fill����
		alphaAnimation.setFillAfter(true); // ���ö��������һ֡�Ǳ�����View����
		main_imageview.setAnimation(alphaAnimation);
		alphaAnimation.setAnimationListener(this); // Ϊ�������ü���

		initLoadingImages();
		Interface = new face();

		time = new Timer();
		time.schedule(task, 3000, 500);

	}
	
	
	@Override
	public void onPause() {
		Log.v(TAG, "LocalSocketActivity-----------onPause ");
		super.onPause();
	}

	public void onDestroy() {
		if (time != null) {
			time.cancel();
			time = null;
		}
		Log.v(TAG, "LocalSocketActivity-----------onDestroy ");
		super.onDestroy();
	}

   //����back
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Log.i(TAG, "KEYCODE_BACK");
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	TimerTask task = new TimerTask() {
		public void run() {
			Log.v(TAG, "-----------TimerTask ");
			connectFlag = Interface.LocketConnect();
			Log.v(TAG, "-----------connectFlag " + connectFlag);
			if (connectFlag == true) {

				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
				if (time != null) {
					time.cancel();
					time = null;
				}

			}

		}

	};

	/*
	 * ��½�����е��ʱ�������й�ϵ����̨������
	 */
	class mylogin extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub
			// 1������localsocket�Ƿ�����
			Log.d(TAG, "doInBackground--check localsocket connection");
			if (testconnection().contains("2013-")
					&& isNetworkAvailable(getApplicationContext())) {// ͨ��OK����ô��½��
				String status = login();
				Log.d(TAG, "doInBackground--loginstatus " + status);
				return status;
			} else {
				Log.d(TAG, "doInBackground--login faild");
				return null;
			}

		}

		protected void onPostExecute(String result) {
			Log.d(TAG, "doInBackground--result:  " + result);
			if (result != null) {
				Message message = new Message();
				message.what = 2;
				handler.sendMessage(message);
			} else if (result == null) {
				Message message = new Message();
				message.what = 3;
				handler.sendMessage(message);

			}
		}

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

	/*
	 * ����ͨ���������
	 */
	private String testconnection() {
		show = Interface.testconnection();
		return show;
	}

	/*
	 * ��½
	 */
	private String login() {
		showlogin = Interface.login();
		return showlogin;

	}

	private void initLoadingImages() {
		int[] imageIds = new int[6];
		imageIds[0] = R.drawable.loader_frame_1;
		imageIds[1] = R.drawable.loader_frame_2;
		imageIds[2] = R.drawable.loader_frame_3;
		imageIds[3] = R.drawable.loader_frame_4;
		imageIds[4] = R.drawable.loader_frame_5;
		imageIds[5] = R.drawable.loader_frame_6;

		main_imageview.setImageIds(imageIds);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		new Thread() {

			@Override
			public void run() {
				main_imageview.startAnim();
			}
		}.start();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}
}