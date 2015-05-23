package com.likeshare.net.bio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Handler;


import com.likeshare.R;
import com.likeshare.transferStatus;
import com.likeshare.net.LikeShareService;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;

public class Transfer {
	public static ClientAdapter ca = null;
	public static int transferLength = 0;
	public String fileName = null;
	public int fileSize = 0;
	public static boolean EnoughSpace = false;
	private String[] Msg;
	private File file = null; // 產生的檔案
	public static int downLength;
	protected static boolean turnoff = false;
	private Context ct;
	public static boolean stop = false;
	public static int nowTransfer=0;
	public Transfer(Context ctext) {
		ct = ctext;
	}

	/*-----------連接Socket方法-----------*/
	public void socketClose() {
		ca.socketClose();
	}

	/**
	 * 設定連接 IP 和 PORT
	 * 
	 * @param ip
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public boolean setAddress(String ip, int port) throws Exception {
		ca = new ClientAdapter(ip, port); // 設定IP
		boolean b = ca.messageConnect(5000); // 設定訊息通道
		if (b) {
			ca.fileConnect();
			return true;
		} else {
			return false;
		}
	}

	public Thread Receiver = new Thread(new Runnable() {
		public void run() {
			stop = false;
			while (!stop) {

				try {
					Msg = ca.waitMessage().split(":");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}

				if (Msg[0].toString().equals("11")) // 11 為檔案傳送
				{
					if (checkFileEnoughspace(Msg[1], Msg[2])) // 判斷空間是否足夠
					{
						new Thread(new Runnable() {
							public void run() {
								downFile(); // 下載檔案
							}
						}).start();
						Bundle bundle = new Bundle();
						bundle.putInt("TransferMode", 12); // 11:傳送按鈕
						bundle.putStringArray("fileMsg", new String[] { Msg[1],
								Msg[2] });
						Intent intent = new Intent();
						intent.setClass(ct, transferStatus.class); // Activity
						intent.putExtras(bundle);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ct.startActivity(intent);
						
						
						
					} else {
						// 空間不足警告
					}
				}
			}
		}
	});

	/**
	 * 下載方法
	 */
	private void downFile() {
		
		stop = false;
		int len;
		int max_len = 0;
		nowTransfer=0;
		int max = Integer.parseInt(Msg[2]);
		byte[] buffer = new byte[8192];
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			while ((len = ca.readData(buffer)) != -1 && !stop) {
				max_len += len;
				fos.write(buffer, 0, len);
				nowTransfer = max_len;
				if (max == max_len)
				{
					LikeShareService.setMyDownloads(Msg[1]);
					break;
				}
					
			}

			// downLength = 0;
			// sa.setFileOver(0);
			fos.flush();
			fos.close();
			if (stop) {
				file.delete(); // 下載失敗 清除
			}
			// Server_Service.setMyDownloads(Msg[1]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			file.delete(); // 下載失敗 清除
		}
	}

	/**
	 * 傳送方法
	 * 
	 * @param path
	 *            亦傳送的檔案路徑
	 * @throws Exception
	 */
	public void transfer(String path) throws Exception {
		
		nowTransfer = 0;
		File f = new File(path);
		FileInputStream fis;
		fileName = f.getName();
		stop = false;
		fis = new FileInputStream(path);
		fileSize = fis.available(); // 取得檔案大小
		ca.sendMessage("11:" + fileName + ":" + fileSize); // 傳送檔案資訊
		String s = ca.waitMessage(); // 等待訊息
		System.out.println("gg");
		if (s.toString().equals("Y")) {
			EnoughSpace = true;
			int len;
			byte[] buffer = new byte[8192];
			while ((len = fis.read(buffer)) != -1 && !stop) {
				ca.sendData(buffer, 0, len);
				nowTransfer += len;
			}
			fis.close();
			if (stop) {
				ca.socketClose();
			}
			// ca.sendMessage("fileOver");
		} else {
			// 空間不足事件
			EnoughSpace = false; // 空間不足
		}
		// FileManager.setSensorSwitch(true);// 開啟sensor

	}

	/**
	 * 確認空間是否足夠，並產生檔案
	 * 
	 * @param name
	 *            產生的檔案名稱
	 * @param max
	 *            欲產生檔案大小
	 * @return 是否足夠
	 */
	private boolean checkFileEnoughspace(String name, String max) {
		boolean DownloadSwitch; // 確認是否可執行 下載執行續

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) // 判斷SD卡是否存在
		{
			// (下) 取得剩餘空間
			StatFs sf = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();

			// 判斷空間是否足夠
			if (Integer.parseInt(Msg[2]) < availCount * blockSize) {
				DownloadSwitch = true;
				ca.sendMessage("Y");
				if (!(new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath()+"/download/").exists())) // 文件夾不存在
				{
					new File(Environment.getExternalStorageDirectory()
							.getAbsolutePath()+"/download/").mkdir();// 文件夾不存在
				}
				file = new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath()+"/download/" + Msg[1]);
			} else
			// 空間不足
			{
				DownloadSwitch = false;
				ca.sendMessage("N");
			}
		} else
		// SDcard不存在
		{
			StatFs sf = new StatFs(Environment.getRootDirectory().getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();

			if (Integer.parseInt(Msg[2]) < availCount * blockSize) {
				DownloadSwitch = true;
				ca.sendMessage("Y");
				if (!(new File("/mnt/download/").exists())) // 文件夾不存在
				{
					new File("/mnt/download/").mkdir();// 文件夾不存在
				}
				file = new File("/mnt/download/" + Msg[1]);
			} else
			// 空間不足
			{
				DownloadSwitch = false;
				ca.sendMessage("N");
			}
		}

		return DownloadSwitch;
	}


}
