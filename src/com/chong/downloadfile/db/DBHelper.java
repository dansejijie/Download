/*
 * @Title DBHelper.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description：
 * @author Yann
 * @date 2015-8-8 上午10:48:31
 * @version 1.0
 */
package com.chong.downloadfile.db;

import android.R.integer;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/** 
 * 数据库帮助类
 * @author Yann
 * @date 2015-8-8 上午10:48:31
 */
public class DBHelper extends SQLiteOpenHelper
{
	private static final String DB_NAME = "download.db";
	private static final int VERSION =3;
	private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement," +
			"thread_id integer, url text, start integer, end integer, finished integer)";
	private static final String SQL_CREATE2 = "create table file_info(" +
			"fileinfo_id text, fileinfo_name text,fileinfo_url text)";
	private static final String SQL_DROP = "drop table if exists thread_info";
	private static final String SQL_DROP2 = "drop table if exists file_info";
	private static DBHelper sDbHelper = null;
	
	/** 
	 *@param context
	 *@param name
	 *@param factory
	 *@param version
	 */
	private DBHelper(Context context)
	{
		super(context, DB_NAME, null, VERSION);
	}

	public static DBHelper getInstance(Context context)
	{
		if (null == sDbHelper)
		{
			sDbHelper = new DBHelper(context);
		}
		
		return sDbHelper;
	}
	
	/**
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(SQL_CREATE);
		db.execSQL(SQL_CREATE2);
	}

	/**
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL(SQL_DROP);
		db.execSQL(SQL_DROP2);
		db.execSQL(SQL_CREATE);
		db.execSQL(SQL_CREATE2);
	}

}
