/*
 *  n//ew ServerAdapter(Port) //設定port
 * 	messageListen() //訊息監聽端口    有 2 種端口 
 * 	fileListen(); //資料監聽端口 
 * 	sendMessage("fileY"); // 送出訊息
 *  Msg = sa.waitMessage(); // 接收訊息
 *  readData(byte[] buffer) // 接收檔案
 * 	
 * */
package com.likeshare.net.bio;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerAdapter implements Runnable
{
	private int port;
	private ServerSocket fileServer;
	private Socket fileSocket;
	private Socket messageSocket;
	private InputStream fileIn;
	private OutputStream fileOut;
	private InputStream messageIn;
	private OutputStream messageOut;
	private int fileOver = 0;

	/**
	 * 設定port
	 * @param port
	 */
	public ServerAdapter(int port)
	{
		this.setPort(port);
	}

	/**
	 * 監聽訊息通道
	 * @param time 逾時
	 * @return boolean
	 * @throws Exception
	 */
	public boolean messageListen(int time) throws Exception
	{
		try
		{
			fileServer = new ServerSocket(port);
			fileServer.setSoTimeout(time);
			messageSocket = fileServer.accept();
			messageIn = messageSocket.getInputStream();
			messageOut = messageSocket.getOutputStream();
			return true;
		} catch(SocketTimeoutException e)
		{
			fileServer.close();
			return false;
		}
	}

	/**
	 * 監聽檔案通道
	 * @throws IOException
	 */
	public void fileListen() throws IOException
	{
		// fileServer = new ServerSocket(port);
		fileSocket = fileServer.accept();
		fileIn = fileSocket.getInputStream();
		fileOut = fileSocket.getOutputStream();
	}

	/**
	 * 等待訊息
	 * @return 收到的訊息
	 * @throws IOException
	 */
	public String waitMessage() throws IOException
	{
		 BufferedReader read = new BufferedReader(new
		 InputStreamReader(messageIn,
		 "utf-8"));
		//byte[] b = new byte[8192];
		//int i = messageIn.read(b);
		//String s = new String(b,0,i);
		return read.readLine();
	}

	/**
	 * 傳送訊息
	 * @param msg
	 */
	public void sendMessage(String msg)
	{
		PrintWriter ps = new PrintWriter(messageOut);
		ps.println(msg);
		ps.flush();
	}

	/**
	 * 傳送檔案
	 * @param buffer
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	public void sendData(byte[] buffer,int off,int len) throws IOException
	{
		DataOutputStream dos = new DataOutputStream(fileOut);
		dos.write(buffer,off,len);
		dos.flush();
	}

	/**
	 * 接收檔案
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public int readData(byte[] buffer) throws IOException
	{
		DataInputStream dis = new DataInputStream(fileIn);
		return dis.read(buffer,0,buffer.length);
	}
	/**
	 * 等待訊息執行續
	 */
	public void run()
	{
		try
		{
			String msg = waitMessage();
			if(msg.equals("fileOver"))
				setFileOver(1);

		} catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * 以下為各個屬性的Getter與Setter
	 */
	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public Socket getFileSocket()
	{
		return fileSocket;
	}

	public void setFileSocket(Socket fileSocket)
	{
		this.fileSocket = fileSocket;
	}

	public InputStream getFileIn()
	{
		return fileIn;
	}

	public void setFileIn(InputStream fileIn)
	{
		this.fileIn = fileIn;
	}

	public OutputStream getFileOut()
	{
		return fileOut;
	}

	public void setFileOut(OutputStream fileOut)
	{
		this.fileOut = fileOut;
	}

	public Socket getMessageSocket()
	{
		return messageSocket;
	}

	public void setMessageSocket(Socket messageSocket)
	{
		this.messageSocket = messageSocket;
	}

	public InputStream getMessageIn()
	{
		return messageIn;
	}

	public void setMessageIn(InputStream messageIn)
	{
		this.messageIn = messageIn;
	}

	public OutputStream getMessageOut()
	{
		return messageOut;
	}

	public void setMessageOut(OutputStream messageOut)
	{
		this.messageOut = messageOut;
	}

	public synchronized int getFileOver()
	{
		return fileOver;
	}

	public synchronized void setFileOver(int fileOver)
	{
		this.fileOver = fileOver;
	}

	public void setSendUrgentData() throws Exception
	{
		messageSocket.sendUrgentData(0xFF);
	}
	/**
	 * 檔案
	 */
	public void socketClose()
	{
		try
		{
			fileServer.close();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
