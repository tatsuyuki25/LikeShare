/*
 * 檔案管理員圖片顯示方法
 * */
package com.likeshare.fileManager;

import java.io.File;
import java.util.List;

import com.likeshare.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter
{

	private LayoutInflater mInflater;
	private FileType ft;
	private List<String> items;
	private List<String> paths;

	public MyAdapter(Context context,List<String> it,List<String> ps)
	{
		mInflater = LayoutInflater.from(context);
		items = it;
		paths = ps;
		ft = new FileType();
		ft.createIcon(context);
	}

	public int getCount()
	{
		return items.size();
	}

	public Object getItem(int position)
	{
		return items.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position,View convertView,ViewGroup parent)
	{
		ViewHolder holder;

		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.file_row,null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.file_detail = (TextView) convertView
					.findViewById(R.id.file_detail);

			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if(position == selectItem)
		{
			convertView.setBackgroundColor(0xffff9900);
		} else
		{
			convertView.setBackgroundColor(0x00000000);
		}
		File f = new File(paths.get(position).toString());
		holder.text.setText(f.getName());
		/*---------------取得文件最後修改時間------------------*/
		/*
		 * String fileLastEditTime = "                                  ";
		 * Calendar cal = Calendar.getInstance(); long time = f.lastModified();
		 * SimpleDateFormat formatter = new
		 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); cal.setTimeInMillis(time);
		 * if(time != 0) fileLastEditTime = formatter.format(cal.getTime());
		 */
		/*---------------取得文件大小------------------*/
		// String file_size = formatSize(f.length());
		holder.icon.setImageBitmap(ft.getFileImage(f));
		holder.file_detail.setText(ft.getFileLastEditTime(f) + "  "
				+ ft.getFileSize(f));
		/*
		 * if(f.isDirectory()) { holder.icon.setImageBitmap(mIcon3);
		 * holder.file_detail.setText(fileLastEditTime); } else {
		 * holder.icon.setImageBitmap(mIcon4);
		 * holder.file_detail.setText(fileLastEditTime+"  "+file_size); }
		 */

		return convertView;
	}

	public void setSelectItem(int selectItem)
	{

		this.selectItem = selectItem;

	}

	private int selectItem = -1;

	private class ViewHolder
	{
		TextView text;
		TextView file_detail;
		ImageView icon;
	}
}
