/*
 * @Title DownloadService.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description��
 * @author Yann
 * @date 2015-8-7 ����10:03:42
 * @version 1.0
 */
package com.chong.downloadfile.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import com.chong.downloadfile.db.ThreadDAOImpl;
import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.param.Param;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView.FindListener;
import android.widget.Toast;

/**
 * ��ע��
 * 
 * @author Yann
 * @date 2015-8-7 ����10:03:42
 */
public class DownloadService extends Service {
	public static final String DOWNLOAD_PATH = Param.DOWNLOAD_PATH;

	public static final String ACTION_NORMAL = "ACTION_NORMAL";
	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final String ACTION_PAUSE = "ACTION_PAUSE";
	public static final String ACTION_CANCEL = "ACTION_CANCAL";
	public static final String ACTION_RUNING = "ACTION_RUNING";
	public static final String ACTION_FINISHED = "ACTION_FINISHED";
	// �Ѵ����ļ�
	public static final String ACTION_EXIST = "ACTION_EXIST";
	public static final String ACTION_FAILURE = "ACTION_FAILURE";

	// Intent�Ĵ���ý������
	public static final String FILE_INFO = "fileInfo";
	public static final String EXCEPTION = "Exception";
	public static final String DATASIZE = "dataSize";

	public static final int MSG_INIT = 0;
	private String TAG = "DownloadService";
	public static Map<String, DownloadTask> mTasks = new LinkedHashMap<String, DownloadTask>();
	ThreadDAOImpl mDao;

	/*
	 * ������������ ��������� 
	 * 1��ȫ������ :���ݿ�����Ϣ��SD������Ϣ(tmp,apk),mTask״̬Ϊ������key
	 * 2����ͣ������:���ݿ�����Ϣ��SD������Ϣ(tmp,!apk),mTask״̬Ϊpause��mTask��Ϊ�գ���ֹ��4��7��ͻ
	 * 3��ȡ��������=���ݿ�����Ϣ��SD������Ϣ(tmp,apk),mTask״̬Ϊ������
	 * 4���������������أ����ݿ�����Ϣ��SD������Ϣ��tmp,!apk��,mTask״̬Ϊ������key
	 * 5���������ݱ�ɾ�����������:���ݿ�����Ϣ ��SD������Ϣ(!tmp,!apk),mTaks״̬����
	 * 6���ظ����أ����ݿ�����,SD������Ϣ(����,apk),mTask����
	 * 7���쳣�����أ����ݿ�����Ϣ��SD������Ϣ(tmp,!apk),mTask״̬Ϊ������key=���������� 
	 * ������ͣ������������
	 * 1��������ͣ���� ����ȡ�������������� 1����ǰ������������ -�����߳�Ϊpasue,ɾ�����ݿ���Ϣ��ɾ��SD����Ϣ��ɾ��mTask��Ϣ��
	 * 2����ǰ��������ͣ-ɾ�����ݿ���Ϣ��ɾ��SD����Ϣ��ɾ��mTask��Ϣ��
	 * ����ȡ��������������
	 * 1������������ͣ������������Ƿ���ȡ������
	 * 2�������������򣬷���ȡ������
	 */
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(FILE_INFO);
		File apkFile = new File(DOWNLOAD_PATH, fileInfo.getFileName());
		File tmpFile = new File(DOWNLOAD_PATH, fileInfo.getFileName() + ".tmp");

		if (mDao == null) {
			mDao = new ThreadDAOImpl(this);
		}

		try {
			// ���Activity�������Ĳ���

			if (ACTION_START.equals(intent.getAction())) {

				Log.i(TAG, "ServiceStart:" + fileInfo.toString());

				// 1��ȫ������ ���ݿ�����Ϣ��SD������Ϣ Task����Ϣ
				if (!mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists() && !tmpFile.exists()
						&& !mTasks.containsKey(fileInfo.getId())) {

					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				// 2����ͣ������:���ݿ�����Ϣ��SD������Ϣ(tmp,!apk),mTask״̬Ϊpause
				if (mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists()
						&& tmpFile.exists()
						&& (mTasks.get(fileInfo.getId()) != null && mTasks.get(fileInfo.getId()).isPause)) {
					
					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				//3��ȡ��������:���ݿ�����Ϣ��SD������Ϣ(tmp,!apk),mTask״̬Ϊ����
				if (mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists()
						&& tmpFile.exists()
						&& (mTasks.get(fileInfo.getId()) != null && mTasks.get(fileInfo.getId()).isPause)) {
					
					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				// 4���������������أ����ݿ�����Ϣ��SD������Ϣ��tmp,!apk��,mTask״̬Ϊ������key
				if (mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists() && tmpFile.exists()
						&& !mTasks.containsKey(fileInfo.getId())) {

					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				// 5���������ݱ�ɾ�����������
				if (mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists() && !tmpFile.exists()) {

					// ɾ�����ݿ���Ϣ�����¿�ʼ����
					mDao.deleteThread(fileInfo.getUrl());
					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				// 6���ظ����أ����ݿ�����,SD������Ϣ(����,apk),mTask����
				if (apkFile.exists()) {

					Intent intent2 = new Intent(DownloadService.ACTION_EXIST);
					intent2.putExtra(FILE_INFO, fileInfo);
					sendBroadcast(intent2);
					if (tmpFile.exists()) {
						tmpFile.delete();
					}
					return super.onStartCommand(intent, flags, startId);
				}

			}
			// 1��������ͣ����
			else if (ACTION_PAUSE.equals(intent.getAction())) {
				Log.i(TAG, "ServicePause:" + fileInfo.toString());

				// �Ӽ�����ȡ����������
				DownloadTask task = mTasks.get(fileInfo.getId());
				task.isPause = true;
				
				fileInfo.setStatu(FileInfo.STATU_PAUSE);
				Intent intent2 = new Intent(DownloadService.ACTION_PAUSE);
				intent2.putExtra(DownloadService.FILE_INFO, fileInfo);
				sendBroadcast(intent2);

				return super.onStartCommand(intent, flags, startId);

			} else if (ACTION_CANCEL.endsWith(intent.getAction())) {

				Log.i(TAG, "ServiceCancel:" + fileInfo.toString());
				DownloadTask task = mTasks.get(fileInfo.getId());

				if (task != null) {
					// 1����ǰ������������
					if (!task.isPause) {
						task.isCancel=true;
						task.isPause = true;
						mDao.deleteThread(fileInfo.getUrl());
						mTasks.remove(fileInfo.getId());
						try {
							if (tmpFile.exists()) {
								tmpFile.delete();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else {
						mDao.deleteThread(fileInfo.getUrl());
						mTasks.remove(fileInfo.getId());
						try {
							if (tmpFile.exists()) {
								tmpFile.delete();
							}
							if (apkFile.exists()) {
								apkFile.delete();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					// ���͹㲥֪��UI�����������
					fileInfo.setStatu(FileInfo.STATU_CANCEL);
					Intent intent2 = new Intent(DownloadService.ACTION_CANCEL);
					intent2.putExtra(DownloadService.FILE_INFO, fileInfo);
					sendBroadcast(intent2);
				}else {
					//2�������������򣬷���ȡ������
					mDao.deleteThread(fileInfo.getUrl());
					mTasks.remove(fileInfo.getId());
					try {
						if (tmpFile.exists()) {
							tmpFile.delete();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

			return super.onStartCommand(intent, flags, startId);
		} catch (Exception e) {
			// TODO: handle exception
			try {
				mDao.deleteThread(fileInfo.getUrl());
				if (apkFile.exists()) {
					apkFile.delete();
				}
				if (tmpFile.exists()) {
					tmpFile.delete();
				}
				mTasks.remove(fileInfo.getId());
			} catch (Exception e2) {
				// TODO: handle exception
			}

			Intent intent2 = new Intent(DownloadService.ACTION_FAILURE);
			intent2.putExtra(EXCEPTION, e);
			sendBroadcast(intent2);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileInfo = (FileInfo) msg.obj;
				// Log.i(TAG, "Init:" + fileInfo+"--��ȡ�ļ���С");
				// ������������
				DownloadTask task = new DownloadTask(DownloadService.this,
						fileInfo, Param.THREAD_COUNT);
				task.downLoad();
				// ������������ӵ�������
				mTasks.put(fileInfo.getId(), task);
				break;

			default:
				break;
			}
		};
	};

	private class InitThread extends Thread {
		private FileInfo mFileInfo = null;

		public InitThread(FileInfo mFileInfo) {
			this.mFileInfo = mFileInfo;
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			HttpURLConnection connection = null;
			RandomAccessFile raf = null;

			try {
				// ���������ļ�
				URL url = new URL(mFileInfo.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				int length = -1;

				if (connection.getResponseCode() == HttpStatus.SC_OK) {
					// ����ļ��ĳ���
					length = connection.getContentLength();
				}

				if (length <= 0) {
					return;
				}

				File dir = new File(DOWNLOAD_PATH);
				if (!dir.exists()) {
					dir.mkdir();
				}

				// �ڱ��ش����ļ�
				File file = new File(dir, mFileInfo.getFileName() + ".tmp");
				raf = new RandomAccessFile(file, "rwd");
				// �����ļ�����
				raf.setLength(length);
				mFileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
				Intent intent = new Intent(DownloadService.ACTION_FAILURE);
				intent.putExtra(EXCEPTION, e);
				sendBroadcast(intent);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
				if (raf != null) {
					try {
						raf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		for (String key : mTasks.keySet()) {
			DownloadTask task = mTasks.get(key);
			if (task != null) {
				task.isPause = true;

			}
		}

		super.onDestroy();
	}

	/**
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static void sendMeg(Context context, String action, FileInfo fileInfo) {

		Intent intent = new Intent(context, DownloadService.class);
		intent.setAction(action);
		intent.putExtra(FILE_INFO, fileInfo);
		context.startService(intent);
	}

}
