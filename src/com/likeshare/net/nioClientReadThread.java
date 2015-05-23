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
				// �M���C�Ӧ��i��IO�ާ@Channel������SelectionKey
				for (SelectionKey sk : selector.selectedKeys()) {

					// �p�G��SelectionKey������Channel�����iŪ���ƾ�
					if (sk.isReadable()) {
						// �ϥ�NIOŪ��Channel�����ƾ�
						SocketChannel sc = (SocketChannel) sk.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						sc.read(buffer);
						buffer.flip();
						// �N�r�`��Ƭ���UTF-16���r�Ŧ�
						String receivedString = Charset.forName("UTF-16")
								.newDecoder().decode(buffer).toString();
						// ����x���L�X��
						System.out.println("������Ӧ۪A�Ⱦ�"
								+ sc.socket().getRemoteSocketAddress() + "���H��:"
								+ receivedString);

						String tmp[] = receivedString.split(",");
						Message msg = null;
						switch (Integer.parseInt(tmp[0])) {
						case 0: // login
							msg = LikeShareService.loginHandler.obtainMessage();
							msg.obj = tmp[1]; // true or false
							msg.sendToTarget();
							break;
						case 1: // ����n�ͤW�u�q�� tmp[1] = MAC
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 2;
								msg.obj = tmp[1];
								msg.sendToTarget();
							}
							break;
						case 2: // �ШD�ڪ��Ҧ��]��
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 1;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break; // ���� �ڪ��Ҧ��]�ƦW��
						case 3: // ���U���\���ѳq�D
							if (LikeShareService.signUpHandler != null) {
								msg = LikeShareService.signUpHandler
										.obtainMessage();
								msg.obj = tmp[1]; // true or false
								msg.sendToTarget();
							}
							break;
						case 4: // ����n�ͲM��
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 3;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break;
						case 5: // ����n�;֦����]��
							if (LikeShareService.cmdHandler != null) {
								msg = LikeShareService.cmdHandler
										.obtainMessage();
								msg.arg1 = 4;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break;
						case 6: // �s�����A�����w��port
							if (LikeShareService.serverHandler != null) {
								msg = LikeShareService.serverHandler
										.obtainMessage();
								msg.arg1 = 1;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break;
						case 7: // �����U��
							if (LikeShareService.serverHandler != null) {
								msg = LikeShareService.serverHandler
										.obtainMessage();
								msg.arg1 = 2;
								msg.obj = receivedString;
								msg.sendToTarget();
							}
							break;
						case 8: // �s�W�n�ͦ��\ OR ����
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

						// ���U�@��Ū���@�ǳ�
						sk.interestOps(SelectionKey.OP_READ);
					}

					// �R�����b�B�z��SelectionKey
					selector.selectedKeys().remove(sk);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}