package com.frank.udpdemo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.util.Log;

public class UDPServer implements Runnable {
	private static final int PORT = 9000;

	private byte[] msg = new byte[4*1024];

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
				Log.i("frankreceived", "------------------------");
				try {
					dSocket.receive(dPacket);
					Log.i("frankreceived", new String(dPacket.getData()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}
