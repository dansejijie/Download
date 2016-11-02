package com.chong.downloadfile.param;

import android.R.integer;
import android.os.Environment;
/**
 * 控制下载的变量
 * 需要修改可以在这里修改
 * @author zhengqiang
 *
 */
public class Param {

	//下载文件存放路径
	public static final String DOWNLOAD_PATH=Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
	//单个文件默认开启线程数
	public static final int THREAD_COUNT=1;
	
	//设置当下载完成后是否打开安装界面
	public static final boolean IS_INSTALL=true;
	
	

}
