package com.likeshare;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.likeshare.R;


public class FriendAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private List<String> friends;
	private Bitmap red;
	private Bitmap green;

	public FriendAdapter(Context context,List<String> friends)
	{
		mInflater = LayoutInflater.from(context);
		
		this.friends = friends;
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return friends.size();
	}

	@Override
	public Object getItem(int arg0)
	{
		// TODO Auto-generated method stub
		return friends.get(arg0);
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		ViewHolder holder;
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.myfriends_row,null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.friend_name);
			holder.txtFriendEmail=(TextView) convertView.findViewById(R.id.txtFriendEmail);
			//holder.iv = (ImageView) convertView.findViewById(R.id.friend_type);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if(friends.size() != 0)
		{
			String[] str = friends.get(position).split(",");
			holder.text.setText(str[1]);
			holder.txtFriendEmail.setText(str[0]);
			/*if(str[1].equals("1"))
			{
				holder.iv.setImageBitmap(green);
			} else
			{
				holder.iv.setImageBitmap(red);
			}*/
		}
		return convertView;
	}

	private class ViewHolder
	{
		TextView text;
		ImageView iv;
		TextView txtFriendEmail;
	}
}
