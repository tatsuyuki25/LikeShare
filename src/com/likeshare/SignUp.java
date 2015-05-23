package com.likeshare;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.likeshare.net.LikeShareService;
import com.likeshare.net.LikeShareService.LikeShareServiceBinder;
import com.likeshare.MultiViewManager;
import com.likeshare.R;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUp extends Activity{
	private EditText edtEmail,edtPasswd,edtConfirmPasswd,edtUserName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sign_up);
		doBindService();
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()     
	      .detectDiskReads()     
	      .detectDiskWrites()     
	      .detectNetwork()   // or .detectAll() for all detectable problems     
	      .penaltyLog()     
	      .build());     
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()     
	      .detectLeakedSqlLiteObjects()        
	      .penaltyLog()     
	      .penaltyDeath()     
	      .build());
		
		edtEmail=(EditText)findViewById(R.id.edtEmail);
		edtPasswd=(EditText)findViewById(R.id.edtPasswd);
		edtConfirmPasswd=(EditText)findViewById(R.id.edtConfirmPasswd);
		edtUserName=(EditText)findViewById(R.id.edtUserName);
		
		Button bt = (Button) findViewById(R.id.btnCreate);
		bt.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				 Pattern p = Pattern.compile( "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");  
			     Matcher m = p.matcher(edtEmail.getText().toString());  
			     
			     if(m.matches() && true) // 檢查是否重複
			     {
			    	 if(edtPasswd.getText().toString().equals(edtConfirmPasswd.getText().toString()))
						{
							if(edtUserName.getText().toString()!= null)
							{
								lss.setSignUpHandler(signUpHandler);
								// 寫入資料庫
								String account = edtEmail.getText().toString();
								String pass = edtPasswd.getText().toString();
								String name = edtUserName.getText().toString();
								try
								{
									lss.signUp(account,pass,name);
									
								} catch(Exception e)
								{
									e.printStackTrace();
									Toast.makeText(getApplicationContext(), "Sign Up error!",
							    		     Toast.LENGTH_SHORT).show();
								}
							}else
							{
								Toast.makeText(getApplicationContext(), "User Name format error!",
						    		     Toast.LENGTH_SHORT).show();
							}
						}else
						{
							Toast.makeText(getApplicationContext(), "Confirm password is not the same!",
					    		     Toast.LENGTH_SHORT).show();
						}
			     }else
			     {
			    	 Toast.makeText(getApplicationContext(), "Sorry, a user with that email address already exists or the email was invalid.",
			    		     Toast.LENGTH_SHORT).show();
			     }
				
				
			}

		});
	}
	/**
	 * Handler 來自 LikeShareService 的 訊息
	 */
	private Handler signUpHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj.toString().equals("true")) {
				finish();
				Toast.makeText(getApplicationContext(), "Create Account Success.",
		    		     Toast.LENGTH_SHORT).show();
				startActivity(new Intent(SignUp.this,
						LoginActivity.class));
			} else {
				Toast.makeText(getApplicationContext(), "That e-mail is already taken. Please choose another.",
		    		     Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	/* Service */
	private void doBindService() {

		bindService(new Intent(SignUp.this, LikeShareService.class),
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
