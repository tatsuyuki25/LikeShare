package com.likeshare.fileManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.likeshare.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FileType
{
	private Bitmap folder;
	private Bitmap default_file;
	private Bitmap compression;
	private Bitmap powerpoint;
	private Bitmap word;
	private Bitmap excel;
	private Bitmap pdf;
	private Bitmap photo;
	private Bitmap video;
	private Bitmap audio;

	public void createIcon(Context context)
	{
		folder = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.file_edit_folder);
		default_file = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.file_edit_file);
		compression = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.compression);
		powerpoint = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.powerpoint);
		word = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.word);
		excel = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.excel);
		pdf = BitmapFactory.decodeResource(context.getResources(),R.drawable.pdf);
		photo = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.photo);
		video = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.video);
		audio = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.audio);
	}

	public Bitmap getFileImage(File f)
	{
		String fName = f.getName();
		String end = fName.substring(fName.lastIndexOf(".") + 1,fName.length())
				.toLowerCase();
		if(end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav"))
		{
			return audio;
		} else if(end.equals("3gp") || end.equals("mp4") || end.equals("rmvb")
				|| end.equals("avi"))
		{
			return video;
		} else if(end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp"))
		{
			return photo;
		} else if(end.equals("7z") || end.equals("zip") || end.equals("rar")
				|| end.equals("tar") || end.equals("gz") || end.equals("bz"))
		{
			return compression;
		} else if(end.equals("doc") || end.equals("docx"))
		{
			return word;
		} else if(end.equals("ppt") || end.equals("pptx"))
		{
			return powerpoint;
		} else if(end.equals("xls") || end.equals("xlsx"))
		{
			return excel;
		} else if(end.equals("pdf"))
		{
			return pdf;
		} else if(f.isDirectory())
		{
			return folder;
		} else
		{
			return default_file;
		}
	}

	/*---------------取得文件最後修改時間------------------*/
	public String getFileLastEditTime(File f)
	{
		String fileLastEditTime = "                                  ";
		Calendar cal = Calendar.getInstance();
		long time = f.lastModified();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cal.setTimeInMillis(time);
		if(time != 0)
			fileLastEditTime = formatter.format(cal.getTime());

		return fileLastEditTime;
	}

	/*---------------取得文件大小------------------*/
	public String getFileSize(File f)
	{
		if(!f.isDirectory())
			return formatSize(f.length());
		else
			return "";
	}

	private String formatSize(long size)
	{
		long SIZE_KB = 1024;
		long SIZE_MB = SIZE_KB * 1024;
		long SIZE_GB = SIZE_MB * 1024;
		if(size < SIZE_KB)
		{
			return String.format("%d B",(int) size);
		} else if(size < SIZE_MB)
		{
			return String.format("%.2f KB",(float) size / SIZE_KB);
		} else if(size < SIZE_GB)
		{
			return String.format("%.2f MB",(float) size / SIZE_MB);
		} else
		{
			return String.format("%.2f GB",(float) size / SIZE_GB);
		}
	}
}
