package com.likeshare.net.bio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;

public class Receiver
{
	protected ServerAdapter sa = null;
	private String[] Msg;
	private File file = null; // 產生的檔案
	protected static int downLength;
	protected static int Port = 8801;
	protected static boolean turnoff = false;
	public static int transferLength = 0;
	public String fileName = null;
	public int fileSize = 0;
	public static boolean EnoughSpace = false;
	private Context ct;
	/**
	 * 設定context
	 * @param ctext
	 */
	public Receiver(Context ctext)
	{
		ct = ctext;
	}
	
	/**
	 * 設定接收端口
	 * @return 成功或失敗
	 * @throws Exception
	 */
	public boolean setSocket() throws Exception
	{
		sa = new ServerAdapter(Port);
		boolean b = sa.messageListen(10000);
		if(b)
		{
			sa.fileListen();
			sa.socketClose();
			return true;
		} else
		{
			sa.socketClose();
			return false;
		}
	}
	/**
	 * 執行序等待命令
	 * 11檔案傳送
	 */
	Thread Receiver = new Thread(new Runnable()
	{
		public void run()
		{
				try
				{
					Msg = sa.waitMessage().split(":");
				} catch(Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}

				if(Msg[0].toString().equals("11")) // 11 為檔案傳送
				{
					if(checkFileEnoughspace(Msg[1],Msg[2])) // 判斷空間是否足夠
					{
						new Thread(new Runnable()
						{
							public void run()
							{
								downFile(); // 下載檔案
							}
						}).start();
						Bundle bundle = new Bundle();
						bundle.putInt("TransferMode",21); // 11:傳送按鈕
						bundle.putStringArray("fileMsg",
								new String[] { Msg[1], Msg[2] });
						Intent intent = new Intent();
						//intent.setClass(ct,transferStatus.class); // Activity
																											// >
																											// Login
						intent.putExtras(bundle);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						ct.startActivity(intent);
					} else
					{
						// 空間不足警告
					}
				}
		}
	});

	private void downFile()
	{
		int len;
		int max_len = 0;
		int max = Integer.parseInt(Msg[2]);
		byte[] buffer = new byte[8192];
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(file);
			while((len = sa.readData(buffer)) != -1)
			{
				max_len += len;
				fos.write(buffer,0,len);
				downLength = max_len;
				if(max == max_len)
					break;
			}
			// downLength = 0;
			// sa.setFileOver(0);
			fos.flush();
			fos.close();
			//Server_Service.setMyDownloads(Msg[1]);
		} catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			file.delete(); // 下載失敗 清除
		}
	}

	/**
	 * 開始傳送
	 * @param path
	 * @throws Exception
	 */
	protected void transfer(String path) throws Exception
	{
		transferLength = 0;
		File f = new File(path);
		FileInputStream fis;
		
		fis = new FileInputStream(path);
		sa.sendMessage("11:" + f.getName() + ":" + fis.available()); // 傳送檔案資訊
													// 檔案大小
		String s = sa.waitMessage(); // 等待訊息
		System.out.println("gg");
		if(s.toString().equals("Y"))
		{
			EnoughSpace = true;
			int len;
			byte[] buffer = new byte[8192];
			while((len = fis.read(buffer)) != -1)
			{
				sa.sendData(buffer,0,len);
				transferLength += len;
			}
			fis.close();
			// ca.sendMessage("fileOver");
		} else
		{
			// 空間不足事件
			EnoughSpace = false; // 空間不足
		}
		//FileManager.setSensorSwitch(true);// 開啟sensor

	}

	/**
	 * 確定容量是否足夠
	 * @param fileName
	 * @param size
	 * @return boolean
	 */
	private boolean checkFileEnoughspace(String fileName,String size)
	{
		boolean DownloadSwitch; // 確認是否可執行 下載執行續

		if(Environment.MEDIA_MOUNTED
				.equals(Environment.getExternalStorageState())) // 判斷SD卡是否存在
		{
			// (下) 取得剩餘空間
			StatFs sf = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();

			// 判斷空間是否足夠
			if(Integer.parseInt(size) < availCount * blockSize)
			{
				DownloadSwitch = true;
				sa.sendMessage("Y");
				if(!(new File("/mnt/sdcard/download/").exists())) // 文件夾不存在
				{
					new File("/mnt/sdcard/download/").mkdir();// 文件夾不存在
				}
				file = new File("/mnt/sdcard/download/" + fileName);
			} else
			// 空間不足
			{
				DownloadSwitch = false;
				sa.sendMessage("N");
			}
		} else
		// SDcard不存在
		{
			StatFs sf = new StatFs(Environment.getRootDirectory().getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();

			if(Integer.parseInt(size) < availCount * blockSize)
			{
				DownloadSwitch = true;
				sa.sendMessage("Y");
				if(!(new File("/mnt/download/").exists())) // 文件夾不存在
				{
					new File("/mnt/download/").mkdir();// 文件夾不存在
				}
				file = new File("/mnt/download/" + fileName);
			} else
			// 空間不足
			{	
				DownloadSwitch = false;
				sa.sendMessage("N");
			}
		}

		return DownloadSwitch;
	}
	public String getAddress()
	{
		return sa.getFileSocket().getLocalAddress()+","+sa.getFileSocket().getPort(); 
	}

}
