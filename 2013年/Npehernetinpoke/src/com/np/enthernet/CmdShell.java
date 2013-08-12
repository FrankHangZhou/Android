package com.np.enthernet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

public class CmdShell {
	
	
	
	
 	private boolean execCommand(String paramString) {
		String str = null;
		try {

			final Process localProcess = Runtime.getRuntime().exec(paramString);

			InputStream localInputStream = localProcess.getInputStream();
			InputStreamReader localInputStreamReader = new InputStreamReader(localInputStream);
			BufferedReader localBufferedReader = new BufferedReader(localInputStreamReader, 8192);
			String[] arrayOfString = new String[10];
			Thread toThread = new Thread() {
				public void run() {
					long tm = System.currentTimeMillis();
					while (System.currentTimeMillis() - tm < 5000) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					localProcess.destroy();
				}
			};
			toThread.start();

			int i = 0;
			while (true) {
				str = localBufferedReader.readLine();
				Log.i("debug",": debug           "+str);
				if (str == null) {
					localBufferedReader.close();
					return true;
				}
				arrayOfString[i] = str;
				i++;
			}
		} catch (Exception localIOException) {
			Log.i("debug", localIOException.getMessage());
		}
		return false;
	}

}
