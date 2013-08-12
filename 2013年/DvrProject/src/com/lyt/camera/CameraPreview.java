package com.lyt.camera;

import java.io.IOException;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {

	/** LOG标识 */
	private static final String TAG = "preview";

	/** 分辨�?*/
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;

	/** 监听接口 */
	private OnCameraStatusListener listener;

	private SurfaceHolder holder;
	private Camera camera;

	// 创建�?��PictureCallback对象，并实现其中的onPictureTaken方法
	private PictureCallback pictureCallback = new PictureCallback() {

		// 该方法用于处理拍摄后的照片数�?
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			// 停止照片拍摄
			camera.stopPreview();
			camera = null;

			// 调用结束事件
			if (null != listener) {
				listener.onCameraStopped(data);
			}
		}
	};

	// Preview类的构�?方法
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 获得SurfaceHolder对象
		holder = getHolder();
		// 指定用于捕捉拍照事件的SurfaceHolder.Callback对象
		holder.addCallback(this);
		// 设置SurfaceHolder对象的类�?
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	// 在surface创建时激�?
	public void surfaceCreated(SurfaceHolder holder) {
		 Log.e(TAG, "==surfaceCreated==");
		// 获得Camera对象
		camera = Camera.open();
		try {
			// 设置用于显示拍照摄像的SurfaceHolder对象
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
			// 释放手机摄像�?
			camera.release();
			camera = null;
		}
	}

	// 在surface�?��时激�?
	public void surfaceDestroyed(SurfaceHolder holder) {
		 Log.e(TAG, "==surfaceDestroyed==");
		// 释放手机摄像�?
		camera.release();
	}

	// 在surface的大小发生改变时�?��
	public void surfaceChanged(final SurfaceHolder holder, int format, int w,
			int h) {
		 Log.e(TAG, "==surfaceChanged==");
		try {
			// 获取照相机参�?
			Camera.Parameters parameters = camera.getParameters();
			// 设置照片格式
			parameters.setPictureFormat(PixelFormat.JPEG);
			// 设置预浏尺寸
			parameters.setPreviewSize(480, 320);
			// 设置照片分辨�?
			parameters.setPictureSize(640, 480);
			// 设置照相机参�?
			camera.setParameters(parameters);
			// �?��拍照
			camera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//恢复预览
	public void restartpreview(){
		//try {
			// 获取照相机参�?
			//Camera.Parameters parameters = camera.getParameters();
			// 设置照片格式
		//	parameters.setPictureFormat(PixelFormat.JPEG);
			// 设置预浏尺寸
			//parameters.setPreviewSize(480, 320);
			// 设置照片分辨�?
			//parameters.setPictureSize(720, 576);
			// 设置照相机参�?
			//camera.setParameters(parameters);
			// �?��拍照
			camera.startPreview();
	//	} catch (Exception e) {
		//	e.printStackTrace();
		//}
	}

	// 停止拍照，并将拍摄的照片传入PictureCallback接口的onPictureTaken方法
	public void takePicture() {
		camera.takePicture(null, null, pictureCallback);
	}

	// 设置监听事件
	public void setOnCameraStatusListener(OnCameraStatusListener listener) {
		this.listener = listener;
	}

	/**
	 * 相机拍照监听接口
	 */
	public interface OnCameraStatusListener {

		// 相机拍照结束事件
		void onCameraStopped(byte[] data);
	}

}