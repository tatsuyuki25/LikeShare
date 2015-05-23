package com.likeshare;

import com.likeshare.net.LikeShareService;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LikeShareActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logo);
        Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				int waitingTime = 500; // ©µ¿ð®É¶¡
				try
				{
					while(waitingTime > 0)
					{
						sleep(100);
						waitingTime -= 100; // 100ms per time
					}
				} catch(InterruptedException e)
				{
					e.printStackTrace();
				} finally
				{
					/*startService(new Intent(SDTActivity.this, Server_Service.class));
					 Intent intent = new Intent(SDTActivity.this,LoginActivity.class);
					 startActivity(intent); // enter the main activity finally
					 finish();*/
					
					//while(ss == null);
					 go();
				}
			}
		};

		thread.start();
    }
    protected void go()
	{
		 
		
			
			// Intent intent = new Intent(SDTActivity.this,LoginActivity.class);
			/* 
			 if(ss.isConnect())
			 {
				 
				 startActivity(new Intent(SDTActivity.this,MultiViewManager.class));
				 finish();
			 }
			 else
			 {
				
				 startActivity(new Intent(SDTActivity.this,LoginActivity.class));
				 startService(new Intent(SDTActivity.this, Server_Service.class));
				 finish();
			 }*/
    		startService(new Intent(LikeShareActivity.this, LikeShareService.class));
    		startActivity(new Intent(LikeShareActivity.this,LoginActivity.class));
    		finish();
		
		
	}
}