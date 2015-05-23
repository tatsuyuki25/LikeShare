package com.likeshare.camera;


import java.io.IOException;
import com.likeshare.net.LikeShareService;
import com.likeshare.net.LikeShareService.LikeShareServiceBinder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;


public class CameraTransfer_Service extends Service implements SensorEventListener {

	private float currentAcceleration = 0;
	private float maxAcceleration = 0;

	boolean off = true;// 只做一次 設定路徑用

	private SensorManager sensorManager;
	public Handler handler;
	private static boolean sensorSwitch = true;	
	private static String filePath="";
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);// 註冊sensor
		
		// 註冊監聽拍照廣播
		IntentFilter filter = new IntentFilter();
		filter.addAction(Camera.ACTION_NEW_PICTURE);
		filter.addAction(Camera.ACTION_NEW_VIDEO);
		try {
			filter.addDataType("image/*");
			filter.addDataType("video/*");

		} catch (MalformedMimeTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(receiver, filter);

		doBindService();
		sensorSwitch = true;
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.arg1) {
				case 1: // 甩動事件
					
					try {
						if(filePath!="")
							lss.connectImage(lss.getDefaultDevice(),filePath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Toast.makeText(CameraTransfer_Service.this,
								"Transfer error!",
								Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
					sensorSwitch = true;
					stopGSensor();
					delay();
					break;
				case 2: // 拍照事件
					startGSensor();
					Toast.makeText(CameraTransfer_Service.this,
							"Shake transfer!",
							Toast.LENGTH_LONG).show();
					break;
				}
			}
		};
	}

	/**
	 * 廣播
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.e(">>>>>>>", intent.getData() + "");

		/*	if (intent.getAction() == android.hardware.Camera.ACTION_NEW_PICTURE) {
				Toast.makeText(CameraTransfer_Service.this,
						getRealPathFromURI(intent.getData()), Toast.LENGTH_LONG)
						.show();
			} else if (intent.getAction() == android.hardware.Camera.ACTION_NEW_VIDEO) {
				Toast.makeText(CameraTransfer_Service.this,
						"VIDEO:" + getRealPathFromURI(intent.getData()),
						Toast.LENGTH_LONG).show();
			}*/
			if (intent.getAction()== android.hardware.Camera.ACTION_NEW_PICTURE||
					intent.getAction() == android.hardware.Camera.ACTION_NEW_VIDEO	)
			{
				filePath=getRealPathFromURI(intent.getData());
				Message msg = handler.obtainMessage();
				msg.arg1 = 2;
				msg.sendToTarget();
			}
			

		}

	};

	/**
	 * URi 轉路徑
	 * 
	 * @param contentUri
	 * @return
	 */
	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(CameraTransfer_Service.this,
				contentUri, proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * 消除Sensor連續反映
	 */
	private void delay() // 消除延遲
	{
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startGSensor() {

		Sensor accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// 感測G力
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);// 註冊事件
	}

	private void stopGSensor() {
		sensorManager.unregisterListener(this);
	}

	/*------------------偵測G sensor事件----------------*/

	@Override
	public void onSensorChanged(SensorEvent event) {
		// 系統設置的重力加速度標準值，設備在水平靜止的情況下就承受這個壓力，所以默認Y軸方向的加速度值為STANDARD_GRAVITY
		double calibration = SensorManager.STANDARD_GRAVITY;
		// TODO Auto-generated method stub
		double x = event.values[0];
		double y = event.values[1];
		double z = event.values[2];
		// 計算三個方向的加速度
		double a = Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)
				+ Math.pow(z, 2)));

		// 消去原有的重力引起的壓力

		currentAcceleration = Math.abs((float) (a - calibration));
		float tmp = currentAcceleration - maxAcceleration;
		if (tmp > 10 && sensorSwitch) {
			sensorSwitch = false;
			Message msgNowCompletion = handler.obtainMessage();
			msgNowCompletion.arg1 = 1;
			msgNowCompletion.sendToTarget();

		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	
	/* -------- Service --------- */
	/* Service */
	private void doBindService() {

		bindService(new Intent(CameraTransfer_Service.this, LikeShareService.class),
				ssserviceconnection, Context.BIND_AUTO_CREATE);
	}

	private LikeShareService lss;
	private ServiceConnection ssserviceconnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			LikeShareServiceBinder fsb = (LikeShareServiceBinder) service;
			lss = (LikeShareService) fsb.getService();
		};

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			lss = null;
		};
	};
	// -------------- Service math --------------//
	private final CameraTransfer_ServiceBinder MyCameraTransfer_ServiceBinder = new CameraTransfer_ServiceBinder();

	public class CameraTransfer_ServiceBinder extends Binder {
		public Service getService() {
			return CameraTransfer_Service.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return MyCameraTransfer_ServiceBinder;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		stopGSensor();
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

}
