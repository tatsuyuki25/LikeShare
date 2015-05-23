package com.likeshare.video;

import android.graphics.Bitmap;

public class VideoInfo
{
	private int id;
	private String tilte;
	private String album;
	private String artist;
	private String url;
	private int duration;
	private int size;
	private String datetime;
	private Bitmap bitmap;
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getTilte()
	{
		return tilte;
	}
	public void setTilte(String tilte)
	{
		this.tilte = tilte;
	}
	public String getAlbum()
	{
		return album;
	}
	public void setAlbum(String album)
	{
		this.album = album;
	}
	public String getArtist()
	{
		return artist;
	}
	public void setArtist(String artist)
	{
		this.artist = artist;
	}
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public int getDuration()
	{
		return duration;
	}
	public void setDuration(int duration)
	{
		this.duration = duration;
	}
	public int getSize()
	{
		return size;
	}
	public void setSize(int size)
	{
		this.size = size;
	}
	public String getDatetime()
	{
		return datetime;
	}
	public void setDatetime(String datetime)
	{
		this.datetime = datetime;
	}
	public Bitmap getBitmap()
	{
		return bitmap;
	}
	public void setBitmap(Bitmap bitmap)
	{
		this.bitmap = bitmap;
	}


}
