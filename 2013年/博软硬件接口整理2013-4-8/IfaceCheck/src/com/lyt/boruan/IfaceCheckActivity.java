package com.lyt.boruan;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IfaceCheckActivity extends Activity {

	private String TAG = "iface";
	Button button;
	Button button2;
	Button button3;
	TextView view;
	
	Context mcontext;
	Interface iface = new Interface();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		view=(TextView)findViewById(R.id.textview);
		// 返回值说明：1，返回-1 打开错误；2，返回 -2表示硬件不支持或者未上电;3，返回1表示打开指纹设备
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				view.setText("状态提示：");
				button.setVisibility(View.INVISIBLE);
				
				new AsyncTask<Void, Void, Integer>() {
					@Override
					protected Integer doInBackground(Void... args) {
						int status = iface.Zhiwencheck();
						Log.i(TAG, "" + status);
						return status;
					}

					@Override
					protected void onPostExecute(Integer result) {
						Log.i(TAG, "result " + result);
						if (result == -2) {
							view.setText("状态提示：指纹硬件不支持");
						} else if (result == -1) {
							view.setText("状态提示：指纹硬件支持打开失败重试");
						} else if (result == 1) {
							view.setText("状态提示：指纹硬件支持正常");

						}
						
						button.setVisibility(View.VISIBLE);
				
					}
				}.execute();
				
				
				
				
				
				
				
				
				
								
				
				

			}
		});
		//返回值说明：1，返回-1表示硬件不支持或无硬件；2，返回-2表示无卡；3，返回1表示读到卡的UID
		button2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				view.setText("状态提示：");
				button2.setVisibility(View.INVISIBLE);
				
				new AsyncTask<Void, Void, Integer>() {
					@Override
					protected Integer doInBackground(Void... args) {
						int status=iface.Rfidcheck();
						Log.i(TAG, "RFID " + status);
						return status;
					}

					@Override
					protected void onPostExecute(Integer result) {
						Log.i(TAG, "result " + result);
						if (result == -1) {
							view.setText("状态提示：RFID硬件不支持");
						} else if (result == -2) {
							view.setText("状态提示：RFID硬件支持但是没插入卡");
						} else if (result == 1) {
							view.setText("状态提示：RFID硬件支持正常");
						}
						
						button2.setVisibility(View.VISIBLE);
				
					}
				}.execute();
							
				
				
			}
		});
  //返回值类型： 返回1 表示GPS硬件支持，返回-1表示GPS硬件不支持
		button3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
               
				view.setText("状态提示：");
				button3.setVisibility(View.INVISIBLE);
				
				new AsyncTask<Void, Void, Integer>() {
					@Override
					protected Integer doInBackground(Void... args) {
						int status=iface.Gpscheck();
						Log.i(TAG, "GPS " + status);
						return status;
					}

					@Override
					protected void onPostExecute(Integer result) {
						Log.i(TAG, "GPS result " + result);
						if (result == -1) {
							view.setText("状态提示：GPS硬件不支持");
						} else if (result == 1) {
							view.setText("状态提示：GPS硬件支持正常");
						}
						
						button3.setVisibility(View.VISIBLE);
				
					}
				}.execute();
							
				
				
				
				
			}
		});

	}
}