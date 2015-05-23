package com.likeshare.video;

import java.io.IOException;
import java.util.ArrayList;

import com.likeshare.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoAdapter extends BaseAdapter
{

		private LayoutInflater mInflater;
		private ArrayList<VideoInfo> al;
		private VideoActivity va;
		public VideoAdapter(Context context, ArrayList<VideoInfo> al,VideoActivity va)
		{
				mInflater = LayoutInflater.from(context);
				this.al = al;
				this.va = va;
		}

		@Override
		public int getCount()
		{
				return al.size();
		}

		@Override
		public Object getItem(int position)
		{
				return al.get(position);
		}

		@Override
		public long getItemId(int position)
		{
				return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
				ViewHolder holder;

				if (convertView == null)
				{
						convertView = mInflater.inflate(R.layout.video_row, null);
						holder = new ViewHolder();
						holder.text = (TextView) convertView.findViewById(R.id.name);
						holder.path = (TextView) convertView.findViewById(R.id.path);
						holder.time = (TextView) convertView.findViewById(R.id.time);
						holder.icon = (ImageView) convertView.findViewById(R.id.iv);
						holder.stream = (Button) convertView.findViewById(R.id.videostream);
						convertView.setTag(holder);
				} 
				else
				{
						holder = (ViewHolder) convertView.getTag();
				}
				holder.text.setText(al.get(position).getTilte());
				holder.path.setText(al.get(position).getUrl());
				holder.icon.setImageBitmap(al.get(position).getBitmap());
				int second = al.get(position).getDuration()/1000;
				int min = second/60;
				second = second % 60;
				holder.time.setText(min+":"+second);
				holder.stream.setFocusable(false);
				holder.stream.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v) 
					{
						try {
							va.lss.connectVideo(va.lss.getDefaultDevice(), al.get(position).getUrl(), 0);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				});
				return convertView;
		}

		private class ViewHolder
		{
				TextView text;
				TextView path;
				TextView time;
				ImageView icon;
				Button stream;
		}

		
}
