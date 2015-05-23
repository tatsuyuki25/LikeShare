package com.likeshare;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.likeshare.net.LikeShareService;
import com.likeshare.net.bio.Transfer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class transferStatus extends Activity {
	private static final String TAG = "LA";
	private ProgressBar transferStatus_pb;
	private TextView nowCompletion_tv, fileName_tv, trans_mode;
	private Button cancel_btn;
	private String fileName = null;
	private int fileSize = 0;
	private Thread transferStart, downStart;
	private boolean startTransfer = false;
	private int mode = 0;
	public static Handler handlerNowCompletion, transferStatusHandler;
	private static ImageView iv;
	private static Animation am;
	private Handler _progressHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transfer_status);

		startTransfer = true;
		Bundle bundle = getIntent().getExtras();
		mode = bundle.getInt("TransferMode");
		cancel_btn = (Button) findViewById(R.id.transferStatus_cancel_Button);
		cancel_btn.setOnClickListener(cancelButton);
		transferStatus_pb = (ProgressBar) findViewById(R.id.transferStatus_fileTransferStatus_ProgressBar);
		nowCompletion_tv = (TextView) findViewById(R.id.transferStatus_nowCompletion_Textview);
		fileName_tv = (TextView) findViewById(R.id.transferStatus_FileName_Textview);
		trans_mode = (TextView) findViewById(R.id.transfer_mode);
		iv = (ImageView) findViewById(R.id.transfer_status_Icon_imageView);

		// 停止檔案傳送
		transferStatusHandler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				switch (msg.arg1) {
				case 1: // stop
					startTransfer = false;
					Transfer.stop = true;
					// FileManager.setSensorSwitch(true);// 開啟sensor
					finish();
					break;
				}
			}
		};
		_progressHandler = new Handler() {

			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (transferStatus_pb.getProgress() >= fileSize) {
					// progDialog.dismiss();
					// 圖片傳送動畫
					new Thread(new Runnable() {
						public void run() {
							if (mode == 11) {
								am = new TranslateAnimation(
										Animation.RELATIVE_TO_SELF,
										Animation.RELATIVE_TO_SELF,
										Animation.RELATIVE_TO_SELF, -1000);
								am.setDuration(1500);
								am.setRepeatCount(0);
								iv.setAnimation(am);
								am.startNow();
							} else {
								am = new TranslateAnimation(
										Animation.RELATIVE_TO_SELF,
										Animation.RELATIVE_TO_SELF, -1000,
										Animation.RELATIVE_TO_SELF);
								am.setDuration(1500);
								am.setRepeatCount(0);
								iv.setAnimation(am);
								am.startNow();
							}
							try {
								Thread.sleep(2500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							finish();
						}
					}).start();

				} else {
					// progDialog.incrementProgressBy(1);
					nowCompletion_tv
							.setText((int) (((double) Transfer.nowTransfer / (double) fileSize) * 100)
									+ "%");
					transferStatus_pb.setProgress(Transfer.nowTransfer);

					_progressHandler.sendEmptyMessageDelayed(0, 100);
				}
			}
		};
		if (mode == 11) {

			trans_mode.setText("Uploading...");
			File f = new File(bundle.getString("filePath"));
			fileName = f.getName();
			iv.setImageResource(LikeShareService.setICON(fileName));
			FileInputStream fis;
			try {
				fis = new FileInputStream(bundle.getString("filePath"));
				fileSize = fis.available();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else if (mode == 12) {

			trans_mode.setText("Downloading...");
			String[] fileMsg = bundle.getStringArray("fileMsg");
			fileName = fileMsg[0];
			fileSize = Integer.parseInt(fileMsg[1]);
			iv.setImageResource(LikeShareService.setICON(fileName));
			
		}
		fileName_tv.setText(fileName); // 顯示檔案名稱
		transferStatus_pb.setMax(fileSize); // 設定進度條最大長度為檔案大小
		_progressHandler.sendEmptyMessage(0);
	}

	private Button.OnClickListener cancelButton = new Button.OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (transferStatusHandler != null) {
				Message msg1 = transferStatusHandler.obtainMessage();
				msg1.arg1 = 1;
				msg1.sendToTarget();
			}
		}
	};

	
}
