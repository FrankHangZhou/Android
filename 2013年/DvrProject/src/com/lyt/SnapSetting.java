package com.lyt;

/*
 * ��װ�������ý��������
 */
public class SnapSetting {

	// ����״̬
	private String status;
	// ���յ�ģʽ
	private int model;
	// �ֱ��ʵ�ѡ��
	private int radio;
	// �Զ����յļ��ʱ��
	private String interneltime;
	// �Ƿ�ͨ��3G�ϴ�
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
