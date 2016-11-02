/*
 * @Title DownloadTask.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description��
 * @author Yann
 * @date 2015-8-7 ����10:11:05
 * @version 1.0
 */
package com.chong.downloadfile.services;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import com.chong.downloadfile.db.ThreadDAO;
import com.chong.downloadfile.db.ThreadDAOImpl;
import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.entities.ThreadInfo;
import com.chong.downloadfile.param.Param;

/** 
 * ����������
 * @author Yann
 * @date 2015-8-7 ����10:11:05
 */
public class DownloadTask
{
	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mFinised = 0;
	public boolean isPause = false;
	public boolean isCancel=false;
	//��ֹ����̷߳���ȡ���㲥��ֻ����һ�����;ͺ���
	private boolean isCancelOk=false;
	
	//���ļ������Ѵ������غõ��ļ����Ƿ񸲸Ǽ�������
	private boolean isContinueWhenExist=true;
	
	public boolean isFinished=false;
	
	private int mThreadCount = 1;  // �߳�����
	private List<DownloadThread> mDownloadThreadList = null; // �̼߳���
	
	/** 
	 *@param mContext
	 *@param mFileInfo
	 */
	public DownloadTask(Context mContext, FileInfo mFileInfo, int count)
	{
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		this.mThreadCount = count;
		mDao = new ThreadDAOImpl(mContext);
	}
	
	public void downLoad()
	{
		// ��ȡ���ݿ���߳���Ϣ
		List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		
		if (0 == threads.size())
		{
			if (isContinueWhenExist) {
				// ����ÿ���߳����س���
				int len = mFileInfo.getLength() / mThreadCount;
				for (int i = 0; i < mThreadCount; i++)
				{
					// ��ʼ���߳���Ϣ����
					threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),
							len * i, (i + 1) * len - 1, 0);
					
					if (mThreadCount - 1 == i)  // �������һ���߳����س��Ȳ�������������
					{
						threadInfo.setEnd(mFileInfo.getLength());
					}
					
					// ��ӵ��̼߳�����
					threads.add(threadInfo);
					mDao.insertThread(threadInfo);
				}
			}			
		}

		mDownloadThreadList = new ArrayList<DownloadTask.DownloadThread>();
		// ��������߳̽�������
		for (ThreadInfo info : threads)
		{
			DownloadThread thread = new DownloadThread(info);
			thread.start();
			// ��ӵ��̼߳�����
			mDownloadThreadList.add(thread);
		}
		
		Intent intent=new Intent(DownloadService.ACTION_START);
		intent.putExtra(DownloadService.FILE_INFO, mFileInfo);
		mContext.sendBroadcast(intent);
	}
	
	/** 
	 * �����߳�
	 * @author Yann
	 * @date 2015-8-8 ����11:18:55
	 */ 
	private class DownloadThread extends Thread
	{
		private ThreadInfo mThreadInfo = null;
		public boolean isFinished = false;  // �߳��Ƿ�ִ�����

		/** 
		 *@param mInfo
		 */
		public DownloadThread(ThreadInfo mInfo)
		{
			this.mThreadInfo = mInfo;
		}
		
		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			HttpURLConnection connection = null;
			RandomAccessFile raf = null;
			InputStream inputStream = null;
			
			try
			{
				URL url = new URL(mThreadInfo.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				// ��������λ��
				int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
				connection.setRequestProperty("Range",
						"bytes=" + start + "-" + mThreadInfo.getEnd());
				// �����ļ�д��λ��
				File file = new File(DownloadService.DOWNLOAD_PATH,
						mFileInfo.getFileName()+".tmp");
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);
				Intent intent = new Intent();
				intent.setAction(DownloadService.ACTION_UPDATE);
				mFinised += mThreadInfo.getFinished();
				Log.i("mFinised", mThreadInfo.getId() + "finished = " + mThreadInfo.getFinished());
				// ��ʼ����
				if (connection.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT)
				{
					// ��ȡ����
					inputStream = connection.getInputStream();
					byte buf[] = new byte[1024 << 2];
					int len = -1;
					long time = System.currentTimeMillis();
					while ((len = inputStream.read(buf)) != -1)
					{
						// д���ļ�
						raf.write(buf, 0, len);
						// �ۼ������ļ���ɽ���
						mFinised += len;
						
						//��ʾ�����ص�״̬
						/*Intent intent2 = new Intent(DownloadService.ACTION_RUNING);
						intent2.putExtra(DownloadService.FILE_INFO, mFileInfo);
						mContext.sendBroadcast(intent2);*/
						
						// �ۼ�ÿ���߳���ɵĽ���
						mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
						if (System.currentTimeMillis() - time > 1000)
						{
							time = System.currentTimeMillis();
							int f = mFinised * 100 / mFileInfo.getLength();
							if (f > mFileInfo.getFinished())
							{
								mFileInfo.setFinished(f);
								mFileInfo.setStatu(FileInfo.STATU_DOWNLOADING);
								intent.putExtra(DownloadService.FILE_INFO, mFileInfo);
								mContext.sendBroadcast(intent);
							}
						}
						
						// ��������ͣʱ���������ؽ���
						if (isPause)
						{
							if (!isCancel) {
								
								mDao.updateThread(mThreadInfo.getUrl(),	
										mThreadInfo.getId(), 
										mThreadInfo.getFinished());
								
								Log.i("mThreadInfo", mThreadInfo.getId() + "Pause = " + mThreadInfo.getFinished());
							}
							

							return;
						}
						if (isCancel) {
							
							isPause=true;
							// ɾ�����ؼ�¼
							if (!isCancelOk) {
								
								Log.i("mThreadInfo", mThreadInfo.getId() + "--cancel");
								
								//ɾ�����ݿ���Ϣ
								mDao.deleteThread(mFileInfo.getUrl());
								
								//ɾ��SD�����ļ�
								File file2 = new File(DownloadService.DOWNLOAD_PATH,
										mFileInfo.getFileName()+".tmp");
								if (file2.exists()) {
									file2.delete();
								}
								
								DownloadService.mTasks.remove(mFileInfo.getId());
								
								// ���͹㲥֪��UI�����������
								/*mFileInfo.setStatu(FileInfo.STATU_CANCEL);
								Intent intent3 = new Intent(DownloadService.ACTION_CANCEL);
								intent3.putExtra(DownloadService.FILE_INFO, mFileInfo);
								mContext.sendBroadcast(intent3);*/
								

								//���ñ�־  �������в�������ɣ������߳������������
								isCancelOk=true;
							}
							return;
						}
					}
					
					// ��ʶ�߳�ִ�����
					isFinished = true;
					checkAllThreadFinished();
				}
			}
			catch (Exception e)
			{
				isCancel=true;
				e.printStackTrace();
				Intent intent2 = new Intent(DownloadService.ACTION_FAILURE);
				intent2.putExtra(DownloadService.EXCEPTION, e);
				mContext.sendBroadcast(intent2);
			}
			finally
			{
				try
				{
					if (connection != null)
					{
						connection.disconnect();
					}
					if (raf != null)
					{
						raf.close();
					}
					if (inputStream != null)
					{
						inputStream.close();
					}
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
		}
	}
	
	/** 
	 * �ж����е��߳��Ƿ�ִ�����
	 * @return void
	 * @author Yann
	 * @date 2015-8-9 ����1:19:41
	 */ 
	private synchronized void checkAllThreadFinished()
	{
		boolean allFinished = true;
		
		// �����̼߳��ϣ��ж��߳��Ƿ�ִ�����
		for (DownloadThread thread : mDownloadThreadList)
		{
			if (!thread.isFinished)
			{
				allFinished = false;
				break;
			}
		}
		
		if (allFinished)
		{
			// ɾ�����ؼ�¼
			mDao.deleteThread(mFileInfo.getUrl());
			
			//�����ļ���
			File tmpFile=new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFileName()+".tmp");
			File apkFile=new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFileName());
			tmpFile.renameTo(apkFile);
			
			//ɾ��Task
			DownloadService.mTasks.remove(mFileInfo.getId());
			
			// ���͹㲥֪��UI�����������
			Intent intent = new Intent(DownloadService.ACTION_FINISHED);
			mFileInfo.setStatu(FileInfo.STATU_FINISH);
			intent.putExtra(DownloadService.FILE_INFO, mFileInfo);
			mContext.sendBroadcast(intent);
			//���õ�ǰ�߳�Ϊ�����غ�
			
			isFinished=true;
			isPause=true;
			if (Param.IS_INSTALL) {
		        //����URI  
		        Uri uri=Uri.fromFile(apkFile);  
		        //����Intent��ͼ  
		        Intent intent2=new Intent(Intent.ACTION_VIEW);  
		        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//�����µ�activity  
		        //����Uri������  
		        intent2.setDataAndType(uri, "application/vnd.android.package-archive");  
		        //ִ�а�װ  
		        mContext.startActivity(intent2); 
			}
		}
	}
}
