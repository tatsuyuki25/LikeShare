package com.likeshare;


import com.likeshare.android.widget.TitleProvider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class DiffAdapter extends BaseAdapter implements TitleProvider
{

	private static final int VIEW1 = 0;
	private static final int VIEW2 = 1;
	private static final int VIEW3 = 2;
	
	private static final int VIEW_MAX_COUNT = VIEW3 + 1;
	private final String[] names = { "My Device", "My Friends" ,"Downloads"};

	private LayoutInflater mInflater;

	public DiffAdapter(Context context)
	{
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getItemViewType(int position)
	{
		return position;
	}

	@Override
	public int getViewTypeCount()
	{
		return VIEW_MAX_COUNT;
	}

	@Override
	public int getCount()
	{
		return 3;
	}

	@Override
	public Object getItem(int position)
	{
		return position;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent)
	{
		int view = getItemViewType(position);
		
		if(convertView == null)
		{
			switch(view)
			{
				case VIEW1:
					convertView = mInflater.inflate(R.layout.mydevice,null);
					
					break;
				case VIEW2:
					convertView = mInflater.inflate(R.layout.myfriends,null);
					
					break;
				case VIEW3:
					convertView = mInflater.inflate(R.layout.mydownloads,null);
			}
		}
		return convertView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
	 */
	public String getTitle(int position)
	{
		return names[position];
	}

}
