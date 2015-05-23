package com.likeshare.video;

import java.util.ArrayList;

import com.likeshare.R;
import com.likeshare.net.LikeShareService;
import com.likeshare.net.LikeShareService.LikeShareServiceBinder;

import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ListView;

public class VideoActivity extends ListActivity
{
	private ArrayList<VideoInfo> al;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_list);
		doBindService();
		getVideoFile();
	}

	private void getVideoFile()
	{
		al = new ArrayList<VideoInfo>();
		Bitmap bitmap = null;
		ContentResolver mContentResolver = this.getContentResolver();
		Cursor cursor = mContentResolver.query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null,null,null,
				MediaStore.Video.DEFAULT_SORT_ORDER);
		if(cursor.moveToFirst())
		{
			for(int i = 0;i < cursor.getCount();i++)
			{
				VideoInfo v = new VideoInfo();
				// ID�GMediaStore.Audio.Media._ID
				int id = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				
				// �W�� �GMediaStore.Audio.Media.TITLE
				String tilte = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));

				// �M��W�GMediaStore.Audio.Media.ALBUM
				String album = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));

				// �q��W�G MediaStore.Audio.Media.ARTIST
				String artist = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));

				// ���| �GMediaStore.Audio.Media.DATA
				String url = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));

				// �`����ɪ� �GMediaStore.Audio.Media.DURATION
				int duration = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));

				// �j�p �GMediaStore.Audio.Media.SIZE
				int size = (int) cursor.getLong(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

				// ����ɶ�
				int dateTaken = cursor.getInt(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN));
				String datetime = DateFormat.format("yyyy-MM-dd kk:mm:ss",dateTaken)
						.toString();

				bitmap = ThumbnailUtils.createVideoThumbnail(url,
						Video.Thumbnails.MICRO_KIND);
				v.setId(id);
				v.setAlbum(album);
				v.setArtist(artist);
				v.setDatetime(datetime);
				v.setSize(size);
				v.setTilte(tilte);
				v.setUrl(url);
				v.setDuration(duration);
				v.setBitmap(bitmap);
				al.add(v);
				cursor.moveToNext();
			}
		}
		setListAdapter(new VideoAdapter(VideoActivity.this,al,VideoActivity.this));
	}

	@Override
	protected void onListItemClick(ListView l,View v,int position,long id)
	{
		// TODO Auto-generated method stub
		super.onListItemClick(l,v,position,id);
		Bundle data = new Bundle();
		data.putString("path",al.get(position).getUrl());
		Intent intent = new Intent();
		intent.setClass(VideoActivity.this,PlayActivity.class);
		intent.putExtras(data);
		startActivityForResult(intent,1);
	}



	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data)
	{
		super.onActivityResult(requestCode,resultCode,data);
		if(resultCode == RESULT_OK)
		{
			finish();
		}
	}
	
	/* Service */
	private void doBindService() {

		bindService(new Intent(VideoActivity.this, LikeShareService.class),
				ssserviceconnection, Context.BIND_AUTO_CREATE);
	}

	public LikeShareService lss;
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
	
}
