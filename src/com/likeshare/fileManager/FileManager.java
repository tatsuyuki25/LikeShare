/*
 * 
 * 
 * */

package com.likeshare.fileManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.likeshare.LoginActivity;
import com.likeshare.R;

import com.likeshare.net.LikeShareService;
import com.likeshare.net.LikeShareService.LikeShareServiceBinder;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public class FileManager extends ListActivity implements
		OnItemLongClickListener, SensorEventListener {

	/*---------- File Edit  ------------*/
	private static List<String> items = null;
	private static List<String> paths = null;
	private String rootPath = "/";
	private TextView mPath;
	protected static Button fileEdit_back_btn, fileEdit_Transfer_btn,
			fileEdit_Exit_btn;
	private MyAdapter adapter;
	private File file;
	private static File_Explorer fe = new File_Explorer();;
	private static String nowPath;
	private static final String TAG = "LA";
	private float currentAcceleration = 0;
	private float maxAcceleration = 0;
	private SensorManager sensorManager;
	private static boolean sensorSwitch = false;
	private String selectMac;
	private int tw = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_edit);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);// 註冊sensor
		sensorSwitch = true;
		findViews();
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()))// 判斷SD卡是否存在
		{
			getFileDir(Environment.getExternalStorageDirectory()
					.getAbsolutePath());
		} else {
			getFileDir("/mnt/");
		}

		fileEdit_Transfer_btn.setEnabled(false);
		setListeners();
		doBindService(); // onBind 一定要放在這
		getListView().setOnItemLongClickListener(this); // 長時間按事件
		Bundle bundle = this.getIntent().getExtras();
		selectMac = bundle.getString("MAC");

		handler = new Handler() //
		{
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				uploadFile();
				delay();
				super.handleMessage(msg);
				sensorSwitch = true;
			}
		};
	}

	private void delay() // 消除延遲
	{
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		getFileDir(nowPath);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	/*---------- Interface  ------------*/
	private static final int NOTIFICATION_ID = 0x12;
	private Notification notification = null;
	private NotificationManager manager = null;
	public Handler handler;
	private int _progress = 0;
	private int max_pro = 0;
	protected static int flag = 0;
	private String file_name;

	private void findViews() {
		mPath = (TextView) findViewById(R.id.mPath);
		fileEdit_Transfer_btn = (Button) findViewById(R.id.fileEdit_Transfer_btn);
		fileEdit_back_btn = (Button) findViewById(R.id.fileEdit_back_btn);
		fileEdit_Exit_btn = (Button) findViewById(R.id.fileEdit_Exit_btn);

	}

	private void setListeners() {
		fileEdit_Transfer_btn.setOnClickListener(transferButton);
		fileEdit_back_btn.setOnClickListener(backButton);

		fileEdit_Exit_btn.setOnClickListener(exitButton);
	}

	// 退出 Button
	private Button.OnClickListener exitButton = new Button.OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// System.exit(0);
			finish();

		}
	};

	// upload button
	private Button.OnClickListener transferButton = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			uploadFile();
		}
	};

	private void uploadFile() {
		sensorManager.unregisterListener(this);//傳送開始，關閉sensor
		sensorSwitch = false;
		Thread transfer_Thread = new Thread(new Runnable() {
			public void run() {
				try {
					lss.connectImage(selectMac,
							mPath.getText() + "/" + file.getName());
					// lss.transferto(mPath.getText() + "/" + file.getName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		transfer_Thread.start();
		/*
		 * Bundle bundle = new Bundle(); // if(lss.getTy() == 1)
		 * bundle.putInt("TransferMode",11); // 11:傳送按鈕 // else if(lss.getTy()
		 * == 2) bundle.putInt("TransferMode",12); // 11:傳送按鈕 // else
		 * bundle.putInt("TransferMode",13); // 11:傳送按鈕
		 * bundle.putString("filePath",mPath.getText() + "/" + file.getName());
		 * Intent intent = new Intent(); //
		 * intent.setClass(FileManager.this,transferStatus.class); // Activity >
		 * // Login intent.putExtras(bundle); startActivity(intent);
		 */
	}

	/* -------- Service --------- */
	/* Service */
	private void doBindService() {

		bindService(new Intent(FileManager.this, LikeShareService.class),
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

	/* -------- File Edit --------- */
	private Button.OnClickListener backButton = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			File f = new File(mPath.getText().toString());
			fileEdit_Transfer_btn.setEnabled(false);// -----------------------
			getFileDir(f.getParent());

			if (mPath.getText().toString().equals(rootPath)) {
				fileEdit_back_btn.setEnabled(false);

			} else {
				fileEdit_back_btn.setEnabled(true);
			}

		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			File f = new File(mPath.getText().toString());
			fileEdit_Transfer_btn.setEnabled(false);// -----------------------
			if (!mPath.getText().toString().equals(rootPath))
				getFileDir(f.getParent());

			if (mPath.getText().toString().equals(rootPath)) {
				fileEdit_back_btn.setEnabled(false);

			} else {
				fileEdit_back_btn.setEnabled(true);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void getFileDir(String filePath) {

		mPath.setText(filePath);
		nowPath = filePath;
		if (!filePath.equals(rootPath)) {
			fileEdit_back_btn.setEnabled(true);
		}
		PackageAdapter pa = (PackageAdapter) fe.getFileDir(filePath);
		items = pa.getitems();
		paths = pa.getpaths();
		adapter = new MyAdapter(this, items, paths);
		setListAdapter(adapter);

	}

	public boolean onItemLongClick(AdapterView parent, View view, int position,
			long id) {

		/* CharSequence number = ((TextView) view).getText(); */

		/*
		 * Intent intent = new Intent(FileManager.this, transferStatus.class);
		 * startActivity(intent);
		 */

		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		file = new File(paths.get(position));
		fileEdit_Transfer_btn.setEnabled(false);// ---------------------------
		sensorSwitch = false;
		if (mPath.getText().toString().equals(rootPath))
			;
		if (file.canRead()) {

			if (file.isDirectory()) {
				getFileDir(paths.get(position));
			} else {
				fileEdit_Transfer_btn.setEnabled(true);// ---------------------
				sensorSwitch = true;
				adapter.setSelectItem(position);
				adapter.notifyDataSetInvalidated();
				fe.openFile(file);
			}
		} else {
			// 系統文件 禁止開啟
			// Toast.makeText(getApplicationContext(),R.string.system_file,Toast.LENGTH_SHORT).show();
			adapter.setSelectItem(-1);
			adapter.notifyDataSetInvalidated();
		}
	}

	/*------------------偵測G sensor事件----------------*/

	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the orientation and
		// accelerometer sensors

		Sensor accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// 感測G力
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);// 註冊事件

	}

	@Override
	protected void onPause() {// 結束
		// unregister listener
		sensorManager.unregisterListener(this);
		super.onStop();

	}

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
}
