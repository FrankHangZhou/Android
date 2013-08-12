package com.lyt.communication;

/*
 * frank@author
 * 此类封装了所有要操作的接口�?
 * 譬如登陆，�?出，获取DVR信息�?
 */
public class face {
	Docommands dvrsocket = new Docommands();

	/*
	 * LOCALSOCKET连接
	 */
	public boolean LocketConnect() {
		return dvrsocket.connect();

	}

	/*
	 * LOCALSOCKET断开连接
	 */
	public void LocketDisconnect() {

		dvrsocket.disconnect();
		dvrsocket.execCommand("killprocess");

	}

	/*
	 * dan重启
	 */
	public void danreset() {

		dvrsocket.execCommand("killprocess");

	}

	/*
	 * 通信正常与否
	 */
	public String testconnection() {
		String connect = dvrsocket.execute("test 1 2");
		return connect;
	}

	/*
	 * 登陆
	 */
	public String login() {
		String login = dvrsocket.execute("login");
		return login;
	}

	/*
	 * �?��状�?
	 */
	public String logout() {
		String logout = dvrsocket.execute("logout");
		return logout;
	}

	/*
	 * 获取录像数据 录像时一个回调接口�?数据不传给上层，不然效率过低
	 */
	public String record() {
		String record = dvrsocket.execute("record");
		return record;
	}

	/*
	 * 获取某一通道的照片snapshot 参数path是表示参�? 1 2 3，注意这里要加保护！
	 */
	public String snap(String path) {
		String record = dvrsocket.execute("snap " + path);
		return record;
	}

}
