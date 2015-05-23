/*
 * 檔案管理員的方法 用於呼叫
 * */
package com.likeshare.fileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.net.Uri;

class PackageAdapter
{
	private List<String> items = null;
	private List<String> paths = null;

	public PackageAdapter(List<String> items,List<String> paths)
	{
		this.items = items;
		this.paths = paths;
	}

	protected List<String> getitems()
	{
		return items;
	}

	protected List<String> getpaths()
	{
		return paths;
	}
}

public class File_Explorer
{
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/";
	private static MyAdapter adapter;

	public File_Explorer()
	{

	}

	protected PackageAdapter getFileDir(String filePath)
	{
		// mPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();
		Arrays.sort(files,fileC);

		for(int i = 0;i < files.length;i++)
		{
			File file = files[i];
			boolean b = false;
			if(file.isDirectory())
			{
				File[] f2 = file.listFiles();
				if(f2 == null)
					b = true;
			}
			if(!b)
			{
				items.add(file.getName());
				paths.add(file.getPath());
			}
		}
		List<String> tmpi = new ArrayList<String>();
		List<String> tmpp = new ArrayList<String>();
		for(int i = 0;i < items.size();i++)
		{

			File file = new File(paths.get(i));
			if(file.isDirectory())
			{
				tmpi.add(items.get(i));
				tmpp.add(paths.get(i));
			}

		}
		for(int i = 0;i < items.size();i++)
		{

			File file = new File(paths.get(i));
			if(file.isFile())
			{
				tmpi.add(items.get(i));
				tmpp.add(paths.get(i));
			}

		}
		items = tmpi;
		paths = tmpp;

		// adapter = new MyAdapter(this,items,paths);
		// setListAdapter(adapter);
		PackageAdapter pa = new PackageAdapter(items,paths);
		return pa;
	}

	private Comparator fileC = new Comparator()
	{

		public int compare(Object lhs,Object rhs)
		{
			// TODO Auto-generated method stub
			File file1 = (File) lhs;
			File file2 = (File) rhs;
			String s1 = file1.getName();
			String s2 = file2.getName();
			return s1.compareToIgnoreCase(s2);
			/*
			 * long diff = file1.length() - file2.length(); if (diff > 0) return 1;
			 * else if (diff == 0) return 0; else return -1;
			 */
		}

	};

	protected String getMIMEType(File f)
	{
		String type = "";
		String fName = f.getName();
		String end = fName.substring(fName.lastIndexOf(".") + 1,fName.length())
				.toLowerCase();

		if(end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav"))
		{
			type = "audio";
		} else if(end.equals("3gp") || end.equals("mp4"))
		{
			type = "video";
		} else if(end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp"))
		{
			type = "image";
		} else
		{
			type = "*";
		}
		type += "/*";
		return type;
	}

	protected void openFile(File f)
	{
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String type = getMIMEType(f);
		intent.setDataAndType(Uri.fromFile(f),type);
		// startActivity(intent);
	}
}
