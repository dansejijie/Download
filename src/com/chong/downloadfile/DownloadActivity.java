package com.chong.downloadfile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.chong.downloadfile.MainActivity.DownloadFileReceiver;
import com.chong.downloadfile.db.ThreadDAOImpl;
import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.param.Param;
import com.chong.downloadfile.receiver.DownloadReceiver;
import com.chong.downloadfile.services.DownloadService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

public class DownloadActivity extends Activity{
	
	CheckBox downloadedCheckBox,downloadingCheckBox;
	ListView downloadedListView,downloadingListView;
	List<String>downloadedData=new ArrayList<String>();
	List<FileInfo>fileInfos=new ArrayList<FileInfo>();
	FileListAdapter fileListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setContentView(R.layout.activity_downloader);
		super.onCreate(savedInstanceState);
		downloadedCheckBox=(CheckBox) findViewById(R.id.cb_downloaded);
		downloadingCheckBox=(CheckBox) findViewById(R.id.cb_downloading);
		downloadedListView=(ListView) findViewById(R.id.lv_downloaded);
		downloadingListView=(ListView) findViewById(R.id.lv_downloading);
		
		initEvent();
		
	}

	private void initEvent() {
		// TODO Auto-generated method stub
		downloadedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					downloadedListView.setVisibility(View.VISIBLE);
				}else {
					downloadedListView.setVisibility(View.GONE);
				}
			}
		});
		downloadingCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					downloadingListView.setVisibility(View.VISIBLE);
				}else {
					downloadingListView.setVisibility(View.GONE);
				}
			}
		});
		
		//装载已下载的程序
		loadDataFromSD();
		
		
		//装载正在下载的数据
		ThreadDAOImpl threadDAOImpl=new ThreadDAOImpl(DownloadActivity.this);
		fileInfos=threadDAOImpl.getFileInfosByFileInfo();
		if (fileInfos==null) {
			fileInfos=new ArrayList<FileInfo>();
		}
		fileListAdapter=new FileListAdapter(this, fileInfos);
		downloadingListView.setAdapter(fileListAdapter);
		
		// 注册广播接收器

		IntentFilter filter = DownloadReceiver.getIntentFilter();
		registerReceiver(mReceiver, filter);
		
		
	}
	private void loadDataFromSD() {
		//装载已下载的程序
				File file=new File(Param.DOWNLOAD_PATH);
				String[]tmpName=file.list();
				for (int i = 0; i < tmpName.length; i++) {
					if (tmpName[i].endsWith(".apk")) {
						downloadedData.add(tmpName[i]);
					}
				}
				
				downloadedListView.setAdapter(new BaseAdapter() {
					
					@Override
					public View getView(final int position, View convertView, ViewGroup parent) {
						// TODO Auto-generated method stub
						convertView=LayoutInflater.from(DownloadActivity.this).inflate(R.layout.item_downloaded, null);
						TextView name=(TextView) convertView.findViewById(R.id.name);
						Button install=(Button) convertView.findViewById(R.id.install);
						name.setText(downloadedData.get(position));
						install.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								File file=new File(Param.DOWNLOAD_PATH,downloadedData.get(position));
								Uri uri=Uri.fromFile(file);
								Intent intent=new Intent(Intent.ACTION_VIEW);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//启动新的activity  
						        //设置Uri和类型  
						        intent.setDataAndType(uri, "application/vnd.android.package-archive");  
						        //执行安装  
						        startActivity(intent); ;
								
							}
						});
						return convertView;
					}
					
					@Override
					public long getItemId(int position) {
						// TODO Auto-generated method stub
						return position;
					}
					
					@Override
					public Object getItem(int position) {
						// TODO Auto-generated method stub
						return downloadedData.get(position);
					}
					
					@Override
					public int getCount() {
						// TODO Auto-generated method stub
						return downloadedData.size();
					}
				});
	}
	
	/**
	 * 更新UI的广播接收器
	 */
	DownloadFileReceiver mReceiver=new DownloadFileReceiver();
	
	class DownloadFileReceiver extends DownloadReceiver{

		@Override
		public void onStart(FileInfo fileInfo) {
			
			Toast.makeText(DownloadActivity.this,"onStart:"+fileInfo.toString(), 0).show();			
		}

		@Override
		public void onUpData(FileInfo fileInfo) {

			fileListAdapter.updateFileInfo(fileInfo);
			//Toast.makeText(MainActivity.this,"onUpdate:"+fileInfo.toString(), 0).show();
		}

		@Override
		public void onPause(final FileInfo fileInfo) {


			//Toast.makeText(DownloadActivity.this,"onPause:"+fileInfo.toString(), 0).show();
			
			final File file=new File(DownloadService.DOWNLOAD_PATH,fileInfo.getFileName());
			
			
		}

		@Override
		public void onCancel(FileInfo fileInfo) {
			// TODO Auto-generated method stub
			fileInfo.setStatu(FileInfo.STATU_CANCEL);
			fileListAdapter.updateFileInfo(fileInfo);

			Toast.makeText(DownloadActivity.this,"onCancel:"+fileInfo.toString(), 0).show();
		}

		@Override
		public void onFinished(FileInfo fileInfo) {
			// TODO Auto-generated method stub
			fileListAdapter.updateFileInfo(fileInfo);

			Toast.makeText(DownloadActivity.this,"onFinished:"+fileInfo.toString(), 0).show();
			ThreadDAOImpl threadDAOImpl=new ThreadDAOImpl(DownloadActivity.this);
			threadDAOImpl.deleteFielInfo(fileInfo.getId());
			//完成后通知已下载的去更新内容
			loadDataFromSD();
		}

		@Override
		public void onExist(final FileInfo fileInfo) {
			// TODO Auto-generated method stub
			final File file=new File(DownloadService.DOWNLOAD_PATH,fileInfo.getFileName());
			
			AlertDialog.Builder builder=new AlertDialog.Builder(DownloadActivity.this);
			builder.setTitle("提醒");
			builder.setMessage("文件已下载，是否重新下载?");
			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					fileInfo.setStatu(FileInfo.STATU_NORMAL);
					fileListAdapter.updateFileInfo(fileInfo);
					DownloadService.sendMeg(DownloadActivity.this, DownloadService.ACTION_CANCEL, fileInfo);
				}
			});
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			});
			AlertDialog dialog=builder.create();
			dialog.show();
		}

		@Override
		public void onFailure(Exception e) {
			// TODO Auto-generated method stub
			Toast.makeText(DownloadActivity.this,"onFailure:"+e.toString(), 0).show();
		}

		@Override
		public void onRuning(FileInfo fileInfo) {

		}
		
		
	}
	
	
}
