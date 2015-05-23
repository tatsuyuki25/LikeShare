package com.likeshare;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.likeshare.DiffAdapter;
import com.likeshare.R;
import com.likeshare.android.widget.ViewFlow;
import com.likeshare.android.widget.TitleFlowIndicator;
import com.likeshare.fileManager.FileManager;
import com.likeshare.net.LikeShareService;
import com.likeshare.net.LikeShareService.LikeShareServiceBinder;
import com.likeshare.video.VideoActivity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class MultiViewManager extends Activity {
	private ViewFlow viewFlow;
	private ProgressBar selectDevice, pbFriendsSelectDevice;
	private ImageButton imgbtnDeviceExit;

	private Handler handler;
	protected String[] input, friendInput;
	private ListView friendsView,lvDownloads;
	private GridView gridView, gvFriendsDevice;
	private ArrayList<String> friendDevice,downloads,friends;
	private String localTempImgFileName = "temp";// 相片的暫存名稱
	private final int CAPTURE_CODE = 100;
	private String selectPath;
	protected static String selectMac; // 要傳送的MAC
	protected static final int MENU_LOGOUT = Menu.FIRST;
	private ImageButton imgbtnAddFriend,imgbtnMovice;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title_layout);

		/*
		 * StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		 * .detectDiskReads().detectDiskWrites().detectNetwork() // or //
		 * .detectAll() // for // all // detectable // problems
		 * .penaltyLog().build()); StrictMode.setVmPolicy(new
		 * StrictMode.VmPolicy.Builder()
		 * .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath() .build());
		 * viewFlow = (ViewFlow) findViewById(R.id.viewflow); DiffAdapter
		 * adapter = new DiffAdapter(this); viewFlow.setAdapter(adapter);
		 */
		// setHandler(cmdHandler);
		viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		DiffAdapter adapter = new DiffAdapter(this);
		viewFlow.setAdapter(adapter);
		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
		indicator.setTitleProvider(adapter);
		viewFlow.setFlowIndicator(indicator);

		selectDevice = (ProgressBar) findViewById(R.id.selectDevice);
		pbFriendsSelectDevice = (ProgressBar) findViewById(R.id.pbFriendsSelectDevice);
		pbFriendsSelectDevice.setVisibility(View.GONE);

		imgbtnDeviceExit = (ImageButton) findViewById(R.id.imgbtnDeviceExit);
		imgbtnDeviceExit.setOnClickListener(imgbtnDeviceExitListener);

		imgbtnMovice = (ImageButton) findViewById(R.id.movies);
		imgbtnMovice.setOnClickListener(imgbtnMoviceListener);
		
		doBindService(); // Bind Service

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (lss != null) {
						lss.setHandler(cmdHandler);
						getMyAllDevice();
						getMyFriends();
						getMyDownloads();

						break;
					}
				}
			}
		}).start();
		/**
		 * 更新畫面
		 */
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.arg1) {
				case 1:
					showMyDevice();
					break;
				case 2:
					showMyFriendsView();
					break;
				case 3:
					 setDownloadsView();
					break;
				case 4:
					showFriendDevice(msg.arg2);
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (lss != null) {
						Log.i("Reeeeeeeeeeeeeeeee","RE");
						//lss.scan();
						break;
					}
				}
			}
		}).start();
	}

	/**
	 * 請求伺服器 送出我的設備清單
	 */
	private void getMyAllDevice() {
		try {
			lss.sendMsg("2," + lss.getAccount());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getMyFriends() {
		try {
			lss.sendMsg("3," + lss.getAccount());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Handler 來自 LikeShareService 的 訊息
	 */
	private Handler cmdHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 1: // 取得我的所有設備
				Log.i("cmdHandler", "cmdHander進入");
				setDeviceLogin((String) msg.obj, "1");
				break;
			case 2: // 有設備上線
				setDeviceOnline((String) msg.obj);
				// setDeviceLogout((String) msg.obj);
				break;
			case 3: // 取得好友名單
				setFriends((String) msg.obj);
				break;
			case 4: // 取得好友擁有設備
				setDeviceLogin((String) msg.obj, "2");
				break;
			case 5: //更新下載清單
				getMyDownloads();
				break;
			case 6: // 更新好友清單
				getMyFriends();
				break;
			case 7: // 新增好友 成功 OR 失敗
				if(msg.arg2 == 1)
				{
					getMyFriends();
					Toast.makeText(getApplicationContext(),
							"Add Friend successfully!",Toast.LENGTH_SHORT).show();
				} else
				{
					Toast.makeText(getApplicationContext(),
							"Add Friend failed! or That account does not exist!",
							Toast.LENGTH_SHORT).show();
				}
				break;

			}
		}
	};

	/**
	 * 設定朋友清單
	 * 
	 * @param msg
	 */

	private void setFriends(String msg) {
		String[] tmp = msg.split(",");
		ArrayList<String> frineds = new ArrayList<String>();
		for (int i = 0; i < Integer.parseInt(tmp[1]) * 3; i += 3) // "4,size,acc,uid,type"
		{
			frineds.add(tmp[i + 2] + "," + tmp[i + 3] + "," + tmp[i + 4]);
		}
		lss.friends = frineds;
		Message msg1 = handler.obtainMessage();
		msg1.arg1 = 2;
		msg1.sendToTarget();
	}

	/**
	 * 顯示好友清單並且監聽
	 */
	private void showMyFriendsView() {
		friends = lss.friends;
		friendsView = (ListView) findViewById(R.id.lvFriends);
		friendsView.setAdapter(new FriendAdapter(this, friends));
		friendsView
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
					@Override
					public void onCreateContextMenu(ContextMenu arg0,
							View arg1, ContextMenuInfo arg2) {
						// TODO Auto-generated method stub
						int selectedPosition = ((AdapterContextMenuInfo) arg2).position;
						String[] friendsName = friends.get(selectedPosition)
								.toString().split(","); // 取得點擊的名稱
						arg0.setHeaderTitle(friendsName[1]);

						arg0.setHeaderIcon(android.R.drawable.ic_dialog_info);
						arg0.add(0, 2, 0, "Rename");
						arg0.add(0, 3, 0, "Delete");

					}

				});
		friendsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				pbFriendsSelectDevice.setVisibility(View.VISIBLE);
				Message msg = handler.obtainMessage();
				msg.arg1 = 4;
				msg.arg2 = arg2;
				msg.sendToTarget();
			}
		});
		imgbtnAddFriend = (ImageButton) findViewById(R.id.imgbtnAddFriend);
		imgbtnAddFriend.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final EditText edtFriendEmail = new EditText(MultiViewManager.this);
				DialogInterface.OnClickListener btnOk = new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog,int which)
					{
						if(!edtFriendEmail.getText().toString().trim().equals(""))
						{
							boolean b = true;
							for(int i = 0;i < friends.size();i++)
							{
								String[] tmp = friends.get(i).split(",");
								if(tmp[0].equals(edtFriendEmail.getText().toString()))
								{
									b = false;
									break;
								}
							}
							if(b){
								try {
									lss.sendMsg("8," + edtFriendEmail.getText().toString());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							else
								Toast.makeText(getApplicationContext(),
										"That Account is already your friend.",
										Toast.LENGTH_SHORT).show();
						}
					}
				};
				new AlertDialog.Builder(MultiViewManager.this)
						.setTitle("Please enter your friend e-mail.")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(edtFriendEmail).setPositiveButton("Add",btnOk)
						.setNegativeButton("Cancel",null).show();
			}
		});

	}

	/**
	 * 顯示好友設備清單並監聽
	 * 
	 * @param arg2
	 */
	private void showFriendDevice(int arg2) {
		final ArrayList<String> friends = lss.friends;
		String[] friendName = friends.get(arg2).toString().split(","); // 取得點擊的名稱
		try {
			lss.sendMsg("4," + friendName[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 更新我的設備清單
	 */
	private void showMyDevice() {
		ArrayList<String> device = lss.device;
		int size = device.size();
		int[] image = new int[size];
		String[] imgText = new String[size];
		int[] online = new int[size];
		String[] tmp;
		for (int i = 0; i < size; i++) {
			tmp = device.get(i).split(","); // id,name,mac,type
			if (tmp[0].equals("android")) {
				image[i] = R.drawable.smart_phone;
			} else {
				image[i] = R.drawable.notebook;
			}
			imgText[i] = tmp[1];
			if (tmp[3].equals("1")) {
				online[i] = R.drawable.connection;
			}
		}
		setMyDeviceGridView(image, imgText, online);

	}

	/**
	 * 設定設備上限狀態
	 * 
	 * @param Mac
	 */
	private void setDeviceOnline(String Mac) {

		for (int i = 0; i < lss.device.size(); i++) {
			if (lss.device.get(i).indexOf(Mac) > 0) {
				// String s = lss.device.get(i).substring(0,
				// (lss.device.get(i).lastIndexOf("0")));
				String s = lss.device.get(i).substring(0,
						lss.device.get(i).length() - 1);
				s += "1";
				lss.device.set(i, s);
				break;
			}
		}
		// 更新頁面
		Message msg = handler.obtainMessage();
		msg.arg1 = 1;
		msg.sendToTarget();
	}

	/**
	 * 收到的 所有上線狀態 並設定 第一次執行 who 1= 自己 2=好友
	 * 
	 * @param message
	 */
	private void setDeviceLogin(String message, String who) // 2,size,id,name,mac,type,-----
	{

		String[] tm = message.split(",");
		int[] image = new int[Integer.parseInt(tm[1])];
		String[] imgText = new String[Integer.parseInt(tm[1])];
		int[] online = new int[Integer.parseInt(tm[1])];
		ArrayList<String> tmp = new ArrayList<String>();
		// lss.device = new ArrayList<String>();
		int k = 0;
		for (int i = 0; i < Integer.parseInt(tm[1]) * 4; i += 4, k++) {
			tmp.add(tm[i + 2] + "," + tm[i + 3] + "," + tm[i + 4] + ","
					+ tm[i + 5]);
			Log.i("getDevice", tm[i + 2] + "," + tm[i + 3] + "," + tm[i + 4]
					+ "," + tm[i + 5]);

			if (tm[i + 2].equals("android")) {
				image[k] = R.drawable.smart_phone;
			} else {
				image[k] = R.drawable.notebook;
			}
			imgText[k] = tm[i + 3];
			if (tm[i + 5].equals("1")) {
				online[k] = R.drawable.connection;
			}
		}
		if (who.equals("1")) {
			lss.device = tmp;
			setMyDeviceGridView(image, imgText, online);
		} else {
			friendDevice = tmp;
			setFriendDeviceGridView(image, imgText, online);
		}

		// id,name,mac,type

	}

	/**
	 * 設定我的設備到 GridView 並監聽
	 * 
	 * @param image
	 * @param imgText
	 * @param online
	 */
	private void setMyDeviceGridView(int[] image, String[] imgText, int[] online) {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (int j = 0; j < image.length; j++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("image", image[j]);
			item.put("text", imgText[j]);
			item.put("online", online[j]);
			items.add(item);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, items,
				R.layout.grid_item, new String[] { "image", "text", "online" },
				new int[] { R.id.image, R.id.text, R.id.online });
		gridView = (GridView) findViewById(R.id.mygridview);
		gridView.setNumColumns(3);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// myDeviceMac = myDeviceMacTmp[0].split(",");
				// Toast.makeText(getApplicationContext(),
				// "Your choice is "+imgText[position],
				// Toast.LENGTH_SHORT).show();
				if (position == 0) {
					Toast.makeText(getApplicationContext(), "Your Device",
							Toast.LENGTH_SHORT).show();
				} else {
					String[] tmp = lss.device.get(position).split(",");
					if (tmp[3].equals("1")) {

						openPopupwin(position, tmp[2]);
						// Toast.makeText(getApplicationContext(),
						// "Online " +
						// imgText[position],Toast.LENGTH_SHORT).show();

					} else
						Toast.makeText(getApplicationContext(), "off-line",
								Toast.LENGTH_SHORT).show();
				}
			}
		});
		gridView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0,View arg1,int position,long id)
			{
				if (position == 0) {
					Toast.makeText(getApplicationContext(), "Your Device",
							Toast.LENGTH_SHORT).show();
				} else {
					String[] tmp = lss.device.get(position).split(",");
					if (tmp[3].equals("1")) {
						lss.setDefaultDevice(tmp[2]);
						Toast.makeText(getApplicationContext(), "Set default device",
							Toast.LENGTH_SHORT).show();
					}else
						Toast.makeText(getApplicationContext(), "off-line",
								Toast.LENGTH_SHORT).show();
					
				}
				return false;
			}
			
		});
		selectDevice.setVisibility(View.GONE); // 圓圈進度條
	}

	/**
	 * 設定好友設備到 GridView 並監聽
	 * 
	 * @param image
	 * @param imgText
	 * @param online
	 */
	private void setFriendDeviceGridView(int[] image, String[] imgText,
			int[] online) {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (int j = 0; j < image.length; j++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("image", image[j]);
			item.put("text", imgText[j]);
			item.put("online", online[j]);
			items.add(item);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, items,
				R.layout.grid_item, new String[] { "image", "text", "online" },
				new int[] { R.id.image, R.id.text, R.id.online });
		gvFriendsDevice = (GridView) findViewById(R.id.gvFriendsDevice);
		gvFriendsDevice.setNumColumns(3);
		gvFriendsDevice.setAdapter(adapter);
		gvFriendsDevice.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String[] tmp = friendDevice.get(position).split(",");
				if (tmp[3].equals("1")) {
					openPopupwin(position, tmp[2]);

				} else
					Toast.makeText(getApplicationContext(), "off-line",
							Toast.LENGTH_SHORT).show();
			}

		});
		pbFriendsSelectDevice.setVisibility(View.GONE); // GONE 停止

	}

	private String[] menu_name_array = { "拍照傳送", "傳送檔案", "傳送照片", "離開" };
	int[] menu_image_array = { android.R.drawable.ic_menu_camera,
			android.R.drawable.ic_menu_upload,
			android.R.drawable.ic_menu_gallery,
			android.R.drawable.ic_menu_revert };
	private GridView menuGrid;
	private PopupWindow popupWindow;

	/**
	 * 功能選單
	 * 
	 * @param position
	 */
	private void openPopupwin(int position, String MAC) {
		selectMac = MAC;
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
				R.layout.gridview_pop, null, true);
		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		menuGrid.requestFocus();
		menuGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:
					camera();
					break;
				case 1:
					Bundle bundle = new Bundle();
					bundle.putString("MAC",selectMac);
					Intent intent = new Intent();
					intent.setClass(MultiViewManager.this,FileManager.class);
					intent.putExtras(bundle);
					startActivity(intent);
					
					/*startActivity(new Intent(MultiViewManager.this,
							FileManager.class));*/
					break;
				case 2:

					startActivityForResult(
							new Intent(
									Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
							1);
					break;
				case 3:
					popupWindow.dismiss();
					break;
				}

			}
		});
		menuGrid.setOnKeyListener(new OnKeyListener() {// MENU 選單
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_MENU:
					if (popupWindow != null && popupWindow.isShowing()) {
						popupWindow.dismiss();
					}
					break;
				}
				System.out.println("menuGridfdsfdsfdfd");
				return true;
			}
		});
		popupWindow = new PopupWindow(menuView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setAnimationStyle(R.style.PopupAnimation); // 動畫
		popupWindow.showAtLocation(findViewById(R.id.parent), Gravity.CENTER
				| Gravity.CENTER, 0, 0);// popup 顯示在 parent 上 從 0 0 開始顯示
		popupWindow.update();
	}

	private ListAdapter getMenuAdapter(String[] menuNameArray, // POP 方法
			int[] menuImageArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", menuImageArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });
		return simperAdapter;

	}

	/**
	 * camera
	 */
	protected void camera() {
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/DCIM/");// 設置暫存路徑
		if (!dir.exists()) {
			dir.mkdir();
		}
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		localTempImgFileName = df.format(new Date()) + ".jpg";
		File f = new File(dir, localTempImgFileName);
		Uri u = Uri.fromFile(f);
		intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
		startActivityForResult(intent, CAPTURE_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) { // 沒有完成拍照
			return;
		} else if (requestCode == CAPTURE_CODE && resultCode == RESULT_OK) { // 完成拍照

			File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/DCIM/");
			File f = new File(dir.getAbsoluteFile(), localTempImgFileName);
			selectPath = f.getAbsolutePath();
			// Toast.makeText(this,myDeviceMac[selectDev] + ":" +
			// f.getAbsolutePath(),Toast.LENGTH_SHORT).show(); // 取得照片路徑
			connectImage(selectMac, selectPath);

		} else {

			Uri originalUri = data.getData();
			ContentResolver resolver = getContentResolver();

			Cursor cursor = resolver.query(originalUri, null, null, null, null);
			cursor.moveToFirst();

			String imagePath = cursor.getString(1); // 已經包含檔案名稱和附檔名
			selectPath = imagePath;
			// Toast.makeText(this,"相簿:" + myDeviceMac[selectDev] + ":" +
			// imagePath,
			// Toast.LENGTH_SHORT).show();
			connectImage(selectMac, selectPath);
		}
	}
	private void setDownloadsView()
	{
		lvDownloads = (ListView) findViewById(R.id.downloads_list);
		lvDownloads.setAdapter(new DownloadsAdapter(this,downloads));
		lvDownloads
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener()
				{
					@Override
					public void onCreateContextMenu(ContextMenu arg0,View arg1,
							ContextMenuInfo arg2)
					{
						// TODO Auto-generated method stub
						int selectedPosition = ((AdapterContextMenuInfo) arg2).position;
						File file = new File(downloads.get(selectedPosition));
						arg0.setHeaderIcon(android.R.drawable.ic_dialog_info);
						arg0.setHeaderTitle(file.getName());
						arg0.add(0,0,0,"Deleted from the device");
						arg0.add(0,1,0,"Clear history");
					}

				});

		lvDownloads.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0,View arg1,int arg2,
					long arg3)
			{
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(android.content.Intent.ACTION_VIEW);
				File file = new File(downloads.get(arg2));
				String type = getMIMEType(file);
				intent.setDataAndType(Uri.fromFile(file),type);
				startActivity(intent);
			}

		});

	}
	private void getMyDownloads()
	{
		downloads = new ArrayList<String>();

		SharedPreferences remdname = getSharedPreferences("download",
				Context.MODE_PRIVATE);
		String downloadHistory = remdname.getString("downloadHistory","");
		if(downloadHistory.length() != 0)
		{
			String[] tmp = downloadHistory.split(",");
			for(int i = 0;i < tmp.length;i++)
			{
				downloads.add(Environment.getExternalStorageDirectory().getAbsolutePath()+"/download/" + tmp[i]);
			}
		}
		//downloads.add("/mnt/sdcard/download/ff.7z");
	
		Message msg = handler.obtainMessage();
		msg.arg1 = 3;
		msg.sendToTarget();
	}
	private void delMyDownloads(String name)
	{
		downloads = new ArrayList<String>();
		SharedPreferences remdname = getSharedPreferences("download",
				Context.MODE_PRIVATE);
		String downloadHistory = remdname.getString("downloadHistory","");
		SharedPreferences.Editor edit = remdname.edit();
		String[] tmp = downloadHistory.split(",");
		String ss = "";
		for(int i = 0;i < tmp.length - 1;i++)
		{
			if(!tmp[i].equals(name))
				ss += tmp[i] + ",";
		}
		try
		{
			downloadHistory = ss.substring(0,ss.length() - 1); // 去除最後逗號
		} catch(StringIndexOutOfBoundsException e)
		{
			downloadHistory = "";
		}
		edit.putString("downloadHistory",downloadHistory);
		edit.commit();
		getMyDownloads();
	}
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		// TODO Auto-generated method stub

		final int selectedPosition = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
		final File file;
		switch(item.getItemId())
		{
			case 0: // 移除
				file = new File(downloads.get(selectedPosition));
				new AlertDialog.Builder(this)
						.setTitle("Are you sure ?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener()
								{

									@Override
									public void onClick(DialogInterface dialog,int which)
									{
										//new AlertDialog.Builder(MultiViewManager.this)
										//		.setMessage("Delete").create().show();
										Toast.makeText(MultiViewManager.this,
												"Delete success!",
												Toast.LENGTH_LONG).show();
										
										delMyDownloads(file.getName());
										file.delete();

									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener()
								{

									@Override
									public void onClick(DialogInterface dialog,int which)
									{
										//new AlertDialog.Builder(MultiViewManager.this)
											//	.setMessage("Cancel").create().show();
										Toast.makeText(MultiViewManager.this,
												"Cancel!",
												Toast.LENGTH_LONG).show();
									}
								}).show();
				break;
			case 1: // 清除
				file = new File(downloads.get(selectedPosition));
				delMyDownloads(file.getName());
				break;
			case 2:
				// 好友改名
				final EditText edtFriendName = new EditText(MultiViewManager.this);
				DialogInterface.OnClickListener btnOk = new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog,int which)
					{
						if(!edtFriendName.getText().toString().trim().equals(""))
						{
							String[] tmp = friends.get(selectedPosition).split(",");
							ArrayList<String> tmp2 = new ArrayList<String>();
							try {
								lss.sendMsg("6,"+tmp[0]+","+edtFriendName.getText().toString());
								for(int i = 0;i < friends.size();i++)
								{
									if(friends.get(i).equals(friends.get(selectedPosition)))
									{
										tmp2.add(tmp[0]+","+edtFriendName.getText().toString()+","+tmp[2]);
									}
									else
									{
										tmp2.add(friends.get(i));
									}
								}
								friends = tmp2;
								if (cmdHandler != null) {
									Message msg = cmdHandler.obtainMessage();
									msg.arg1 = 6;
									msg.sendToTarget();
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}
				};
				new AlertDialog.Builder(MultiViewManager.this)
						.setTitle("Please enter your friend e-mail.")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(edtFriendName).setPositiveButton("Add",btnOk)
						.setNegativeButton("Cancel",null).show();
				break;
			case 3:
				new AlertDialog.Builder(this)
						.setTitle("Are you sure ?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener()
								{

									@Override
									public void onClick(DialogInterface dialog,int which)
									{
										new AlertDialog.Builder(MultiViewManager.this)
												.setMessage("Delete").create().show();
										// 刪除好友
										String[] tmp = friends.get(selectedPosition)
												.split(",");
										try {
											lss.sendMsg("7,"+tmp[0]);
											friends.remove(selectedPosition);
											if (cmdHandler != null) {
												Message msg = cmdHandler.obtainMessage();
												msg.arg1 = 6;
												msg.sendToTarget();
											}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener()
								{

									@Override
									public void onClick(DialogInterface dialog,int which)
									{
										new AlertDialog.Builder(MultiViewManager.this)
												.setMessage("Cancel").create().show();
									}
								}).show();
				break;
			default:
				break;
		}

		return super.onContextItemSelected(item);
	}


	private void connect(String mac) {
		// myDialog = ProgressDialog.show(this,"建立連線","連線中",true);
		// lss.connect(mac);
	}

	private void connectImage(String mac, String path) {
		// myDialog = ProgressDialog.show(this,"建立連線","連線中",true);
		try {
			lss.connectImage(mac, path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 離開 不登出
	 */
	private Button.OnClickListener imgbtnDeviceExitListener = new Button.OnClickListener() {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent it = new Intent(Intent.ACTION_MAIN);
			it.addCategory(Intent.CATEGORY_HOME);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(it);
			// System.exit(0);
		}
	};
	
	private Button.OnClickListener imgbtnMoviceListener = new Button.OnClickListener() {
		public void onClick(View v) {
			
			Intent intent = new Intent();
			intent.setClass(MultiViewManager.this,VideoActivity.class);
			startActivity(intent);
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/**
	 * Service 方法
	 */
	private void doBindService() {

		bindService(new Intent(MultiViewManager.this, LikeShareService.class),
				ssserviceconnection, Context.BIND_AUTO_CREATE);
	}

	private LikeShareService lss;
	private ServiceConnection ssserviceconnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			LikeShareServiceBinder fsb = (LikeShareServiceBinder) service;
			lss = (LikeShareService) fsb.getService();
		};

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			lss = null;
		};
	};

	/**
	 * MENU 登出選單
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_LOGOUT, 0, "Logout");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 針對 item selection id 進行動作處理
		switch (item.getItemId()) {
		case MENU_LOGOUT:
			stopService(new Intent(this, LikeShareService.class));
			System.exit(0);
			break;
		}
		return true;
	}
	protected String getMIMEType(File f)
	{
		String type = "";
		String fName = f.getName();
		String end = fName.substring(fName.lastIndexOf(".") + 1,fName.length())
				.toLowerCase();

		if(end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav"))
		{
			type = "audio/*";
		} else if(end.equals("3gp") || end.equals("mp4"))
		{
			type = "video/*";
		} else if(end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp"))
		{
			type = "image/*";
		} else if(end.equals("apk"))
		{
			type = "application/vnd.android.package-archive";
		} else if(end.equals("pdf"))
		{
			type = "application/pdf";
		} else if(end.equals("txt"))
		{
			type = "text/plain";
		} else if(end.equals("doc") || end.equals("docx"))
		{
			type = "application/msword";
		} else if(end.equals("xls") || end.equals("xlsx"))
		{
			type = "application/vnd.ms-excel";
		} else if(end.equals("ppt") || end.equals("pptx"))
		{
			type = "application/vnd.ms-powerpoint";
		} else if(end.equals("zip"))
		{
			type = "application/zip";
		} else if(end.equals("rar"))
		{
			type = "application/x-rar-compressed";
		} else if(end.equals("tar"))
		{
			type = "application/x-tar";
		} else if(end.equals("tgz"))
		{
			type = "application/x-compressed";
		} else if(end.equals("tgz"))
		{
			type = "application/x-compressed";
		} else if(end.equals("7z"))
		{
			type = "application/*";
		} else
		{
			type = "*/*";
		}

		return type;
	}

}
