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
	private File file = null; // ���ͪ��ɮ�
	protected static int downLength;
	protected static int Port = 8801;
	protected static boolean turnoff = false;
	public static int transferLength = 0;
	public String fileName = null;
	public int fileSize = 0;
	public static boolean EnoughSpace = false;
	private Context ct;
	/**
	 * �]�wcontext
	 * @param ctext
	 */
	public Receiver(Context ctext)
	{
		ct = ctext;
	}
	
	/**
	 * �]�w�����ݤf
	 * @return ���\�Υ���
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
	 * ����ǵ��ݩR�O
	 * 11�ɮ׶ǰe
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

				if(Msg[0].toString().equals("11")) // 11 ���ɮ׶ǰe
				{
					if(checkFileEnoughspace(Msg[1],Msg[2])) // �P�_�Ŷ��O�_����
					{
						new Thread(new Runnable()
						{
							public void run()
							{
								downFile(); // �U���ɮ�
							}
						}).start();
						Bundle bundle = new Bundle();
						bundle.putInt("TransferMode",21); // 11:�ǰe���s
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
						// �Ŷ�����ĵ�i
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
			file.delete(); // �U������ �M��
		}
	}

	/**
	 * �}�l�ǰe
	 * @param path
	 * @throws Exception
	 */
	protected void transfer(String path) throws Exception
	{
		transferLength = 0;
		File f = new File(path);
		FileInputStream fis;
		
		fis = new FileInputStream(path);
		sa.sendMessage("11:" + f.getName() + ":" + fis.available()); // �ǰe�ɮ׸�T
													// �ɮפj�p
		String s = sa.waitMessage(); // ���ݰT��
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
			// �Ŷ������ƥ�
			EnoughSpace = false; // �Ŷ�����
		}
		//FileManager.setSensorSwitch(true);// �}��sensor

	}

	/**
	 * �T�w�e�q�O�_����
	 * @param fileName
	 * @param size
	 * @return boolean
	 */
	private boolean checkFileEnoughspace(String fileName,String size)
	{
		boolean DownloadSwitch; // �T�{�O�_�i���� �U��������

		if(Environment.MEDIA_MOUNTED
				.equals(Environment.getExternalStorageState())) // �P�_SD�d�O�_�s�b
		{
			// (�U) ���o�Ѿl�Ŷ�
			StatFs sf = new StatFs(Environment.getExternalStorageDirectory()
					.getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();

			// �P�_�Ŷ��O�_����
			if(Integer.parseInt(size) < availCount * blockSize)
			{
				DownloadSwitch = true;
				sa.sendMessage("Y");
				if(!(new File("/mnt/sdcard/download/").exists())) // ��󧨤��s�b
				{
					new File("/mnt/sdcard/download/").mkdir();// ��󧨤��s�b
				}
				file = new File("/mnt/sdcard/download/" + fileName);
			} else
			// �Ŷ�����
			{
				DownloadSwitch = false;
				sa.sendMessage("N");
			}
		} else
		// SDcard���s�b
		{
			StatFs sf = new StatFs(Environment.getRootDirectory().getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();

			if(Integer.parseInt(size) < availCount * blockSize)
			{
				DownloadSwitch = true;
				sa.sendMessage("Y");
				if(!(new File("/mnt/download/").exists())) // ��󧨤��s�b
				{
					new File("/mnt/download/").mkdir();// ��󧨤��s�b
				}
				file = new File("/mnt/download/" + fileName);
			} else
			// �Ŷ�����
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
