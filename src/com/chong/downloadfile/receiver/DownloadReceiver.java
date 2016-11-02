package com.chong.downloadfile.receiver;

import java.io.File;

import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.services.DownloadService;

import android.app.PendingIntent.OnFinished;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public abstract class DownloadReceiver extends BroadcastReceiver{

	
	private static IntentFilter intentFilter=new IntentFilter();
	public static IntentFilter getIntentFilter(){
		
		intentFilter.addAction(DownloadService.ACTION_START);

		intentFilter.addAction(DownloadService.ACTION_UPDATE);
		
		intentFilter.addAction(DownloadService.ACTION_PAUSE);
		
		intentFilter.addAction(DownloadService.ACTION_CANCEL);

		intentFilter.addAction(DownloadService.ACTION_FINISHED);
		
		intentFilter.addAction(DownloadService.ACTION_FAILURE);
		
		intentFilter.addAction(DownloadService.ACTION_EXIST);
		
		intentFilter.addAction(DownloadService.ACTION_RUNING);
		
		return intentFilter;
	}
	public DownloadReceiver() {
		// TODO Auto-generated constructor stub
		
	}
	
	
	private static String TAG="DownloadReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		FileInfo fileInfo = (FileInfo) intent.getSerializableExtra(DownloadService.FILE_INFO);
		if (DownloadService.ACTION_START.equals(intent.getAction())) {
			
			onStart(fileInfo);	
			Log.i(TAG,"onStart:"+fileInfo.toString());
		}
		if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
			
			onUpData(fileInfo);
			Log.i(TAG,"onUpData:"+fileInfo.toString());
		}
		if (DownloadService.ACTION_PAUSE.equals(intent.getAction())) {
			
			onPause(fileInfo);
			Log.i(TAG,"onPause:"+fileInfo.toString());
		}
		if (DownloadService.ACTION_CANCEL.equals(intent.getAction())) {
			
			onCancel(fileInfo);
			Log.i(TAG,"onCancel:"+fileInfo.toString());
		}
		if (DownloadService.ACTION_FINISHED.equals(intent.getAction())) {
			
			onFinished(fileInfo);
			Log.i(TAG,"onFinished:"+fileInfo.toString());
		}
		if (DownloadService.ACTION_EXIST.equals(intent.getAction())) {
			
			onExist(fileInfo);
			Log.i(TAG,"onExist:"+fileInfo.toString());
		}
		if (DownloadService.ACTION_FAILURE.equals(intent.getAction())) {
			
			Exception exception=(Exception) intent.getSerializableExtra(DownloadService.EXCEPTION);
			onFailure(exception);
			Log.i(TAG,"onFailure:"+exception.toString());
		}
		if (DownloadService.ACTION_RUNING.equals(intent.getAction())) {
			onRuning(fileInfo);
			Log.i(TAG,"onRuning:"+fileInfo.toString());
		}
	}
	
	public abstract void onStart(FileInfo fileInfo);
	public abstract void onUpData(FileInfo fileInfo);
	public abstract void onPause(FileInfo fileInfo);
	public abstract void onCancel(FileInfo fileInfo);
	public abstract void onFinished(FileInfo fileInfo);
	public abstract void onExist(FileInfo fileInfo);
	public abstract void onRuning(FileInfo fileInfo);
	public abstract void onFailure(Exception e);

}
