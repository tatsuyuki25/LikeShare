package com.likeshare.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;



import com.likeshare.MultiViewManager;
import com.likeshare.R;
import com.likeshare.transferStatus;
import com.likeshare.camera.CameraTransfer_Service;
import com.likeshare.net.bio.ClientAdapter;
import com.likeshare.net.bio.Receiver;
import com.likeshare.net.bio.Transfer;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class LikeShareService extends Service {
	private static Context likeShareServiceContext;
	private static String selectTransferPath;
	public static ProgressDialog progressDialog;
	/**
	 * NIO
	 */
	// 信道選擇器
	private static Selector selector;
	// 與服務器通信的信道
	public static SocketChannel socketChannel;
	// 要連接的服務器Ip地址
	private static String hostIp = "220.133.107.221";
	// 要連接的遠程服務器在監聽的端口
	private static int hostListenningPort = 1978;

	/**
	 * BIO
	 */
	private Transfer tf;
	private Receiver rc;
	private String account; // 登入的帳號
	private ClientAdapter ca;
	private String serverIP = "220.133.107.221";
	private static WifiManager wifiManager;
	public static Handler loginHandler, signUpHandler, transportHandler;
	public static Handler cmdHandler, serverHandler;
	public ArrayList<String> device, friendDevices, friends;
	public static Thread bioFileServerThread;

	/**
	 * ProgressDialog
	 */
	private Handler _progressHandler;
	private ProgressDialog progDialog;
	private int transferFileSize=0;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Toast.makeText(getApplicationContext(), "LikeShareService Star",
				Toast.LENGTH_SHORT).show();
		// tf = new Transfer(this);
		// rc = new Receiver(this);
		startService(new Intent(LikeShareService.this, CameraTransfer_Service.class));
		likeShareServiceContext = this;
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		progressDialog = new ProgressDialog(LikeShareService.this);
		serverHandler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				switch (msg.arg1) {
				case 1: // 接收到要求連接伺服器 並扮演 傳送者T或接收者R
					starBioFileService((String) msg.obj);
					break;
				case 2: // 取消下載
					stopBioFileService();
					if (transferStatus.transferStatusHandler != null) {
						Message msg1 = transferStatus.transferStatusHandler
								.obtainMessage();
						msg1.arg1 = 1;
						msg1.sendToTarget();
					}
					Toast.makeText(getApplicationContext(), "Transfer aborted",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
	}

	/**
	 * 請求 account 的所有設備(完成)
	 * 
	 * @param account
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> getMyDevices(String account) throws IOException {
		// waitMessage.stop();
		ca.sendMessage("2," + account);
		ArrayList<String> devices = new ArrayList<String>();
		String tmp = ca.waitMessage();
		int size = Integer.parseInt(tmp);
		Log.i("zzzzzzzzzzzz", tmp);

		for (int i = 0; i < size; i++) {
			ca.sendMessage("size");
			tmp = ca.waitMessage();
			Log.i("-----------------", tmp);
			devices.add(tmp);
		}
		ca.sendMessage("ok");
		// waitMessage.start();
		return devices;
	}

	/**
	 * 停止執行檔案下載
	 */
	private synchronized void stopBioFileService() {
		tf.stop = true; // 停止下載迴圈
		tf.socketClose();
	}
	
	/**
	 * 開始執行檔案傳輸(完成)
	 * 
	 * @param msg
	 */
	private synchronized void starBioFileService(final String msg) {
		bioFileServerThread = new Thread(new Runnable() {
			public void run() {
				String[] tmp = msg.split(","); // 6,port,R or T
				tf = new Transfer(LikeShareService.this);
				try {
					boolean typeb = tf.setAddress(serverIP,
							Integer.parseInt(tmp[1]));
					Log.i("GGGGGGGGGGG", "連接伺服器:" + typeb + "");
					if (typeb) {
						if (tmp[2].equalsIgnoreCase("T")) // 傳送端
						{
							new Thread(new Runnable() {
								public void run() {
									try {
										tf.transfer(selectTransferPath);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}).start();
							
							Bundle bundle = new Bundle();
							bundle.putInt("TransferMode", 11);
							bundle.putString("filePath", selectTransferPath);
							Intent intent = new Intent();
							intent.setClass(LikeShareService.this,
									transferStatus.class);
							intent.putExtras(bundle);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);

						} else // 接收端
						{
							tf.ca.sendMessage("true");
							tf.Receiver.start();
						}

					} else {
						ca.sendCommand("false");
						// ca.waitCommand();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		bioFileServerThread.start();
	}
	
	/**
	 * 登入用 (完成)
	 * 
	 * @param account
	 * @param pass
	 * @throws IOException
	 * @throws Exception
	 */
	public void login(String account, String pass) throws Exception {

		if (!checkConnectionStatus()) {
			// 打開監聽信道並設置為非阻塞模式
			socketChannel = SocketChannel.open(new InetSocketAddress(hostIp,
					hostListenningPort));
			socketChannel.configureBlocking(false);

			// 打開並註冊選擇器到信道
			selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_READ);

			// 啟動讀取線程
			new nioClientReadThread(selector);
		}
		// 送出 MAC
		boolean wb = wifiManager.isWifiEnabled();
		wifiManager.setWifiEnabled(true);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String m = wifiInfo.getMacAddress(); // MAC
		if (!wb)
			wifiManager.setWifiEnabled(false);
		sendMsg("1," + account + "," + pass + "," + m + ",android");

		/*
		 * String b = ca.waitMessage(); if (b.equals("true")) {
		 * this.setAccount(account); // Log.i("return", ca.waitMessage());
		 * Log.i("login", waitCommand.isAlive() + ""); waitCommand.start();
		 * return true; } else return false;
		 */
	}

	public void signUp(String account, String pass, String name)
			throws IOException {
		if (!checkConnectionStatus()) {
			// 打開監聽信道並設置為非阻塞模式
			socketChannel = SocketChannel.open(new InetSocketAddress(hostIp,
					hostListenningPort));
			socketChannel.configureBlocking(false);

			// 打開並註冊選擇器到信道
			selector = Selector.open();
			socketChannel.register(selector, SelectionKey.OP_READ);

			// 啟動讀取線程
			new nioClientReadThread(selector);
		}
		sendMsg("0," + account + "," + pass + "," + name);
	}
	protected static boolean checkConnectionStatus() // 確認是否連線
	{
		try {
			SocketAddress tmp = socketChannel.socket().getRemoteSocketAddress();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 取得 MultiViewManager 的 handler
	 * 
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		LikeShareService.cmdHandler = handler;
	}

	/**
	 * 發送字串到伺服器 (完成) 0註冊 1登入 2請求我的所有設備 3請求我的所有好友清單 4請求好友的設備 5請求好友名單
	 * 6修改好友名稱 7刪除好友 8新增好友
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sendMsg(String message) throws IOException {
		ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes("UTF-16"));
		socketChannel.write(writeBuffer);
	}

	public void setAccount(String account) {
		this.account = account;
	}
	
	public String getAccount() {
		return this.account;
	}

	/**
	 * 取得 LoginActivity 的 handler
	 * 
	 * @param handler
	 */
	public void setLoginHandler(Handler handler) {
		LikeShareService.loginHandler = handler;
	}

	public void setSignUpHandler(Handler handler) {
		LikeShareService.signUpHandler = handler;
	}

	/**
	 * 添加下載內容
	 * 
	 * @param name
	 */
	public static void setMyDownloads(String name) // 下載後必須呼叫
	{
		SharedPreferences remdname = likeShareServiceContext
				.getSharedPreferences("download", Context.MODE_PRIVATE);
		String downloadHistory = remdname.getString("downloadHistory", "");
		SharedPreferences.Editor edit = remdname.edit();
		if (downloadHistory.length() != 0) {
			downloadHistory += "," + name;

		} else {
			downloadHistory += name;
		}
		edit.putString("downloadHistory", downloadHistory);
		edit.commit();
		if (cmdHandler != null) {
			Message msg = cmdHandler.obtainMessage();
			msg.arg1 = 5;
			msg.sendToTarget();
		}
	}
	
	public void setDefaultDevice(String mac) 
	{
		SharedPreferences remdname = likeShareServiceContext
				.getSharedPreferences("movies", Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = remdname.edit();
		edit.putString("defaultDevice", mac);
		edit.commit();
	}
	
	public String getDefaultDevice()
	{
		SharedPreferences remdname = likeShareServiceContext
				.getSharedPreferences("movies", Context.MODE_PRIVATE);
		return remdname.getString("defaultDevice", "");
	}

	/**
	 * Service
	 */
	private final LikeShareServiceBinder MyLikeShareServiceBinder = new LikeShareServiceBinder();

	public class LikeShareServiceBinder extends Binder {
		public Service getService() {
			return LikeShareService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return MyLikeShareServiceBinder;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	/*
	 * public void connect(String mac) throws IOException { // TODO
	 * Auto-generated method stub
	 * 
	 * 
	 * tf = new Transfer(LikeShareService.this); rc = new
	 * Receiver(LikeShareService.this); if(rc.setSocket()) { sendMsg("5," + mac
	 * +","+rc.); } }
	 */

	public void connectImage(String mac, String path) throws IOException {
		// TODO Auto-generated method stub
		sendMsg("5," + mac);
		selectTransferPath = path;
	}
	
	public void connectVideo(String mac, String path, int cp) throws IOException {
		// TODO Auto-generated method stub
		sendMsg("9," + mac+","+cp);
		selectTransferPath = path;
	}

	/**
	 * 取得檔案對應ICON
	 * 
	 * @param fileName
	 * @return ICON資源
	 */
	public static int setICON(String fileName) {
		String end = fileName.substring(fileName.lastIndexOf(".") + 1,
				fileName.length()).toLowerCase();

		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			return R.drawable.audio;
		} else if (end.equals("3gp") || end.equals("mp4")) {
			return R.drawable.video;
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			return R.drawable.photo;
		} else if (end.equals("pdf")) {
			return R.drawable.pdf;
		} else if (end.equals("doc") || end.equals("docx")) {
			return R.drawable.word;
		} else if (end.equals("7z") || end.equals("rar") || end.equals("zip")) {
			return R.drawable.compression;
		} else if (end.equals("xls") || end.equals("xlsx")) {
			return R.drawable.excel;
		} else if (end.equals("ppt") || end.equals("pptx")) {
			return R.drawable.powerpoint;
		} else {
			return R.drawable.file_edit_file;
		}
	}
	
	/*public void scan()
	{
		Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED,Uri.parse("file://"+Environment.getExternalStorageDirectory()));
		sendBroadcast(intent);
	}*/

}
