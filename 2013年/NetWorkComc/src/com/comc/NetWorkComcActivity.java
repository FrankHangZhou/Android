package com.comc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.TextView;

public class NetWorkComcActivity extends Activity {

	Context mcontext;
    private boolean bRecording;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mcontext = getApplicationContext();
		TextView view = (TextView) findViewById(R.id.view);
		view.setText(String.valueOf(checkNet(mcontext)));

	}

	/*
	 * ÄÚ²¿Àà
	 */
	private static boolean checkNet(Context context) {

		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {

				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {

					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	
	
	private Runnable threadStartRec = new Runnable() {

		public void run() {
			// TODO Auto-generated method stub

			// start record
			int minBuffSize = AudioRecord.getMinBufferSize(8000,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			AudioRecord mAudioRecorder = new AudioRecord(
					MediaRecorder.AudioSource.MIC, 8000,
					// AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minBuffSize * 3);
			mAudioRecorder.startRecording();

			byte[] mBuffer = new byte[minBuffSize * 3];


			int len = 0;
			File fw = new File("/sdcard/save.pcm");
			FileOutputStream fisWriter = null;

			try {
				fw.createNewFile();
				fisWriter = new FileOutputStream(fw);
				// FileInputStream fisReader = new FileInputStream (fw);

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (bRecording) {
				len = 0;
				len = mAudioRecorder.read(mBuffer, 0, minBuffSize);
				// write into file
				// if(len>0&&len<=minBuffSize)
				{
					//
					try {
						fisWriter.write(mBuffer);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block

					e.printStackTrace();
				}
			}// end of while
			try {
				fisWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}// end of Run
	};

}