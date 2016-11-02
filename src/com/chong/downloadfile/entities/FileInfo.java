/*
 * @Title FileInfo.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description：
 * @author Yann
 * @date 2015-8-7 下午10:13:28
 * @version 1.0
 */
package com.chong.downloadfile.entities;

import java.io.Serializable;

/** 
 * 文件信息
 * @author Yann
 * @date 2015-8-7 下午10:13:28
 */
public class FileInfo implements Serializable
{
	public static final String STATU_NORMAL="normal";
	public static final String STATU_DOWNLOADING="downloading";
	public static final String STATU_FINISH="finished";
	public static final String STATU_PAUSE="pause";
	public static final String STATU_CANCEL="cancel";
	
	private String id;
	private String url;
	private String fileName;
	private int length;//单位 bit
	private int finished;//单位 %
	private String statu="normal";//当前下载状态  normal 未下载，downloading 正在下载  finish 完成下载  pause 暂停下载
	
	
	
	public String getStatu() {
		return statu;
	}

	public void setStatu(String statu) {
		this.statu = statu;
	}

	/** 
	 *@param id
	 *@param url
	 *@param fileName
	 *@param length
	 *@param finished
	 */
	public FileInfo(String id, String url, String fileName, int length,
			int finished)
	{
		this.id = id;
		this.url = url;
		this.fileName = fileName;
		this.length = length;
		this.finished = finished;
	}
	
	public FileInfo(String id, String url, String fileName){
		this.id = id;
		this.url = url;
		this.fileName = fileName;
		this.length = 0;
		this.finished = 0;
	}
	public FileInfo(){
		
	}
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public String getFileName()
	{
		return fileName;
	}
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	public int getLength()
	{
		return length;
	}
	public void setLength(int length)
	{
		this.length = length;
	}
	public int getFinished()
	{
		return finished;
	}
	public void setFinished(int finished)
	{
		this.finished = finished;
	}
	@Override
	public String toString()
	{
		return "FileInfo [id=" + id + ", url=" + url + ", fileName=" + fileName
				+ ", length=" + length + ", finished=" + finished + "]";
	}
	
	
}
