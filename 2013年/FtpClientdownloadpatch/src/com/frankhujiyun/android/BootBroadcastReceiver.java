package com.frankhujiyun.android;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.frankhuijiyun.android.util.MySystemProperties;
import com.frankhuijiyun.android.util.ZipUtils;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/*
 * 开机10S后（如果没有打开程序则打开）自动运行
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		Timer time;
		TimerTask task;
		mContext = context;

		// TODO Auto-generated method stub
		if (intent.getAction().equals(ACTION)) {

			task = new MyTimerTask();
			time = new Timer();
			time.schedule(task, 10 * 1000);// 10s

		}

	}

	class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i("download", "run...MyTimerTask delAllFile 2013-7-18");
			Log.i("download", "run...MyTimerTask delAllFile 2013-7-18");
			String savePAth = Environment.getExternalStorageDirectory() + "/DownFile";
			ZipUtils.delAllFile(savePAth);
			
			
			//这只之前开机开启应用的部分，考虑新中新更新不能让其每次开机开启应用，
			//我们需要对开机做处理，每次开机都检验删除标志位。应用让客户自己手动或应用去调用。这样不不会 每次弹出更新界面
			/*
			Log.i("download",
					"run...MyTimerTask:  " + MySystemProperties.get("Download"));
			Log.i("download", "run...MyTimerTask");
			Log.i("download", "run...MyTimerTask");
			ActivityManager am = (ActivityManager) mContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> list = am.getRunningTasks(100);
			boolean isAppRunning = false;
			String MY_PKG_NAME = "com.frankhujiyun.android";
			for (RunningTaskInfo info : list) {
				Log.i("download",
						"getPackageName:    "
								+ info.topActivity.getPackageName());
				Log.i("download", "getPackageName:    " + info.getClass());
				if (info.topActivity.getPackageName().equals(MY_PKG_NAME)
						|| info.baseActivity.getPackageName().equals(
								MY_PKG_NAME)) {
					isAppRunning = true;
					Log.i("download", info.topActivity.getPackageName()
							+ " info.baseActivity.getPackageName()="
							+ info.baseActivity.getPackageName());
					break;
				}
			}

			if (!isAppRunning) {
				if (MySystemProperties.get("service.download").equals("NoFirst")) {
					Log.i("download", "Do nothing............");
				} else {
					Log.i("download", "Do start");
					Intent download = new Intent(mContext,
							FtpClientdownloadpatchActivity.class);
					download.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(download);
				}
			}
			
			*/

		}

	}

}
