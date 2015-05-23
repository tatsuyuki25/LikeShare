package com.likeshare;

import java.io.File;
import java.util.List;

import com.likeshare.fileManager.FileType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class DownloadsAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private List<String> downloads;
	private FileType ft;

	public DownloadsAdapter(Context context,List<String> downloads)
	{
		mInflater = LayoutInflater.from(context);
		
		this.downloads = downloads;
		ft = new FileType();
		ft.createIcon(context);
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return downloads.size();
	}

	@Override
	public Object getItem(int arg0)
	{
		// TODO Auto-generated method stub
		return downloads.get(arg0);
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
			convertView = mInflater.inflate(R.layout.mydownloads_row,null);
			holder = new ViewHolder();
			holder.txtFileName = (TextView) convertView.findViewById(R.id.txtFileName);
			holder.txtDetail = (TextView) convertView.findViewById(R.id.txtDetail);
			holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if(downloads.size() != 0)
		{
			/*holder.text.setText(str[0]);
			if(str[1].equals("1"))
			{
				holder.iv.setImageBitmap(green);
			} else
			{
				holder.iv.setImageBitmap(red);
			}*/
			File f=new File(downloads.get(position).toString());
			
			holder.txtFileName.setText(f.getName());
			holder.imgIcon.setImageBitmap(ft.getFileImage(f));
			holder.txtDetail.setText(ft.getFileLastEditTime(f) + "  "
					+ ft.getFileSize(f));
		}
		return convertView;
	}

	private class ViewHolder
	{
		TextView txtFileName;
		TextView txtDetail;
		ImageView imgIcon;
	}
}
