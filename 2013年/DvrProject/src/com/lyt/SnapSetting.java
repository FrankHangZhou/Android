package com.lyt;

/*
 * 封装拍照设置界面的配置
 */
public class SnapSetting {

	// 拍照状态
	private String status;
	// 拍照的模式
	private int model;
	// 分辨率的选择
	private int radio;
	// 自动拍照的间隔时间
	private String interneltime;
	// 是否通过3G上传
	private Boolean update;

	public String getstatus() {
		return status;
	}

	public void setstatus(String status) {
		this.status = status;
	}

	public int getmodel() {
		return model;
	}

	public void setmodel(int model) {
		this.model = model;
	}

	public int getradio() {
		return radio;
	}

	public void setradio(int radio) {
		this.radio = radio;
	}

	public String getinterneltime() {
		return interneltime;
	}

	public void setinterneltime(String interneltime) {
		this.interneltime = interneltime;
	}

	public Boolean getupdate() {
		return update;
	}

	public void setupdate(Boolean update) {
		this.update = update;
	}

}
