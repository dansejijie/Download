/*
 * @Title FileListAdapter.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description：
 * @author Yann
 * @date 2015-8-9 上午11:37:18
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
 * 类注释
 * @author Yann
 * @date 2015-8-9 上午11:37:18
 */
public class FileListAdapter2 extends BaseAdapter
{
	private Context mContext;
	private List<FileInfo> mList;
	
	public FileListAdapter2(Context context, List<FileInfo> fileInfos)
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
			
			if (!viewHolder.mStartBtn.getTag().equals(Integer.valueOf(fileInfo.getId())))
			{
				convertView = null;
			}
		}
		
		if (null == convertView)
		{
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.item2, null);
			
			viewHolder = new ViewHolder(
					
					(Button) convertView.findViewById(R.id.downloader_button),
					(TextView)convertView.findViewById(R.id.name)
					);
			convertView.setTag(viewHolder);
		}
		
		viewHolder.name.setText(fileInfo.getFileName().subSequence(0, 8));
		
		
		if (fileInfo.getStatu().equals(FileInfo.STATU_NORMAL)) {
			viewHolder.mStartBtn.setText("下载");
		}else if (fileInfo.getStatu().equals(FileInfo.STATU_PAUSE)) {
			viewHolder.mStartBtn.setText("继续下载");
		}else if (fileInfo.getStatu().equals(FileInfo.STATU_FINISH)) {
			viewHolder.mStartBtn.setText("安装");
		}else if (fileInfo.getStatu().equals(FileInfo.STATU_DOWNLOADING)) {
			viewHolder.mStartBtn.setText(fileInfo.getFinished()+"%/暂停");
		}
		
		viewHolder.mStartBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (fileInfo.getStatu().equals(FileInfo.STATU_NORMAL)||fileInfo.getStatu().equals(FileInfo.STATU_PAUSE)) {
					
					fileInfo.setStatu(FileInfo.STATU_DOWNLOADING);
					((Button)v).setText("0.0%/暂停");

					DownloadService.sendMeg(mContext, DownloadService.ACTION_START, fileInfo);
					
				}else if (fileInfo.getStatu().equals(FileInfo.STATU_DOWNLOADING)) {
					fileInfo.setStatu(FileInfo.STATU_PAUSE);
					((Button)v).setText("继续下载");

					DownloadService.sendMeg(mContext, DownloadService.ACTION_PAUSE, fileInfo);
				}else if (fileInfo.getStatu().equals(FileInfo.STATU_FINISH)) {
					File apkFile=new File(Param.DOWNLOAD_PATH,fileInfo.getFileName());
			        //创建URI  
			        Uri uri=Uri.fromFile(apkFile);  
			        //创建Intent意图  
			        Intent intent2=new Intent(Intent.ACTION_VIEW);  
			        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//启动新的activity  
			        //设置Uri和类型  
			        intent2.setDataAndType(uri, "application/vnd.android.package-archive");  
			        //执行安装  
			        mContext.startActivity(intent2); 
				}
				
			}
		});
		viewHolder.mStartBtn.setTag(Integer.valueOf(fileInfo.getId()));
		return convertView;
	}

	/** 
	 * 更新列表项中的进度条
	 * @param id
	 * @param progress
	 * @return void
	 * @author Yann
	 * @date 2015-8-9 下午1:34:14
	 */ 
	public void updateProgress(int id, int progress)
	{
		FileInfo fileInfo = mList.get(id);
		fileInfo.setFinished(progress);
		notifyDataSetChanged();
	}
	
	public void updateFileInfo(FileInfo fileInfo){
		
		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i).getId().equals(fileInfo.getId())) {
				mList.get(i).setFinished(fileInfo.getFinished());
				mList.get(i).setStatu(fileInfo.getStatu());
				notifyDataSetChanged();
				break;
			}
		}
		
	}
	
	private static class ViewHolder
	{

		Button mStartBtn;
		TextView name;

		/** 
		 *@param mFileName
		 *@param mProgressBar
		 *@param mStartBtn
		 *@param mStopBtn
		 */
		public ViewHolder(
				Button mStartBtn,TextView name)
		{

			this.mStartBtn = mStartBtn;
			this.name=name;

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
}
