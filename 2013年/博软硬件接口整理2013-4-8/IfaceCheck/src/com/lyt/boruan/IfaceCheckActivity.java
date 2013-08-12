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
		// ����ֵ˵����1������-1 �򿪴���2������ -2��ʾӲ����֧�ֻ���δ�ϵ�;3������1��ʾ��ָ���豸
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				view.setText("״̬��ʾ��");
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
							view.setText("״̬��ʾ��ָ��Ӳ����֧��");
						} else if (result == -1) {
							view.setText("״̬��ʾ��ָ��Ӳ��֧�ִ�ʧ������");
						} else if (result == 1) {
							view.setText("״̬��ʾ��ָ��Ӳ��֧������");

						}
						
						button.setVisibility(View.VISIBLE);
				
					}
				}.execute();
				
				
				
				
				
				
				
				
				
								
				
				

			}
		});
		//����ֵ˵����1������-1��ʾӲ����֧�ֻ���Ӳ����2������-2��ʾ�޿���3������1��ʾ��������UID
		button2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				view.setText("״̬��ʾ��");
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
							view.setText("״̬��ʾ��RFIDӲ����֧��");
						} else if (result == -2) {
							view.setText("״̬��ʾ��RFIDӲ��֧�ֵ���û���뿨");
						} else if (result == 1) {
							view.setText("״̬��ʾ��RFIDӲ��֧������");
						}
						
						button2.setVisibility(View.VISIBLE);
				
					}
				}.execute();
							
				
				
			}
		});
  //����ֵ���ͣ� ����1 ��ʾGPSӲ��֧�֣�����-1��ʾGPSӲ����֧��
		button3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
               
				view.setText("״̬��ʾ��");
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
							view.setText("״̬��ʾ��GPSӲ����֧��");
						} else if (result == 1) {
							view.setText("״̬��ʾ��GPSӲ��֧������");
						}
						
						button3.setVisibility(View.VISIBLE);
				
					}
				}.execute();
							
				
				
				
				
			}
		});

	}
}