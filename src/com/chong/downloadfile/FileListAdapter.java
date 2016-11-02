/*
 * @Title FileListAdapter.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description��
 * @author Yann
 * @date 2015-8-9 ����11:37:18
 * @version 1.0
 */
package com.chong.downloadfile;

import java.io.File;
import java.util.List;

import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.param.Param;
import com.chong.downloadfile.services.DownloadService;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


/** 
 * ��ע��
 * @author Yann
 * @date 2015-8-9 ����11:37:18
 */
public class FileListAdapter extends BaseAdapter
{
	private Context mContext;
	private List<FileInfo> mList;
	
	public FileListAdapter(Context context, List<FileInfo> fileInfos)
	{
		this.mContext = context;
		this.mList = fileInfos;
	}
	
	/**
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount()
	{
		return mList.size();
	}

	/**
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		final FileInfo fileInfo = mList.get(position);
		
		if (convertView != null)
		{
			viewHolder = (ViewHolder) convertView.getTag();
			
			if (!viewHolder.mFileName.getTag().equals(fileInfo.getId()))
			{
				convertView = null;
			}
		}
		
		if (null == convertView)
		{
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.item, null);
			
			viewHolder = new ViewHolder(
					(TextView) convertView.findViewById(R.id.tv_fileName),
					(ProgressBar) convertView.findViewById(R.id.pb_progress),
					(Button) convertView.findViewById(R.id.btn_start),
					(Button) convertView.findViewById(R.id.btn_stop),
					(Button) convertView.findViewById(R.id.btn_cancel)
					);
			convertView.setTag(viewHolder);
		}
		

		viewHolder.mStartBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (fileInfo.getStatu().equals(FileInfo.STATU_NORMAL)||fileInfo.getStatu().equals(FileInfo.STATU_PAUSE)) {
					
					fileInfo.setStatu(FileInfo.STATU_DOWNLOADING);

					DownloadService.sendMeg(mContext, DownloadService.ACTION_START, fileInfo);
					
				}else if (fileInfo.getStatu().equals(FileInfo.STATU_DOWNLOADING)) {
					fileInfo.setStatu(FileInfo.STATU_PAUSE);
					((Button)v).setText("��������");

					DownloadService.sendMeg(mContext, DownloadService.ACTION_PAUSE, fileInfo);
				}else if (fileInfo.getStatu().equals(FileInfo.STATU_FINISH)) {
					File apkFile=new File(Param.DOWNLOAD_PATH,fileInfo.getFileName());
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
		});
		viewHolder.mFileName.setTag(fileInfo.getId());
		
		viewHolder.mCancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DownloadService.sendMeg(mContext, DownloadService.ACTION_CANCEL, fileInfo);
				fileInfo.setStatu(FileInfo.STATU_CANCEL);
			}
		});
		viewHolder.mStopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DownloadService.sendMeg(mContext, DownloadService.ACTION_PAUSE, fileInfo);
				fileInfo.setStatu(FileInfo.STATU_PAUSE);
			}
		});
		
		viewHolder.mFileName.setText(fileInfo.getFileName());
		viewHolder.mProgressBar.setMax(100);
			
		viewHolder.mProgressBar.setProgress(fileInfo.getFinished());
		
		return convertView;
	}

	/** 
	 * �����б����еĽ�����
	 * @param id
	 * @param progress
	 * @return void
	 * @author Yann
	 * @date 2015-8-9 ����1:34:14
	 */ 
	public void updateProgress(int id, int progress)
	{
		FileInfo fileInfo = mList.get(id);
		fileInfo.setFinished(progress);
		notifyDataSetChanged();
	}
	
	private static class ViewHolder
	{
		TextView mFileName;
		ProgressBar mProgressBar;
		Button mStartBtn;
		Button mStopBtn;
		Button mCancelBtn;
		/** 
		 *@param mFileName
		 *@param mProgressBar
		 *@param mStartBtn
		 *@param mStopBtn
		 */
		public ViewHolder(TextView mFileName, ProgressBar mProgressBar,
				Button mStartBtn, Button mStopBtn,Button mCancelBtn)
		{
			this.mFileName = mFileName;
			this.mProgressBar = mProgressBar;
			this.mStartBtn = mStartBtn;
			this.mStopBtn = mStopBtn;
			this.mCancelBtn=mCancelBtn;
		}
	}
	
	/**
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position)
	{
		return mList.get(position);
	}

	/**
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	public void updateFileInfo(FileInfo fileInfo){
		
		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i).getId().equals(fileInfo.getId())) {
				
				if (fileInfo.getStatu().equals(FileInfo.STATU_FINISH)||fileInfo.getStatu().equals(FileInfo.STATU_CANCEL)) {
					mList.remove(i);
				}
				mList.get(i).setFinished(fileInfo.getFinished());
				
				mList.get(i).setStatu(fileInfo.getStatu());
				notifyDataSetChanged();
				break;
			}
		}
		
	}
}
