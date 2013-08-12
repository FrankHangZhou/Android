package com.lyt.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.pm.PackageStats;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;


/*
 * Frank@author
 * LOCALSOCKET操作�?
 * 封装了收发连接关闭LOCALSOCKET功能
 */
public class Docommands {

	private static final String TAG = "commond";// for test
	InputStream mIn; // 全局
	OutputStream mOut; // 全局
	LocalSocket mSocket;// 全局


	byte buf[] = new byte[1024];
	int buflen = 0;
    
	/*
	 * 考虑有些SOKET阻塞等异�?dis之后重新收发命令，需�?��是否Socket还在
	 * 当什么时候会出现呢？
	 * 1，发下的命令底层没有获取buf数据，目前底层由于保持实时�?，我采用的是
	 *   read阻塞。所以一旦出现连续的buf读阻塞�?则会卡死。这时�?我们就需要判断了�?
	 * 2，当然，如果你下发的命令buf带数据的 则没有关系了�?
	 */
	public boolean connect() {
		if (mSocket != null) {

			return true;
		}
		Log.i(TAG, "Docommands--new connect11");
		try {
			// 创建socket
			mSocket = new LocalSocket();
			// 设置连接地址
			LocalSocketAddress address = new LocalSocketAddress(
					"getStream", LocalSocketAddress.Namespace.RESERVED);
			// 建立连接
			mSocket.connect(address);
			mSocket.setReceiveBufferSize(1000);
			mSocket.setSendBufferSize(1000);
			mSocket.setSoTimeout(30000);   //设置读超时如�?0S没有数据返回，报出读异常（�?虑TCPIP拍照登陆返回并不是很快）
			mIn = mSocket.getInputStream();// 获取数据输入�?可以读数�?
			mOut = mSocket.getOutputStream();// 获取数据输出�?可以写数�?
		} catch (IOException ex) {
			Log.i(TAG, "connecting..." + ex);// 打印出异�?
			disconnect();
			return false;
		}
		return true;
	}

	public void disconnect() {
		Log.i(TAG, "Docommands--disconnecting...");

		try {
			if (mIn != null)
				mIn.close();
		} catch (IOException ex) {
		}
		try {
			if (mOut != null)
				mOut.close();
		} catch (IOException ex) {
		}

		try {
			if (mSocket != null)
				mSocket.close();
		} catch (IOException ex) {
		}
		mSocket = null;
		mIn = null;
		mOut = null;
	}

	public boolean readBytes(byte buffer[], int len) {
		int off = 0, count;
		if (len < 0)
			return false;
		while (off != len) {
			try {
				count = mIn.read(buffer, off, len - off);
				if (count <= 0) {
					Log.e(TAG, "Docommands--read error " + count);
					break;
				}
				off += count;
			} catch (IOException ex) {
				Log.e(TAG, "Docommands--read exception" + ex);
				execCommand("killprocess");
				break;
			}
		}
		Log.i(TAG, "Docommands--read " + len + " bytes" + "off" + off);
		if (off == len)
			return true;
		disconnect();
		return false;
	}

	public boolean readReply() {
		int len;
		buflen = 0;
		if (!readBytes(buf, 2))// for char oldreadBytes(buf, 2)
			return false;
		len = (((int) buf[0]) & 0xff) | ((((int) buf[1]) & 0xff) << 8);
		if ((len < 1) || (len > 1024)) {
			Log.e(TAG, "invalid reply length (" + len + ")");
			disconnect();
			return false;
		}
		Log.i(TAG, "readReply--len " + len);
		if (!readBytes(buf, len))
			return false;
		buflen = len;
		String re = new String(buf, 0, len);
		Log.e(TAG, "valid reply:   " + re);
		return true;
	}

	public boolean writeCommand(String _cmd) {
		byte[] cmd = _cmd.getBytes();
		int len = cmd.length;
		if ((len < 1) || (len > 1024))
			return false;
		buf[0] = (byte) (len & 0xff);
		buf[1] = (byte) ((len >> 8) & 0xff);
		try {
			mOut.write(buf, 0, 2);
			mOut.write(cmd, 0, len);
		} catch (IOException ex) {
			Log.e(TAG, "write error");
			disconnect();
			return false;
		}
		return true;
	}

	public synchronized String transaction(String cmd) {
		 Log.e(TAG, "Docommands--transaction");

		 if (!connect()) {//加入链接没链接成功时候的重试，避免空指针
		   Log.e(TAG, "Docommands--connection failed");
		 return "-1";
		 }

		if (!writeCommand(cmd)) {
			/*
			 * If getStream died and restarted in the background (unlikely but
			 * possible) we'll fail on the next write (this one). Try to
			 * reconnect and write the command one more time before giving up.
			 */
			Log.e(TAG, "Docommands--write command failed? reconnect!");
			return "-1";
		}
		Log.i(TAG, "send: '" + cmd + "'");
		if (readReply()) {
			String s = new String(buf, 0, buflen);
			Log.i(TAG, "recv: '" + s + "'");

			return s;
		} else {
			Log.i(TAG, "fail");

			return "-1";
		}
	}

	public String execute(String cmd) {
		String res = transaction(cmd);
		return res;// frank

	}

	
	/*
	 * 执行�?��，命令脚本我已经封装成killprocess
	 */

	public static synchronized String execCommand(String command) {
		Process process = null;
		DataOutputStream os = null;
		String line = "";
		StringBuilder sb = new StringBuilder(line);
		try {
			process = Runtime.getRuntime().exec("su");

			InputStream inputstream = process.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(
					inputstream);
			BufferedReader bufferedreader = new BufferedReader(
					inputstreamreader);

			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			while ((line = bufferedreader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			process.waitFor();
		} catch (Exception e) {
			Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "
					+ e.getMessage());

		}

		return sb.toString();

	}


}
