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

	boolean off = true;// �u���@�� �]�w���|��

	private SensorManager sensorManager;
	public Handler handler;
	private static boolean sensorSwitch = true;	
	private static String filePath="";
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);// ���Usensor
		
		// ���U��ť��Ӽs��
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
				case 1: // �ϰʨƥ�
					
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
				case 2: // ��Өƥ�
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
	 * �s��
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
	 * URi ����|
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
	 * ����Sensor�s��ϬM
	 */
	private void delay() // ��������
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
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// �P��G�O
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);// ���U�ƥ�
	}

	private void stopGSensor() {
		sensorManager.unregisterListener(this);
	}

	/*------------------����G sensor�ƥ�----------------*/

	@Override
	public void onSensorChanged(SensorEvent event) {
		// �t�γ]�m�����O�[�t�׼зǭȡA�]�Ʀb�����R����p�U�N�Ө��o�����O�A�ҥH�q�{Y�b��V���[�t�׭Ȭ�STANDARD_GRAVITY
		double calibration = SensorManager.STANDARD_GRAVITY;
		// TODO Auto-generated method stub
		double x = event.values[0];
		double y = event.values[1];
		double z = event.values[2];
		// �p��T�Ӥ�V���[�t��
		double a = Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)
				+ Math.pow(z, 2)));

		// ���h�즳�����O�ް_�����O

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
