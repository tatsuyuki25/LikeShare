package com.likeshare.video;

import java.io.IOException;

import com.likeshare.R;
import com.likeshare.net.LikeShareService;
import com.likeshare.net.LikeShareService.LikeShareServiceBinder;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayActivity extends Activity implements
		MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
	public static final String TAG = "VideoPlayer";
	private VideoView mVideoView;
	private Uri mUri;
	private int mPositionWhenPaused = -1;
	private MediaController mMediaController;
	private WakeLock m_wklk;
	private String path;
	private SensorManager sensorManager;
	private float maxAcceleration;
	private float currentAcceleration;
	private boolean sw = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		
		doBindService();
		PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
		m_wklk = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "tw");
		m_wklk.acquire();
		Bundle data = getIntent().getExtras();
		path = data.getString("path"); 
		mVideoView = (VideoView) findViewById(R.id.videoview);
		// 文件路径
		mUri = Uri.parse(path);
		// Create media controller
		mMediaController = new MediaController(this);
		// 设置MediaController
		mVideoView.setMediaController(mMediaController);
	}


	// 监听MediaPlayer上报的错误信息
	@Override
	public boolean onError(MediaPlayer mp,int what,int extra)
	{
		// TODO Auto-generated method stub
		return false;
	}

	// Video播完的时候得到通知
	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.finish();
	}

	// 开始
	@Override
	public void onStart()
	{
		// Play Video
		mVideoView.setVideoURI(mUri);
		mVideoView.start();
		super.onStart();
	}

	// 暂停
	@Override
	public void onPause()
	{
		// Stop video when the activity is pause.
		mPositionWhenPaused = mVideoView.getCurrentPosition();
		mVideoView.stopPlayback();
		Log.d(TAG,"OnStop: mPositionWhenPaused = " + mPositionWhenPaused);
		Log.d(TAG,"OnStop: getDuration  = " + mVideoView.getDuration());
		m_wklk.release();
		sensorManager.unregisterListener(sensorEventListener);
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		// Resume video player
		if(mPositionWhenPaused >= 0)
		{
			mVideoView.seekTo(mPositionWhenPaused);
			mPositionWhenPaused = -1;
		}
		m_wklk.acquire();
		setGSensor();
		super.onResume();
	}
	
	private void setGSensor()
	{
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 註冊sensor
		Sensor accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// 感測G力
		sensorManager.registerListener(sensorEventListener,accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);// 註冊事件
	}

	/*------------------偵測G sensor事件----------------*/
	private final SensorEventListener sensorEventListener = new SensorEventListener()
	{
		// 系統設置的重力加速度標準值，設備在水平靜止的情況下就承受這個壓力，所以默認Y軸方向的加速度值為STANDARD_GRAVITY
		double calibration = SensorManager.STANDARD_GRAVITY;

		public void onAccuracyChanged(Sensor sensor,int accuracy)
		{
		}

		public void onSensorChanged(SensorEvent event)
		{
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			// 計算三個方向的加速度
			double a = Math.round(Math.sqrt(Math.pow(x,2) + Math.pow(y,2)
					+ Math.pow(z,2)));

			// 消去原有的重力引起的壓力
			currentAcceleration = Math.abs((float) (a - calibration));
			float tmp = currentAcceleration - maxAcceleration;
			if(tmp > 5.5 && sw)
			{
				sw = false;
				mVideoView.pause();
				int cp = mVideoView.getCurrentPosition();
				setResult(RESULT_OK);
				PlayActivity.this.finish();
				try
				{
					lss.connectVideo(lss.getDefaultDevice(),path,cp);
				} catch(IOException e)
				{
					e.printStackTrace();
				}
			}
			else
				sw = true;
			maxAcceleration = currentAcceleration;
		}
	};
	
	/* Service */
	private void doBindService() {

		bindService(new Intent(PlayActivity.this, LikeShareService.class),
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
}
