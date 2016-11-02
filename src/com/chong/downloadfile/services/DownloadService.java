/*
 * @Title DownloadService.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description：
 * @author Yann
 * @date 2015-8-7 下午10:03:42
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
 * 类注释
 * 
 * @author Yann
 * @date 2015-8-7 下午10:03:42
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
	// 已存在文件
	public static final String ACTION_EXIST = "ACTION_EXIST";
	public static final String ACTION_FAILURE = "ACTION_FAILURE";

	// Intent的传播媒介名称
	public static final String FILE_INFO = "fileInfo";
	public static final String EXCEPTION = "Exception";
	public static final String DATASIZE = "dataSize";

	public static final int MSG_INIT = 0;
	private String TAG = "DownloadService";
	public static Map<String, DownloadTask> mTasks = new LinkedHashMap<String, DownloadTask>();
	ThreadDAOImpl mDao;

	/*
	 * 若是下载命令 则区分情况 
	 * 1、全新下载 :数据库无信息，SD卡无信息(tmp,apk),mTask状态为不存在key
	 * 2、暂停后下载:数据库有信息，SD卡有信息(tmp,!apk),mTask状态为pause且mTask不为空，防止与4、7冲突
	 * 3、取消后下载=数据库无信息，SD卡无信息(tmp,apk),mTask状态为不存在
	 * 4、重新启动后下载：数据库有信息，SD卡有信息（tmp,!apk）,mTask状态为不存在key
	 * 5、缓存数据被删除后继续下载:数据库有信息 ，SD卡无信息(!tmp,!apk),mTaks状态随意
	 * 6、重复下载：数据库随意,SD卡有信息(随意,apk),mTask随意
	 * 7、异常后下载：数据库有信息，SD卡有信息(tmp,!apk),mTask状态为不存在key=重启后下载 
	 * 若是暂停命令，则区分情况
	 * 1、发出暂停命令 若是取消命令，则区分情况 1、当前任务正在下载 -设置线程为pasue,删除数据库信息，删除SD卡信息，删除mTask信息，
	 * 2、当前任务处于暂停-删除数据库信息，删除SD卡信息，删除mTask信息，
	 * 若是取消命令，则区分情况
	 * 1、当在下载暂停后和正在下载是发出取消命令
	 * 2、当刚启动程序，发出取消命令
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
			// 获得Activity传过来的参数

			if (ACTION_START.equals(intent.getAction())) {

				Log.i(TAG, "ServiceStart:" + fileInfo.toString());

				// 1、全新下载 数据库无信息，SD卡无信息 Task无信息
				if (!mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists() && !tmpFile.exists()
						&& !mTasks.containsKey(fileInfo.getId())) {

					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				// 2、暂停后下载:数据库有信息，SD卡有信息(tmp,!apk),mTask状态为pause
				if (mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists()
						&& tmpFile.exists()
						&& (mTasks.get(fileInfo.getId()) != null && mTasks.get(fileInfo.getId()).isPause)) {
					
					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				//3、取消后下在:数据库无信息，SD卡有信息(tmp,!apk),mTask状态为存在
				if (mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists()
						&& tmpFile.exists()
						&& (mTasks.get(fileInfo.getId()) != null && mTasks.get(fileInfo.getId()).isPause)) {
					
					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				// 4、重新启动后下载：数据库有信息，SD卡有信息（tmp,!apk）,mTask状态为不存在key
				if (mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists() && tmpFile.exists()
						&& !mTasks.containsKey(fileInfo.getId())) {

					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				// 5、缓存数据被删除后继续下载
				if (mDao.isExistFileInfo(fileInfo.getUrl())
						&& !apkFile.exists() && !tmpFile.exists()) {

					// 删除数据库信息，重新开始下载
					mDao.deleteThread(fileInfo.getUrl());
					new InitThread(fileInfo).start();
					return super.onStartCommand(intent, flags, startId);
				}
				// 6、重复下载：数据库随意,SD卡有信息(随意,apk),mTask随意
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
			// 1、发出暂停命令
			else if (ACTION_PAUSE.equals(intent.getAction())) {
				Log.i(TAG, "ServicePause:" + fileInfo.toString());

				// 从集合中取出下载任务
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
					// 1、当前任务正在下载
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

					// 发送广播知道UI下载任务结束
					fileInfo.setStatu(FileInfo.STATU_CANCEL);
					Intent intent2 = new Intent(DownloadService.ACTION_CANCEL);
					intent2.putExtra(DownloadService.FILE_INFO, fileInfo);
					sendBroadcast(intent2);
				}else {
					//2、当刚启动程序，发出取消命令
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
				// Log.i(TAG, "Init:" + fileInfo+"--获取文件大小");
				// 启动下载任务
				DownloadTask task = new DownloadTask(DownloadService.this,
						fileInfo, Param.THREAD_COUNT);
				task.downLoad();
				// 把下载任务添加到集合中
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
				// 连接网络文件
				URL url = new URL(mFileInfo.getUrl());
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				int length = -1;

				if (connection.getResponseCode() == HttpStatus.SC_OK) {
					// 获得文件的长度
					length = connection.getContentLength();
				}

				if (length <= 0) {
					return;
				}

				File dir = new File(DOWNLOAD_PATH);
				if (!dir.exists()) {
					dir.mkdir();
				}

				// 在本地创建文件
				File file = new File(dir, mFileInfo.getFileName() + ".tmp");
				raf = new RandomAccessFile(file, "rwd");
				// 设置文件长度
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
