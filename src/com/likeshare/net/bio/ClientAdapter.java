/*
 * TransportService 的 TCP方法
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
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ClientAdapter extends Thread
{
	private int port;
	private String IP;
	private Socket fileSocket;
	private Socket messageSocket;
	private Socket commandSocket;
	private InputStream fileIn;
	private OutputStream fileOut;
	private InputStream messageIn;
	private OutputStream messageOut;
	private InputStream commandIn;
	private OutputStream commandOut;
	private int fileOver = 0;

	/**
	 * 設定IP和port
	 * @param ip
	 * @param port
	 */
	public ClientAdapter(String ip,int port)
	{
		this.setIP(ip);
		this.setPort(port);

	}

	/**
	 * 設定訊息連接通道
	 * @param time 逾時
	 * @return boolean
	 * @throws Exception
	 */
	public boolean messageConnect(int time) throws Exception
	{
		try
		{
			messageSocket = new Socket();
			InetSocketAddress isa = new InetSocketAddress(
					InetAddress.getByName(this.IP),this.port);
			messageSocket.connect(isa,time);
			messageIn = messageSocket.getInputStream();
			messageOut = messageSocket.getOutputStream();
			return true;
		} catch(SocketTimeoutException e)
		{
			messageSocket.close();
			return false;
		} catch(ConnectException e1)
		{
			messageSocket.close();
			return false;
		}
	}
	public void socketClose()
	{
		try {
			messageSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fileSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 資料傳送通道
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void fileConnect() throws UnknownHostException, IOException
	{
		fileSocket = new Socket(InetAddress.getByName(this.IP),this.port);
		fileIn = fileSocket.getInputStream();
		fileOut = fileSocket.getOutputStream();
	}

	/**
	 * 設定命令通道
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void commandConnect() throws UnknownHostException, IOException
	{
		commandSocket = new Socket(InetAddress.getByName(this.IP),this.port);
		commandIn = commandSocket.getInputStream();
		commandOut = commandSocket.getOutputStream();
	}

	/**
	 * 等待命令
	 * @return 收到的命令
	 * @throws IOException
	 */
	public String waitCommand() throws IOException
	{
		// Scanner read = new Scanner(messageIn,"utf-8");
		 BufferedReader read = new BufferedReader(new
		 InputStreamReader(commandIn,
		 "utf-8"));
		//byte[] b = new byte[8192];
		//int i = commandIn.read(b);
		//String s = new String(b,0,i);
		return read.readLine();
	}
	/**
	 * 送出命令
	 * @param msg
	 */
	public void sendCommand(String msg)
	{
		PrintWriter ps = new PrintWriter(commandOut);
		ps.println(msg);
		ps.flush();
	}

	/**
	 * 等待訊息通道
	 * @return 收到訊息
	 * @throws IOException
	 */
	public String waitMessage() throws IOException
	{
		// Scanner read = new Scanner(messageIn,"utf-8");
		 BufferedReader read = new BufferedReader(new
		 InputStreamReader(messageIn));
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
	 * 執行緒等待訊息
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

	public int getPort()
	{
		return port;
	}

	public SocketAddress getRemoteSocketAddress() throws Exception
	{
		return messageSocket.getRemoteSocketAddress();
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getIP()
	{
		return IP;
	}

	public void setIP(String iP)
	{
		IP = iP;
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

	public int getFileOver()
	{
		return fileOver;
	}

	public void setFileOver(int fileOver)
	{
		this.fileOver = fileOver;
	}

	public InputStream getCommandIn()
	{
		return commandIn;
	}

	public void setCommandIn(InputStream commandIn)
	{
		this.commandIn = commandIn;
	}

	public OutputStream getCommandOut()
	{
		return commandOut;
	}

	public void setCommandOut(OutputStream commandOut)
	{
		this.commandOut = commandOut;
	}
	public boolean isConnect()
	{
		return messageSocket.isConnected();
	}
}
