package com.likeshare;

import java.io.IOException;

import com.likeshare.net.LikeShareService;
import com.likeshare.net.LikeShareService.LikeShareServiceBinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class LoginActivity extends Activity {
	private Button btnWiFi, btnSignIn, btnSignUp, btnExit;
	private EditText edtAccount, edtPasswd;
	private static boolean wifiStatus = false; // WiFi狀態
	private static WifiManager wifiManager;
	private CheckBox chkSave;
	private static String account;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork() // or
																		// .detectAll()
																		// for
																		// all
																		// detectable
																		// problems
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
				.build());
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE); // 取得目前 WiFi
		startService(new Intent(LoginActivity.this, LikeShareService.class)); // 狀態
		buttonCreate(); // 創建按鈕
		autoSignUp();
		//checkNetworkInfo(); 

	}
	
	private void checkNetworkInfo() {
		
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			//result = false;
		} else {
			if (!info.isAvailable()) {
			//	result = false;
			} else {
			//	result = true;
				if (chkSave.isChecked()) {
					setLogin();
				}
				
			}
		}

		
	}

	private void buttonCreate() {
		btnWiFi = (Button) findViewById(R.id.btnWifi);
		btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnSignUp = (Button) findViewById(R.id.btnSignUp);
		btnExit = (Button) findViewById(R.id.btnExit);
		chkSave = (CheckBox) findViewById(R.id.chkSave);
		edtAccount = (EditText) findViewById(R.id.edtAccount);
		edtPasswd = (EditText) findViewById(R.id.edtPasswd);
		btnWiFi.setOnClickListener(OnClickListener);
		btnSignIn.setOnClickListener(OnClickListener);
		btnSignUp.setOnClickListener(OnClickListener);
		btnExit.setOnClickListener(OnClickListener);
	}

	private Button.OnClickListener OnClickListener = new Button.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnSignIn:

				setLogin();
				break;
			case R.id.btnSignUp:

				startActivity(new Intent(LoginActivity.this, SignUp.class));
				break;
			case R.id.btnWifi:
				if (wifiStatus) {
					btnWiFi.setTextColor(0xFF8F8F8F);
					wifiStatus = false;
					wifiManager.setWifiEnabled(false);
				} else {
					btnWiFi.setTextColor(0xFFFFFFFF);
					wifiStatus = true;
					wifiManager.setWifiEnabled(true);
				}
				break;
			case R.id.btnExit:
				stopService(new Intent(LoginActivity.this,
						LikeShareService.class));
				System.exit(0);
				break;
			}
		}
	};

	private void setLogin() {
		final Thread login = new Thread(new Runnable() {
			public void run() {
				try {
					account = edtAccount.getText().toString();
					lss.login(account, edtPasswd.getText().toString());

					lss.setLoginHandler(loginHandler);
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		});
		login.start();
		ProgressDialog m_pDialog = new ProgressDialog(LoginActivity.this);
		m_pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		m_pDialog.setTitle("Login...");
		m_pDialog.setMessage("Please wait...");
		m_pDialog.setIcon(android.R.drawable.ic_dialog_info);
		m_pDialog.setIndeterminate(false);
		m_pDialog.setCancelable(true);
		m_pDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int i) {
				dialog.cancel();
			}
		});
		m_pDialog.show();

	}

	/**
	 * Login 成功 失敗 事件
	 */
	private Handler loginHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj.toString().equals("true")) {
				finish();

				// 登錄按鈕事件保存第一次的SharedPreferences
				if (chkSave.isChecked())// 檢測使用者名密碼
				{
					SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
					SharedPreferences.Editor edit = remdname.edit();
					edit.putString("email", edtAccount.getText().toString());
					edit.putString("pass", edtPasswd.getText().toString());
					edit.commit();
				}
				lss.setAccount(account);
				startActivity(new Intent(LoginActivity.this,
						MultiViewManager.class));
			} else {
				Toast.makeText(LoginActivity.this, "登入失敗", Toast.LENGTH_SHORT)
						.show();
			}
		}
	};

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		doBindService(); // onBind 綁Service
		// 檢查WIFI是否啟動
		if (wifiManager.isWifiEnabled()) {
			btnWiFi.setTextColor(0xFFFFFFFF);
			wifiStatus = true;

		} else {
			btnWiFi.setTextColor(0xFF8F8F8F);
			wifiStatus = false;
		}
	}

	private void autoSignUp() {

		chkSave.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
					SharedPreferences.Editor edit = remdname.edit();
					edit.putString("email", edtAccount.getText().toString());
					edit.putString("pass", edtPasswd.getText().toString());
					edit.commit();
				} else if (!isChecked) {

					SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
					SharedPreferences.Editor edit = remdname.edit();
					edit.putString("email", "");
					edit.putString("pass", "");
					edit.commit();
				}
			}
		});

		SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
		String email_str = remdname.getString("email", "");
		String pass_str = remdname.getString("pass", "");
		edtAccount.setText(email_str);
		edtPasswd.setText(pass_str);
		if (!email_str.equals("")) {
			chkSave.setChecked(true);
		}

	}

	/* Service */
	private void doBindService() {

		bindService(new Intent(LoginActivity.this, LikeShareService.class),
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
