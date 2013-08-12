package com.np.enthernet;

import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 *  public static final int ETHERNET_STATE_UNKNOWN = 0;
    public static final int ETHERNET_STATE_DISABLED = 1;
    public static final int ETHERNET_STATE_ENABLED = 2;
 */
public class NpehernetinpokeActivity extends Activity {

	private String TAG = "debug";
	private Button bton;
	private Button btoff;
	Thread StateListen;
	Boolean flag = false;
	private BroadcastReceiver mReceiver;
    TextView tv;
	Nptools tool;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		bton = (Button) findViewById(R.id.bton);
		btoff = (Button) findViewById(R.id.btoff);
		tv=(TextView)findViewById(R.id.view);

		tool = new Nptools();

		btoff.setVisibility(View.INVISIBLE);

		bton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("debug", "以太网打开");
				tool.doInit(getApplicationContext());
				bton.setVisibility(View.INVISIBLE);
				btoff.setVisibility(View.VISIBLE);

			}
		});

		btoff.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.i("debug", "以太网服务关闭");
				tool.doExit(getApplicationContext());
				bton.setVisibility(View.VISIBLE);
				btoff.setVisibility(View.INVISIBLE);

			}
		});

		StateListen = new Thread() {

			public void run() {
				while (!flag) {
                    int state=tool.StatusEth(getApplicationContext());
					Log.i("debug",
							"以太网服务状态： "
									+ state);
					Message message = new Message();
					message.what = 1;
					message.arg1=state;
					handler.sendMessage(message);
					SystemClock.sleep(1 * 1000);

				}

			}

		};

		StateListen.start();

		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				if (action.equals("eth_ip_dhcp_start")) {
					Toast.makeText(NpehernetinpokeActivity.this,
							R.string.eth_static_ip_succ, Toast.LENGTH_SHORT)
							.show();
				}
				if (action.equals("eth_ip_conf_success")) {

					Toast.makeText(NpehernetinpokeActivity.this,
							R.string.eth_static_ip_succ, Toast.LENGTH_SHORT)
							.show();
				}
				if (action.equals("eth_ip_conf_failed")) {

					Toast.makeText(NpehernetinpokeActivity.this,
							R.string.eth_static_ip_fail, Toast.LENGTH_SHORT)
							.show();
				}
				if (action.equals("eth_dhcp_wire_out")) {
					Toast.makeText(NpehernetinpokeActivity.this,
							R.string.ethernet_net_down, Toast.LENGTH_SHORT)
							.show();
				}
				if (action.equals("eth_report_iface_up")) {
					Toast.makeText(NpehernetinpokeActivity.this,
							R.string.ethernet_net_up, Toast.LENGTH_SHORT)
							.show();
				}
				if (action.equals("eth_ip_dhcp_failed")) {

					Toast.makeText(NpehernetinpokeActivity.this,
							R.string.eth_ip_dhcp_failed, Toast.LENGTH_SHORT)
							.show();
				}
				if (action.equals("eth_ip_dhcp_success")) {

					Toast.makeText(NpehernetinpokeActivity.this,
							R.string.eth_ip_dhcp_success, Toast.LENGTH_SHORT)
							.show();
				}
				if (action.equals("eth_ip_conf_success_but_no_hw")) {

					Toast.makeText(NpehernetinpokeActivity.this,
							R.string.eth_ip_conf_success_but_no_hw,
							Toast.LENGTH_SHORT).show();

				}

			}
		};
		IntentFilter filter = new IntentFilter();

		filter.addAction("eth_ip_conf_success");
		filter.addAction("eth_ip_conf_failed");
		filter.addAction("eth_dhcp_wire_out");
		filter.addAction("eth_report_iface_up");
		filter.addAction("eth_ip_dhcp_success");
		filter.addAction("eth_ip_dhcp_failed");
		filter.addAction("eth_ip_dhcp_start");// jyq12.28
		filter.addAction("eth_ip_conf_success_but_no_hw");
		NpehernetinpokeActivity.this.registerReceiver(mReceiver, filter);

	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 * 
	 * 
	 * UI以及状态更新
	 */
	private Handler handler = new Handler() {
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss  ");       		
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 1:
					Date curDate= new Date(System.currentTimeMillis());//获取当前时间 
					String    str    =    formatter.format(curDate); 
					switch (msg.arg1) {
					case 0:
						tv.setText(" 以太网状态未知"+" 时间："+str);
						break;
                    case 1:
                    	tv.setText(" 以太网关闭了"+" 时间："+str);
						break;
                     case 2:
                    	 tv.setText(" 以太网开启"+" 时间："+str);
						break;
					default:
						tv.setText(" 无法获取以太网状态"+" 时间："+str);
						break;
					}
					
					break;

				default:
					break;
				}
			}
		}

	};
	
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "destroy");
		unregisterReceiver(mReceiver);

		if (StateListen != null) {
			flag = true;
			StateListen = null;

		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "resume");
		
		int status=tool.StatusEth(getApplicationContext());
		if(status==1||status==0){//以太网已经关闭
			
			bton.setVisibility(View.VISIBLE);
			btoff.setVisibility(View.INVISIBLE);
		}else if(status==2){//以太网已经开启
			
			bton.setVisibility(View.INVISIBLE);
			btoff.setVisibility(View.VISIBLE);
			
		}else if(status==-1){//以太网未知
			
			bton.setVisibility(View.INVISIBLE);
			btoff.setVisibility(View.INVISIBLE);
			
			
		}
		

	}

}