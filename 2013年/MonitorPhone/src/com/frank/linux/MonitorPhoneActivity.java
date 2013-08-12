package com.frank.linux;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallTracker;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.cdma.CdmaCallTracker;


/*
 * Author@frank 2013-6-19
 * 后台电话以及挂断（不会开启界面以及弹出），返回电话的实时状态（此状态与域格模块的AT手册RIL一致）
 * 方法：直接绕过phoneAPP的界面，来拨号获取状态挂断。可以作为流氓软件应用。
 * 
 */
public class MonitorPhoneActivity extends Activity {

	TelephonyManager tm;
	Thread check;
	Boolean flage;
	Phone phonelocal;
	Button bt, btopen;
	EditText et1;
	TextView tv;
	ITelephony phone;
	TelephonyManager telephony;
	/*
	 * 状态全部根据域格AT手册1.24 指令CLCC当前电话状态来设置
	 */
	private Handler mphoneHandle = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1:
				if (phonelocal.getForegroundCall().getState() == Call.State.DIALING)
					tv.setText("DIALING.......发起呼叫，拨号状态");
				if (phonelocal.getForegroundCall().getState() == Call.State.ACTIVE)
					tv.setText("ACTIVE........激活状态");
				if (phonelocal.getForegroundCall().getState() == Call.State.INCOMING)
					tv.setText("INCOMING......来电振铃状态");
				if (phonelocal.getForegroundCall().getState() == Call.State.ALERTING)
					tv.setText("ALERTING......发起呼叫，振铃状态");
				if (phonelocal.getForegroundCall().getState() == Call.State.HOLDING)
					tv.setText("HOLDING...... 呼叫保持状态");
				if (phonelocal.getForegroundCall().getState() == Call.State.DISCONNECTED)
					tv.setText("DISCONNECTED...... 呼叫断开");
				break;
			case 2:
				Log.i("Frankcheck", "挂断");
				Closephonecall();

				break;

			default:
				break;
			}

		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		init();
		phonelocal = PhoneFactory.getDefaultPhone();
		telephony = (TelephonyManager) MonitorPhoneActivity.this
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {// 获取实例
			phone = PhoneUtils.getITelephony(telephony);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 关闭 注意必须用子线程去挂断
		bt.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message message = new Message();
				message.what = 2;
				mphoneHandle.sendMessage(message);
				bt.setVisibility(View.INVISIBLE);
				btopen.setVisibility(View.VISIBLE);
			}
		});
		// 拨打
		btopen.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				String number = "10000";
				if (et1.getText().toString().equals("")
						|| et1.getText().toString() == null) {
					Toast.makeText(getApplicationContext(), "请先输入电话号码~", 1)
							.show();
					return;
				} else {
					Log.i("Frankcheck", "et1.getText().toString():"
							+ et1.getText().toString());
					number = et1.getText().toString().trim();

				}
				MyCallPhone(number);
				btopen.setVisibility(View.INVISIBLE);
				bt.setVisibility(View.VISIBLE);
			}
		});

	}

	public void init() {
		bt = (Button) findViewById(R.id.bt0);// 关闭
		et1 = (EditText) findViewById(R.id.ed1);// 电话
		tv = (TextView) findViewById(R.id.ed2);// 状态
		btopen = (Button) findViewById(R.id.bt1);// 拨打电话
		bt.setVisibility(View.INVISIBLE);
	}

	public void MyCallPhone(String number) {
		try {

			Phone phonecal = PhoneFactory.getDefaultPhone();
			Log.i("Frankcheck", "phone:" + phonecal);
			phonecal.dial(number.trim());

		} catch (Exception e) {
			System.out.println(e);
		}
		check = new Thread() {
			public void run() {
				while (flage) {
					Log.i("Frankcheck", "this Runnable running ");
					System.out.println("Phone running state: "
							+ phonelocal.getForegroundCall().getState());

					Message message = new Message();
					message.what = 1;
					mphoneHandle.sendMessage(message);
					SystemClock.sleep(100);
				}

			}

		};
		Log.i("Frankcheck", "phone:666");
		flage = true;
		check.start();
	}

	public void Closephonecall() {

		new Thread() {

			public void run() {
				try {
					Log.i("Frankcheck", "current:  "
							+ Thread.currentThread().getId());
					phone.endCall();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}.start();

		Log.i("Frankcheck", "Closephonecall()");
		Log.i("Frankcheck", "Closephonecall()");
		Log.i("Frankcheck", "Closephonecall()");
	}

	protected void onDestory()// 
	{
		super.onDestroy();
		System.out.println("normal onPause() call");
		if (check != null) {
			check = null;
			flage = false;
		}

	}

	protected void onPause() {
		super.onPause();
		System.out.println("onPause");
		if (phonelocal.getForegroundCall().getState() == Call.State.DIALING
				|| phonelocal.getForegroundCall().getState() == Call.State.ACTIVE
				|| phonelocal.getForegroundCall().getState() == Call.State.ALERTING) {
			Closephonecall();

		}
		if (check != null) {
			check = null;
			flage = false;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		System.out.println("onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("onResume");
	}

}
