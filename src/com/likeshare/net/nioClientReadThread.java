package com.likeshare.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class nioClientReadThread implements Runnable {
	private Selector selector;

	public nioClientReadThread(Selector selector) {
		this.selector = selector;

		new Thread(this).start();
	}

	public void run() {
		try {
			Log.i("Login", "nio read start");
			while (selector.select() > 0) {
				// 遍歷每個有可用IO操作Channel對應的SelectionKey
				for (SelectionKey sk : selector.selectedKeys()) {

					// 如果該SelectionKey對應的Channel中有可讀的數據
					if (sk.isReadable()) {
						// 使用NIO讀取Channel中的數據
						SocketChannel sc = (SocketChannel) sk.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						sc.read(buffer);
						buffer.flip();
						// 將字節轉化為為UTF-16的字符串
						String receivedString = Charset.forName("UTF-16")
								.newDecoder().decode(buffer).toString();
						// 控制台打印出來
						System.out.println("接收到來自服務器"
								+ sc.socket().getRemoteSocketAddress() + "的信息:"
								+ receivedString);

						String tmp[] = receivedString.split(",");
						Message msg = null;
						switch (Integer.parseInt(tmp[0])) {
						case 0: // login
							msg = LikeShareService.loginHandler.obtainMessage();
							msg.obj = tmp[1]; // true or false
							msg.sendToTarget();
							break;
						case 1: // 收到好友上線通知 tmp[1] = MAC
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 2;
								msg.obj = tmp[1];
								msg.sendToTarget();
							}
							break;
						case 2: // 請求我的所有設備
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 1;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break; // 收到 我的所有設備名單
						case 3: // 註冊成功失敗通道
							if (LikeShareService.signUpHandler != null) {
								msg = LikeShareService.signUpHandler
										.obtainMessage();
								msg.obj = tmp[1]; // true or false
								msg.sendToTarget();
							}
							break;
						case 4: // 收到好友清單
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 3;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break;
						case 5: // 收到好友擁有的設備
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 4;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break;
						case 6: // 連接伺服器指定的port
							if (LikeShareService.serverHandler != null) {
								msg = LikeShareService.serverHandler
										.obtainMessage();
								msg.arg1 = 1;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break;
						case 7: // 取消下載
							if (LikeShareService.serverHandler != null) {
								msg = LikeShareService.serverHandler
										.obtainMessage();
								msg.arg1 = 2;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break;
						case 8: // 新增好友成功 OR 失敗
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 7;
								if (tmp[1].equals("true")) {
									msg.arg2 = 1;
								} else
									msg.arg2 = 0;
								msg.sendToTarget();
								break;
							}
						}

						// 為下一次讀取作準備
						sk.interestOps(SelectionKey.OP_READ);
					}

					// 刪除正在處理的SelectionKey
					selector.selectedKeys().remove(sk);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}