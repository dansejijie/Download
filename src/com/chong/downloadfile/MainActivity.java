package com.chong.downloadfile;

import java.io.File;
import java.util.ArrayList;

import java.util.List;

import com.chong.downloadfile.db.ThreadDAOImpl;
import com.chong.downloadfile.entities.FileInfo;
import com.chong.downloadfile.param.Param;
import com.chong.downloadfile.receiver.DownloadReceiver;
import com.chong.downloadfile.services.DownloadService;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.content.Context;

import android.content.Intent;

import android.content.IntentFilter;

import android.os.Bundle;

import android.os.Handler;

import android.util.Log;

import android.view.KeyEvent;

import android.view.View;

import android.view.View.OnClickListener;

import android.widget.Button;

import android.widget.ListView;

import android.widget.ProgressBar;

import android.widget.TextView;

import android.widget.Toast;


public class MainActivity extends Activity {
	
	Button downloadActivityButton;

	public static MainActivity mMainActivity = null;
	
	TextView textView;

	private ListView mListView = null;

	private List<FileInfo> mFileInfoList = null;

	
	private FileListAdapter2 mAdapter = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		downloadActivityButton=(Button) findViewById(R.id.button1);
		downloadActivityButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(MainActivity.this,DownloadActivity.class);
				startActivity(intent);
			}
		});
		textView=(TextView) findViewById(R.id.textView1);
		ThreadDAOImpl mDaoImpl=new ThreadDAOImpl(this);
		
		try {
			List<FileInfo>list=mDaoImpl.getFileInfo();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		mListView = (ListView) findViewById(R.id.lv_downLoad);

		mFileInfoList = new ArrayList<FileInfo>();

		// 初始化文件信息对象
		mFileInfoList.add(new FileInfo("0", "http://gdown.baidu.com/data/wisegame/91319a5a1dfae322/baidu_16785426.apk","baidu_16785426.apk", 
				 0, 0));
		
		mFileInfoList.add(new FileInfo("1", "http://183.134.9.35/apk.r1.market.hiapk.com/data/upload/apkres/2016/6_8/14/com.ladatiao_022403.apk?wsiphost=local",
				"com.ladatiao_022403.apk", 
				 0, 0));
		
		mFileInfoList.add(new FileInfo("2", "http://apk.r1.market.hiapk.com/data/upload/apkres/2016/8_3/14/com.malangstudio.alarmmon_025305.apk","com.malangstudio.alarmmon_025305.apk", 
				0, 0));
		
		mFileInfoList.add(new FileInfo("3", "http://183.134.9.35/apk.r1.market.hiapk.com/data/upload/apkres/2016/5_12/15/com.baozou.baozou.android_032607.apk?wsiphost=local","com.baozou.baozou.android_032607.apk", 
				0, 0));
		mFileInfoList.add(new FileInfo("4", "http://marketdown1.gamedog.cn/big/game/yizhi/438822/kaixinxiaoxiaole_an.apk","kaixinxiaoxiaole_an.apk", 
				0, 0));
		mFileInfoList.add(new FileInfo("5", "http://marketdown1.gamedog.cn/big/game/dongzuo/474795/yibuliangbu_an.apk","yibuliangbu_an.apk", 
				0, 0));
		mFileInfoList.add(new FileInfo("6", "http://marketdown1.gamedog.cn/among/game/jingsu/1878355/4Dcheshenkuangbiao_yxdog.apk","4Dcheshenkuangbiao_yxdog.apk", 
				0, 0));
		mFileInfoList.add(new FileInfo("7", "http://marketdown1.gamedog.cn/among/game/yizhi/526805/jiejiqianjipaobuyu_yxdog.apk","jiejiqianjipaobuyu_yxdog.apk", 
				0, 0));
		mFileInfoList.add(new FileInfo("8", "http://marketdown1.gamedog.cn/big/game/dongzuo/398629/xiongchumozhixiongdakuaipao_an.apk","xiongchumozhixiongdakuaipao_an.apk", 
				0, 0));
		mFileInfoList.add(new FileInfo("9", "http://marketdown1.gamedog.cn/big/game/dongzuo/47096/ditiepaokunyb_yxdog.apk","ditiepaokunyb_yxdog.apk", 
				0, 0));
		mFileInfoList.add(new FileInfo("10", "http://marketdown1.gamedog.cn/among/game/yizhi/8226/shuiguorenzhexnb_yxdog.apk","shuiguorenzhexnb_yxdog.apk", 
				0, 0));
		
		checkFileInfoStatu();

		mAdapter = new FileListAdapter2(this, mFileInfoList);

		mListView.setAdapter(mAdapter);

		// 注册广播接收器

		IntentFilter filter = DownloadReceiver.getIntentFilter();
		registerReceiver(mReceiver, filter);

		mMainActivity = this;
	}

	private void checkFileInfoStatu() {
		// TODO Auto-generated method stub
		ThreadDAOImpl impl=new ThreadDAOImpl(this);
		
		//从数据库里获取信息判断是否是下载还是继续下载
		/*List<FileInfo>mTemps=impl.getFileInfo();
		if (mTemps.size()>0) {
			for (int i = 0; i < mTemps.size(); i++) {
				for (int j = 0; j < mFileInfoList.size(); j++) {
					if (mTemps.get(i).getUrl().equals(mFileInfoList.get(j).getUrl())) {
						mFileInfoList.get(j).setStatu(FileInfo.STATU_PAUSE);
						break;
					}
				}
			}
		}*/
		
		File file=new File(Param.DOWNLOAD_PATH);
		String[]name=file.list();
		for (int i = 0; i < name.length; i++) {
			for (int j = 0; j < mFileInfoList.size(); j++) {
				if (name[i].equals(mFileInfoList.get(j).getFileName())) {
					mFileInfoList.get(j).setStatu(FileInfo.STATU_FINISH);
					break;
				}
				if (name[i].startsWith((mFileInfoList.get(j).getFileName()))&&(name[i]!=mFileInfoList.get(j).getFileName())) {
					mFileInfoList.get(j).setStatu(FileInfo.STATU_PAUSE);
					break;
				}
			}
		}
		
		
	}

	protected void onDestroy()

	{
		super.onDestroy();

		unregisterReceiver(mReceiver);
		Intent intent=new Intent(this,DownloadService.class);
		stopService(intent);

	}

	/**
	 * 更新UI的广播接收器
	 */
	DownloadFileReceiver mReceiver=new DownloadFileReceiver();
	
	class DownloadFileReceiver extends DownloadReceiver{

		@Override
		public void onStart(FileInfo fileInfo) {
			// TODO Auto-generated method stub
			textView.setText("onStart:"+fileInfo.getId());
			Toast.makeText(MainActivity.this,"onStart:"+fileInfo.toString(), 0).show();
			
			ThreadDAOImpl threadDAOImpl=new ThreadDAOImpl(MainActivity.this);
			threadDAOImpl.insertFileInfo(fileInfo);
			
		}

		@Override
		public void onUpData(FileInfo fileInfo) {
			// TODO Auto-generated method stub
			textView.setText("onUpate:"+fileInfo.getFinished());
			mAdapter.updateFileInfo(fileInfo);
			//Toast.makeText(MainActivity.this,"onUpdate:"+fileInfo.toString(), 0).show();
		}

		@Override
		public void onPause(final FileInfo fileInfo) {
			// TODO Auto-generated method stub
			textView.setText("onPause:"+fileInfo.getId());
			Toast.makeText(MainActivity.this,"onPause:"+fileInfo.toString(), 0).show();
			
			final File file=new File(DownloadService.DOWNLOAD_PATH,fileInfo.getFileName());
			
			AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("提醒");
			builder.setMessage("是否取消下载？");
			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					DownloadService.sendMeg(MainActivity.this, DownloadService.ACTION_CANCEL, fileInfo);
					fileInfo.setStatu(FileInfo.STATU_NORMAL);
					mAdapter.updateFileInfo(fileInfo);
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
		public void onCancel(FileInfo fileInfo) {
			// TODO Auto-generated method stub
			fileInfo.setStatu(FileInfo.STATU_NORMAL);
			mAdapter.updateFileInfo(fileInfo);
			textView.setText("onCancel:"+fileInfo.getId());
			Toast.makeText(MainActivity.this,"onCancel:"+fileInfo.toString(), 0).show();
			
			ThreadDAOImpl threadDAOImpl=new ThreadDAOImpl(MainActivity.this);
			threadDAOImpl.deleteFielInfo(fileInfo.getId());
		}

		@Override
		public void onFinished(FileInfo fileInfo) {
			// TODO Auto-generated method stub
			mAdapter.updateFileInfo(fileInfo);
			textView.setText("onFinished:"+fileInfo.getId());
			Toast.makeText(MainActivity.this,"onFinished:"+fileInfo.toString(), 0).show();
			ThreadDAOImpl threadDAOImpl=new ThreadDAOImpl(MainActivity.this);
			threadDAOImpl.deleteFielInfo(fileInfo.getId());
		}

		@Override
		public void onExist(final FileInfo fileInfo) {
			// TODO Auto-generated method stub
			final File file=new File(DownloadService.DOWNLOAD_PATH,fileInfo.getFileName());
			
			AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("提醒");
			builder.setMessage("文件已下载，是否重新下载?");
			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					fileInfo.setStatu(FileInfo.STATU_NORMAL);
					mAdapter.updateFileInfo(fileInfo);
					DownloadService.sendMeg(MainActivity.this, DownloadService.ACTION_CANCEL, fileInfo);
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
			Toast.makeText(MainActivity.this,"onFailure:"+e.toString(), 0).show();
		}

		@Override
		public void onRuning(FileInfo fileInfo) {
			// TODO Auto-generated method stub
			textView.setText("fileId:"+fileInfo.getId()+"finish:"+fileInfo.getFinished()+"%");
		}
		
		
	}
	

	/**
	 * 监听返回键
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// if (KeyEvent.KEYCODE_BACK == keyCode && mStartBtn != null)
		// 按了返回键时应暂停下载
		{
			//
			//mStopBtn.performClick(); // 模拟按下暂停按钮
		}

		return super.onKeyUp(keyCode, event);

	}
}
