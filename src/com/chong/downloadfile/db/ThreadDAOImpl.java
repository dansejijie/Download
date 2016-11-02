/*
 * @Title ThreadDAOImpl.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description：
 * @author Yann
 * @date 2015-8-8 上午11:00:38
 * @version 1.0
 */
package com.chong.downloadfile.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.entities.ThreadInfo;

/** 
 * 数据访问接口实现
 * @author Yann
 * @date 2015-8-8 上午11:00:38
 */
public class ThreadDAOImpl implements ThreadDAO
{
	private DBHelper mHelper = null;
	
	public ThreadDAOImpl(Context context)
	{
		mHelper = DBHelper.getInstance(context);
	}
	
	/**
	 * @see com.chong.downloadfile.db.ThreadDAO#insertThread(com.chong.downloadfile.entities.ThreadInfo)
	 */
	@Override
	public synchronized void insertThread(ThreadInfo threadInfo)
	{
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
				new Object[]{threadInfo.getId(), threadInfo.getUrl(),
				threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});
		db.close();
	}

	/**
	 * @see com.chong.downloadfile.db.ThreadDAO#deleteThread(java.lang.String, int)
	 */
	@Override
	public synchronized void deleteThread(String url)
	{
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL("delete from thread_info where url = ?",
				new Object[]{url});
		db.close();
	}

	/**
	 * @see com.chong.downloadfile.db.ThreadDAO#updateThread(java.lang.String, int, int)
	 */
	@Override
	public synchronized void updateThread(String url, int thread_id, int finished)
	{
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
				new Object[]{finished, url, thread_id});
		db.close();
	}

	/**
	 * @see com.chong.downloadfile.db.ThreadDAO#getThreads(java.lang.String)
	 */
	@Override
	public List<ThreadInfo> getThreads(String url)
	{
		List<ThreadInfo> list = new ArrayList<ThreadInfo>();
		
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
		while (cursor.moveToNext())
		{
			ThreadInfo threadInfo = new ThreadInfo();
			threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
			threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
			threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
			list.add(threadInfo);
		}
		cursor.close();
		db.close();
		return list;
	}

	/**
	 * @see com.chong.downloadfile.db.ThreadDAO#isExists(java.lang.String, int)
	 */
	@Override
	public boolean isExists(String url, int thread_id)
	{
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?", new String[]{url, thread_id+""});
		boolean exists = cursor.moveToNext();
		cursor.close();
		db.close();
		return exists;
	}

	@Override
	public List<FileInfo> getFileInfo() {
		// TODO Auto-generated method stub
		List<FileInfo>list=new ArrayList<FileInfo>();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery("select url,sum(finished) as finished from thread_info group by url", null);
		
		while (cursor.moveToNext()) {
			FileInfo fileInfo=new FileInfo();
			fileInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			fileInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
			list.add(fileInfo);
			
		}
		
		return list;
	}

	@Override
	public boolean isExistFileInfo(String url) {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
		boolean exists = cursor.moveToNext();
		cursor.close();
		db.close();
		return exists;
	}

	@Override
	public void insertFileInfo(FileInfo fileInfo) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL("replace into file_info(fileinfo_id,fileinfo_name,fileinfo_url) values(?,?,?)",
				new Object[]{fileInfo.getId(), fileInfo.getFileName(),
				fileInfo.getUrl()});
		db.close();
	}

	@Override
	public void deleteFielInfo(String fileinfo_id) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.execSQL("delete from file_info where fileinfo_id = ?",
				new Object[]{fileinfo_id});
		db.close();
	}

	@Override
	public List<FileInfo> getFileInfosByFileInfo() {
		List<FileInfo>list=new ArrayList<FileInfo>();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery("select fileinfo_id,fileinfo_name,fileinfo_url from file_info", null);
		
		while (cursor.moveToNext()) {
			FileInfo fileInfo=new FileInfo();
			fileInfo.setId(cursor.getString(cursor.getColumnIndex("fileinfo_id")));
			fileInfo.setFileName(cursor.getString(cursor.getColumnIndex("fileinfo_name")));
			fileInfo.setUrl(cursor.getString(cursor.getColumnIndex("fileinfo_url")));
			list.add(fileInfo);
			
		}
		
		return list;
	}
}
