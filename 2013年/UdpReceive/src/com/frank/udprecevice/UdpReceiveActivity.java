package com.frank.udprecevice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

public class UdpReceiveActivity extends Activity {

	TextView view;
	public String receivemess;
	ExecutorService exec;
	PowerManager.WakeLock mWakeLock;
	final Handler handler2 = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 0:
				view.setText("------------------------new");
				view.setText(null);
				break;
			case 1:
				// message
				view.setText(null);
				StringBuilder sb = new StringBuilder();
				Log.i("frankreceived", "update");
				sb.append("接收到字节数：" + receivemess.length()).append("\n\n");
				sb.append(receivemess);
				view.setText(sb.toString());
				sb = null;
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
		view = (TextView) findViewById(R.id.textView2);

		// 开启服务器
		exec = Executors.newCachedThreadPool();
		UDPServer server = new UDPServer();
		exec.execute(server);

		PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);

		mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FRANK");// 屏幕亮，不休眠
		mWakeLock.acquire();// 获得唤醒锁

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWakeLock.isHeld()) {
			Log.e("frankreceived", "release!!!!!");
			mWakeLock.release();

		}
		if (!exec.isShutdown()) {

			exec.shutdown();

		}
	}

	// UDP接收
	public class UDPServer implements Runnable {
		private static final int PORT = 9000;

		private byte[] msg = new byte[2 * 4 * 1024];

		private boolean life = true;

		public UDPServer() {
		}

		/**
		 * @return the life
		 */
		public boolean isLife() {
			return life;
		}

		/**
		 * @param life
		 *            the life to set
		 */
		public void setLife(boolean life) {
			this.life = life;
		}

		@Override
		public void run() {
			DatagramSocket dSocket = null;
			DatagramPacket dPacket = new DatagramPacket(msg, msg.length);
			try {
				dSocket = new DatagramSocket(PORT);

				while (life) {
					Log.i("frankreceived", "####");
					Message message1 = handler2.obtainMessage();
					message1.what = 0;
					handler2.sendMessage(message1);
					try {
						dSocket.receive(dPacket);

						receivemess = new String(dPacket.getData());
						Log.i("frankreceived", receivemess);
					} catch (IOException e) {
						e.printStackTrace();
					}
					// 更新UI
					Thread thread = new Thread() {
						@Override
						public void run() {
							Log.i("frankreceived", "thread");
							Message message = handler2.obtainMessage();
							message.what = 1;
							handler2.sendMessage(message);
						}
					};
					thread.start();
					thread = null;

				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
	}

}